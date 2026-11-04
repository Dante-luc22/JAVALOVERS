package ar.uba.fi.cb100.tp2.construccion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
}
