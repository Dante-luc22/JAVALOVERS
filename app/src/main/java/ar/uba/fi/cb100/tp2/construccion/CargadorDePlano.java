package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.cofres.CatalogoDeCofres;
import ar.uba.fi.cb100.tp2.modelo.monstruos.CatalogoDeMonstruos;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ar.uba.fi.cb100.material.ValidacionesUtiles;
import java.util.Random;

/**
 * Convierte un JSON de plano validado en objetos del modelo.
 * Los tipos concretos de entidades se sortean usando los catálogos.
 */
public class CargadorDePlano {

    private final CatalogoDeCofres catalogoDeCofres;
    private final CatalogoDeMonstruos catalogoDeMonstruos;
    private final Random azar;

    public CargadorDePlano(
            CatalogoDeCofres catalogoDeCofres,
            CatalogoDeMonstruos catalogoDeMonstruos,
            Random azar
    ) {
        ValidacionesUtiles.validarDistintoDeNull(catalogoDeCofres, "catálogo de cofres");
        ValidacionesUtiles.validarDistintoDeNull(catalogoDeMonstruos, "catálogo de monstruos");
        ValidacionesUtiles.validarDistintoDeNull(azar, "generador de azar");
        this.catalogoDeCofres = catalogoDeCofres;
        this.catalogoDeMonstruos = catalogoDeMonstruos;
        this.azar = azar;
    }

    /**
     * Valida el JSON y construye el plano correspondiente.
     *
     * @param json JSON de salida de un generador
     * @return plano con sus terrenos, posiciones y entidades
     * @throws IllegalArgumentException si el validador rechaza el plano
     * @throws IllegalStateException si se necesita crear una entidad
     *         y su catálogo está vacío
     */
    public Plano cargar(String json) {
        new ValidadorPlano().validar(json);

        JsonObject objeto = JsonParser.parseString(json).getAsJsonObject();
        JsonObject parametros = objeto.getAsJsonObject("parametros");

        int filas = parametros.get("filas").getAsInt();
        int columnas = parametros.get("columnas").getAsInt();

        Tablero tablero = new Tablero(filas, columnas);

        Posicion entrada = leerPosicion(objeto.get("entrada"));
        Posicion salida = leerPosicion(objeto.get("salida"));

        Plano plano = new Plano(tablero, entrada, salida);
        JsonArray grilla = objeto.getAsJsonArray("plano");

        for (int fila = 0; fila < filas; fila++) {
            String linea = grilla.get(fila).getAsString();

            for (int columna = 0; columna < columnas; columna++) {
                char simbolo = linea.charAt(columna);
                Posicion posicion = new Posicion(fila + 1, columna + 1);

                tablero.setTerreno(posicion, interpretarTerreno(simbolo));
                agregarEntidad(plano, posicion, simbolo);
            }
        }

        return plano;
    }

    private Posicion leerPosicion(JsonElement elemento) {
        if (elemento == null || elemento.isJsonNull()) {
            return null;
        }

        JsonObject objeto = elemento.getAsJsonObject();

        return new Posicion(
                objeto.get("fila").getAsInt(),
                objeto.get("columna").getAsInt()
        );
    }

    private Terreno interpretarTerreno(char simbolo) {
        return switch (simbolo) {
            case '#' -> Terreno.PARED;
            case '~' -> Terreno.AGUA;
            case '^' -> Terreno.LAVA;
            case '.', 'E', 'S', 'C', 'M' -> Terreno.PISO;
            default -> throw new IllegalArgumentException(
                    "Símbolo de terreno desconocido: " + simbolo
            );
        };
    }

    private void agregarEntidad(
            Plano plano,
            Posicion posicion,
            char simbolo
    ) {
        switch (simbolo) {
            case 'C' -> plano.agregarEntidad(
                    catalogoDeCofres.crearAlAzar(posicion, azar)
            );
            case 'M' -> plano.agregarEntidad(
                    catalogoDeMonstruos.crearAlAzar(posicion, azar)
            );
            default -> {
                // Esta celda no contiene una entidad.
            }
        }
    }
}