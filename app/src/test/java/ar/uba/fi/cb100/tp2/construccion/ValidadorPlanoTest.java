package ar.uba.fi.cb100.tp2.construccion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidadorPlanoTest {

    /**
     * Plano de ejemplo del Anexo B.3. Según B.5 cumple las once reglas.
     */
    private static final String PLANO_EJEMPLO = """
            {
              "version": 1,
              "generador": "Grupo 03",
              "parametros": { "version": 1, "dificultad": "MEDIO", "filas": 9, "columnas": 12,
                              "semilla": 7, "cofres": 1, "monstruos": 1, "conSalida": true },
              "entrada": { "fila": 2, "columna": 2 },
              "salida":  { "fila": 8, "columna": 10 },
              "plano": [
                "############",
                "#E..#......#",
                "#.#.#.##.#.#",
                "#.#...#..~.#",
                "#.###.#.##.#",
                "#...#...C..#",
                "#.#.#.#.##.#",
                "#.M.....#S.#",
                "############"
              ]
            }
            """;

    @Test
    void elPlanoDeEjemploEsValido() {
        assertDoesNotThrow(() -> new ValidadorPlano().validar(PLANO_EJEMPLO));
    }

    /**
     * Verifica que el validador rechace el plano y que el mensaje nombre la regla.
     */
    private void assertRechaza(String regla, String json) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new ValidadorPlano().validar(json));

        assertTrue(error.getMessage().startsWith(regla + ":"), error.getMessage());
    }

    @Test
    void r1RechazaCantidadDeFilasDistintaALaDeLosParametros() {
        assertRechaza("R1", PLANO_EJEMPLO.replace("\"filas\": 9", "\"filas\": 10"));
    }

    @Test
    void r2RechazaSimboloDesconocido() {
        assertRechaza("R2", PLANO_EJEMPLO.replace("\"#.#.#.##.#.#\"", "\"#.#.#.##X#.#\""));
    }

    @Test
    void r3RechazaBordeSinPared() {
        assertRechaza("R3", PLANO_EJEMPLO.replace("\"#E..#......#\"", "\"#E..#.......\""));
    }

    @Test
    void r4RechazaEntradaQueNoApuntaALaE() {
        assertRechaza("R4", PLANO_EJEMPLO.replace(
                "\"entrada\": { \"fila\": 2, \"columna\": 2 }",
                "\"entrada\": { \"fila\": 3, \"columna\": 2 }"));
    }

    @Test
    void r7RechazaPocasCeldasTransitables() {
        // Una cruz de 5 celdas en un plano de 7 x 7: 5 de 49, menos del 30 %.
        String cruz = """
                {
                  "parametros": { "filas": 7, "columnas": 7, "cofres": 0, "monstruos": 0, "conSalida": false },
                  "entrada": { "fila": 3, "columna": 4 },
                  "salida": null,
                  "plano": [
                    "#######",
                    "#######",
                    "###E###",
                    "##...##",
                    "###.###",
                    "#######",
                    "#######"
                  ]
                }
                """;
        assertRechaza("R7", cruz);
    }

    @Test
    void r8RechazaCantidadDeCofresDistinta() {
        assertRechaza("R8", PLANO_EJEMPLO.replace("\"cofres\": 1", "\"cofres\": 2"));
    }

    @Test
    void r10RechazaDemasiadaAgua() {
        // 13 celdas de agua de 49 transitables: más del 25 %.
        assertRechaza("R10", PLANO_EJEMPLO
                .replace("\"#E..#......#\"", "\"#E..#~~~~~~#\"")
                .replace("\"#.#...#..~.#\"", "\"#.#~~~#~~~~#\""));
    }

    @Test
    void r11RechazaMonstruoCercaDeLaEntrada() {
        // Se mueve la M a 2 pasos de la E.
        assertRechaza("R11", PLANO_EJEMPLO
                .replace("\"#.M.....#S.#\"", "\"#.......#S.#\"")
                .replace("\"#E..#......#\"", "\"#E.M#......#\""));
    }
}
