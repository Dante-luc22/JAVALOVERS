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

    /**
     * Obtiene un campo obligatorio del JSON.
     */
    private JsonElement obtenerCampo(JsonObject json, String nombre) {
        if (!json.has(nombre) || json.get(nombre).isJsonNull()) {
            throw new IllegalArgumentException(
                    "Falta el parámetro obligatorio: " + nombre
            );
        }

        return json.get(nombre);
    }

    /**
     * Lee un campo numérico y exige que sea un entero válido.
     */
    private int leerEntero(JsonObject json, String nombre) {
        JsonElement campo = obtenerCampo(json, nombre);

        if (!campo.isJsonPrimitive()
                || !campo.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un número entero"
            );
        }

        try {
            return campo.getAsBigDecimal().intValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un entero válido",
                    e
            );
        }
    }

    /**
     * Lee un campo numérico y exige que sea un entero largo válido.
     */
    private long leerLong(JsonObject json, String nombre) {
        JsonElement campo = obtenerCampo(json, nombre);

        if (!campo.isJsonPrimitive()
                || !campo.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un número entero"
            );
        }

        try {
            return campo.getAsBigDecimal().longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un long válido",
                    e
            );
        }
    }
}