package ar.uba.fi.cb100.tp2.generador;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

}