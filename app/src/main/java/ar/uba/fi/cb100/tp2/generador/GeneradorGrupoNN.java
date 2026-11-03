package ar.uba.fi.cb100.tp2.generador;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Genera laberintos mediante backtracking con una pila explícita.
 */
public class GeneradorGrupoNN implements GeneradorDeLaberinto {

    @Override
    public String generar(String parametrosJson) {
        throw new UnsupportedOperationException(
                "Generación pendiente de implementar"
        );
    }

    /**
     * Coordenadas internas, con índices desde cero.
     */
    private record Celda(int fila, int columna) {
    }

    /**
     * Parámetros del contrato de generación.
     */
    private record Parametros(
            int version,
            String dificultad,
            int filas,
            int columnas,
            long semilla,
            int cofres,
            int monstruos,
            boolean conSalida
    ) {
    }

    /**
     * Interpreta el texto recibido como un objeto JSON.
     *
     * @throws IllegalArgumentException si el texto es nulo,
     *         está mal formado o no representa un objeto
     */
    private JsonObject leerObjetoJson(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "Los parámetros JSON no pueden estar vacíos"
            );
        }

        try {
            JsonElement elemento = JsonParser.parseString(texto);

            if (!elemento.isJsonObject()) {
                throw new IllegalArgumentException(
                        "Los parámetros deben ser un objeto JSON"
                );
            }

            return elemento.getAsJsonObject();
        } catch (JsonParseException e) {
            throw new IllegalArgumentException(
                    "El JSON de parámetros es inválido", e
            );
        }
    }

}