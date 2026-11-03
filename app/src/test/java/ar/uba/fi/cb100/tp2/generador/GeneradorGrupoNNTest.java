package ar.uba.fi.cb100.tp2.generador;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Random;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorGrupoNNTest {

    /**
     * Entrada válida nueva para cada prueba.
     */
    private JsonObject parametrosValidos() {
        return JsonParser.parseString("""
                {
                    "version": 1,
                    "dificultad": "FACIL",
                    "filas": 25,
                    "columnas": 40,
                    "semilla": 42,
                    "cofres": 12,
                    "monstruos": 4,
                    "conSalida": true
                }
                """).getAsJsonObject();
    }

    @Test
    void rechazaTextoVacio() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar("")
        );
    }

    @Test
    void rechazaJsonQueNoEsObjeto() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar("[]")
        );
    }

    @Test
    void rechazaFaltaDeFilas() {
        JsonObject json = parametrosValidos();
        json.remove("filas");

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void rechazaFilasMenoresACinco() {
        JsonObject json = parametrosValidos();
        json.addProperty("filas", 3);

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void rechazaFilasConParteFraccionaria() {
        JsonObject json = parametrosValidos();
        json.addProperty("filas", 25.5);

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void rechazaCofresNegativos() {
        JsonObject json = parametrosValidos();
        json.addProperty("cofres", -1);

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void rechazaMasCofresQueCeldas() {
        JsonObject json = parametrosValidos();
        json.addProperty("cofres", 1000);

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void rechazaConSalidaComoTexto() {
        JsonObject json = parametrosValidos();
        json.addProperty("conSalida", "true");

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void creaGrillaConDimensionesIndicadasYTodasParedes() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        char[][] grilla = generador.crearGrilla(5, 7);

        assertEquals(5, grilla.length, "Debe tener 5 filas");

        for (char[] fila : grilla) {
            assertEquals(7, fila.length, "Cada fila debe tener 7 columnas");

            for (char celda : fila) {
                assertEquals('#', celda, "Todas las celdas deben ser paredes");
            }
        }
    }

    @Test
    void excavarAbreElInicioYConservaLosBordes() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();
        char[][] grilla = generador.crearGrilla(15, 20);

        generador.excavar(grilla, new Random(42));

        assertEquals('.', grilla[1][1]);

        for (int columna = 0; columna < grilla[0].length; columna++) {
            assertEquals('#', grilla[0][columna]);
            assertEquals('#', grilla[grilla.length - 1][columna]);
        }

        for (int fila = 0; fila < grilla.length; fila++) {
            assertEquals('#', grilla[fila][0]);
            assertEquals('#', grilla[fila][grilla[fila].length - 1]);
        }
    }

    @Test
    void excavarConLaMismaSemillaProduceLaMismaGrilla() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();
        char[][] primera = generador.crearGrilla(15, 20);
        char[][] segunda = generador.crearGrilla(15, 20);

        generador.excavar(primera, new Random(42));
        generador.excavar(segunda, new Random(42));

        for (int fila = 0; fila < primera.length; fila++) {
            assertArrayEquals(
                    primera[fila],
                    segunda[fila],
                    "Las grillas difieren en la fila " + fila
            );
        }
    }

    @Test
    void agregaBifurcacionesSinSuperarElLimiteTransitable() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        char[][] grilla = {
                "#######".toCharArray(),
                "#.....#".toCharArray(),
                "#####.#".toCharArray(),
                "#.....#".toCharArray(),
                "#.#####".toCharArray(),
                "#.....#".toCharArray(),
                "#######".toCharArray()
        };

        boolean pudoAjustar = generador.agregarBifurcaciones(
                grilla, new Random(42)
        );

        assertTrue(pudoAjustar, "Debe poder ajustar este laberinto");

        int transitables = 0;
        int bifurcaciones = 0;

        for (int fila = 1; fila < grilla.length - 1; fila++) {
            for (int columna = 1;
                 columna < grilla[fila].length - 1;
                 columna++) {

                if (grilla[fila][columna] == '#') {
                    continue;
                }

                transitables++;
                int vecinos = 0;

                if (grilla[fila - 1][columna] != '#') {
                    vecinos++;
                }
                if (grilla[fila + 1][columna] != '#') {
                    vecinos++;
                }
                if (grilla[fila][columna - 1] != '#') {
                    vecinos++;
                }
                if (grilla[fila][columna + 1] != '#') {
                    vecinos++;
                }

                if (vecinos >= 3) {
                    bifurcaciones++;
                }
            }
        }

        long totalCeldas = (long) grilla.length * grilla[0].length;

        assertTrue(
                bifurcaciones * 10L >= transitables,
                "Al menos el 10 % debe tener tres o más vecinos"
        );

        assertTrue(
                transitables * 100L <= totalCeldas * 60,
                "No debe superar el 60 % de celdas transitables"
        );
    }
}