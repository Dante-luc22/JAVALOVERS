package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.generador.GeneradorGrupoNN;
import ar.uba.fi.cb100.tp2.modelo.cofres.CatalogoDeCofres;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePuntos;
import ar.uba.fi.cb100.tp2.modelo.monstruos.CatalogoDeMonstruos;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Errante;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CargadorDePlanoTest {

    private CargadorDePlano cargador;

    @BeforeEach
    void prepararCargador() {
        Random azar = new Random(1);

        CatalogoDeCofres cofres = new CatalogoDeCofres();
        cofres.registrar("puntos", CofreDePuntos::new);

        CatalogoDeMonstruos monstruos = new CatalogoDeMonstruos();
        monstruos.registrar(
                "errante",
                posicion -> new Errante(posicion, 10, azar)
        );

        cargador = new CargadorDePlano(cofres, monstruos, azar);
    }

    @Test
    void convierteCadaCeldaAlTerrenoCorrespondiente() {
        String json = generarJson(true);
        Plano plano = cargador.cargar(json);

        JsonArray grilla = leerObjeto(json).getAsJsonArray("plano");

        for (int fila = 0; fila < grilla.size(); fila++) {
            String linea = grilla.get(fila).getAsString();

            for (int columna = 0; columna < linea.length(); columna++) {
                char simbolo = linea.charAt(columna);
                Posicion posicion = new Posicion(fila + 1, columna + 1);

                Terreno esperado = switch (simbolo) {
                    case '#' -> Terreno.PARED;
                    case '~' -> Terreno.AGUA;
                    case '^' -> Terreno.LAVA;
                    case '.', 'E', 'S', 'C', 'M' -> Terreno.PISO;
                    default -> throw new AssertionError(
                            "Símbolo inesperado en el ejemplo: " + simbolo
                    );
                };

                assertEquals(
                        esperado,
                        plano.getTablero().getTerreno(posicion),
                        "Terreno incorrecto en " + posicion
                );
            }
        }

        // Verifica también los límites del tablero construido.
        assertTrue(plano.getTablero().estaDentro(new Posicion(15, 20)));
        assertFalse(plano.getTablero().estaDentro(new Posicion(16, 20)));
        assertFalse(plano.getTablero().estaDentro(new Posicion(15, 21)));
    }

    @Test
    void conservaEntradaYSalidaConCoordenadasDesdeUno() {
        String json = generarJson(true);
        JsonObject objeto = leerObjeto(json);

        Plano plano = cargador.cargar(json);

        assertEquals(
                leerPosicion(objeto.getAsJsonObject("entrada")),
                plano.getEntrada()
        );
        assertEquals(
                leerPosicion(objeto.getAsJsonObject("salida")),
                plano.getSalida()
        );
    }

    @Test
    void cargaPlanoSinSalida() {
        String json = generarJson(false);

        Plano plano = cargador.cargar(json);

        assertNotNull(plano.getEntrada());
        assertNull(plano.getSalida());
    }

    @Test
    void creaEntidadesDelCatalogoEnLasPosicionesDelJson() {
        String json = generarJson(true);
        Plano plano = cargador.cargar(json);

        JsonArray grilla = leerObjeto(json).getAsJsonArray("plano");

        // El ejemplo pide dos cofres y dos monstruos.
        assertEquals(4, plano.getEntidades().size());

        for (int fila = 0; fila < grilla.size(); fila++) {
            String linea = grilla.get(fila).getAsString();

            for (int columna = 0; columna < linea.length(); columna++) {
                char simbolo = linea.charAt(columna);
                Posicion posicion = new Posicion(fila + 1, columna + 1);
                List<Entidad> entidades = plano.obtenerEntidades(posicion);

                if (simbolo == 'C') {
                    assertEquals(1, entidades.size());
                    assertInstanceOf(CofreDePuntos.class, entidades.get(0));
                } else if (simbolo == 'M') {
                    assertEquals(1, entidades.size());
                    assertInstanceOf(Errante.class, entidades.get(0));
                } else {
                    assertTrue(
                            entidades.isEmpty(),
                            "Entidad inesperada en " + posicion
                    );
                }
            }
        }
    }

    @Test
    void rechazaUnPlanoConElBordeAbiertoAntesDeCrearEntidades() {
        JsonObject objeto = leerObjeto(generarJson(true));
        JsonArray grilla = objeto.getAsJsonArray("plano");

        String primeraFila = grilla.get(0).getAsString();
        grilla.set(
                0,
                new JsonPrimitive("." + primeraFila.substring(1))
        );

        // Si intenta crear entidades antes de validar, el test falla.
        CatalogoDeCofres cofres = new CatalogoDeCofres();
        cofres.registrar("prohibido", posicion -> {
            throw new AssertionError("Se creó un cofre antes de validar");
        });

        CatalogoDeMonstruos monstruos = new CatalogoDeMonstruos();
        monstruos.registrar("prohibido", posicion -> {
            throw new AssertionError("Se creó un monstruo antes de validar");
        });

        CargadorDePlano cargadorControlado = new CargadorDePlano(
                cofres, monstruos, new Random(1)
        );

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> cargadorControlado.cargar(objeto.toString())
        );

        assertTrue(error.getMessage().startsWith("R3:"));
    }

    private String generarJson(boolean conSalida) {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("version", 1);
        parametros.addProperty("dificultad", "MEDIO");
        parametros.addProperty("filas", 15);
        parametros.addProperty("columnas", 20);
        parametros.addProperty("semilla", 1L);
        parametros.addProperty("cofres", 2);
        parametros.addProperty("monstruos", 2);
        parametros.addProperty("conSalida", conSalida);

        return new GeneradorGrupoNN().generar(parametros.toString());
    }

    private JsonObject leerObjeto(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private Posicion leerPosicion(JsonObject objeto) {
        return new Posicion(
                objeto.get("fila").getAsInt(),
                objeto.get("columna").getAsInt()
        );
    }
}