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
        JsonObject json = leerObjetoJson(parametrosJson);
        Parametros parametros = leerParametros(json);

        throw new UnsupportedOperationException(
                "Parámetros validados; generación pendiente de implementar"
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
     * Lee los parámetros y comprueba sus restricciones básicas.
     */
    private Parametros leerParametros(JsonObject json) {
        int version = leerEntero(json, "version");
        String dificultad = leerTexto(json, "dificultad");
        int filas = leerEntero(json, "filas");
        int columnas = leerEntero(json, "columnas");
        long semilla = leerLong(json, "semilla");
        int cofres = leerEntero(json, "cofres");
        int monstruos = leerEntero(json, "monstruos");
        boolean conSalida = leerBooleano(json, "conSalida");

        if (version != 1) {
            throw new IllegalArgumentException(
                    "La versión del formato debe ser 1"
            );
        }

        if (!dificultad.equals("FACIL")
                && !dificultad.equals("MEDIO")
                && !dificultad.equals("DIFICIL")) {
            throw new IllegalArgumentException(
                    "La dificultad debe ser FACIL, MEDIO o DIFICIL"
            );
        }

        if (filas < 5 || columnas < 5) {
            throw new IllegalArgumentException(
                    "Las filas y columnas deben ser al menos 5"
            );
        }

        if (cofres < 0 || monstruos < 0) {
            throw new IllegalArgumentException(
                    "Las cantidades de cofres y monstruos no pueden ser negativas"
            );
        }

        long celdasInteriores = (long) (filas - 2) * (columnas - 2);
        int posicionesReservadas = conSalida ? 2 : 1;
        long entidadesSolicitadas = (long) cofres + monstruos;

        if (entidadesSolicitadas > celdasInteriores - posicionesReservadas) {
            throw new IllegalArgumentException(
                    "No hay espacio interior suficiente para cofres y monstruos"
            );
        }

        return new Parametros(
                version,
                dificultad,
                filas,
                columnas,
                semilla,
                cofres,
                monstruos,
                conSalida
        );
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
    /**
     * Lee un campo y exige que su valor sea texto.
     */
    private String leerTexto(JsonObject json, String nombre) {
        JsonElement campo = obtenerCampo(json, nombre);

        if (!campo.isJsonPrimitive()
                || !campo.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser texto"
            );
        }

        return campo.getAsString();
    }
    /**
     * Lee un campo y exige que sea un booleano.
     */
    private boolean leerBooleano(JsonObject json, String nombre) {
        JsonElement campo = obtenerCampo(json, nombre);

        if (!campo.isJsonPrimitive()
                || !campo.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un booleano"
            );
        }

        return campo.getAsBoolean();
    }

}