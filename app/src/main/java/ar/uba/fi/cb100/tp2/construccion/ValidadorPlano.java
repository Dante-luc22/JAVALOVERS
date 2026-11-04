package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Valida un plano en el formato JSON del Anexo B.3 con las once reglas de B.4.
 */
public class ValidadorPlano {

    // Datos leídos del JSON. Las posiciones quedan en base 0.
    private int filas;
    private int columnas;
    private int cofres;
    private int monstruos;
    private boolean conSalida;
    private Posicion entrada;
    private Posicion salida; // null si el plano no tiene salida
    private char[][] plano;

    /**
     * Si el plano es válido no hace nada. Si alguna regla falla, lanza
     * IllegalArgumentException con un mensaje que empieza por la regla
     * (por ejemplo "R5: ...").
     */
    public void validar(String json) {
        leer(json);
    }

    /**
     * Lee el JSON y guarda sus datos en los atributos.
     */
    private void leer(String json) {
        try {
            JsonObject raiz = JsonParser.parseString(json).getAsJsonObject();

            JsonObject parametros = obtener(raiz, "parametros").getAsJsonObject();
            filas = obtener(parametros, "filas").getAsInt();
            columnas = obtener(parametros, "columnas").getAsInt();
            cofres = obtener(parametros, "cofres").getAsInt();
            monstruos = obtener(parametros, "monstruos").getAsInt();
            conSalida = obtener(parametros, "conSalida").getAsBoolean();

            entrada = leerPosicion(obtener(raiz, "entrada"));

            JsonElement salidaJson = raiz.get("salida");
            if (salidaJson == null || salidaJson.isJsonNull()) {
                salida = null;
            } else {
                salida = leerPosicion(salidaJson);
            }

            JsonArray filasDelPlano = obtener(raiz, "plano").getAsJsonArray();
            plano = new char[filasDelPlano.size()][];
            for (int fila = 0; fila < filasDelPlano.size(); fila++) {
                plano[fila] = filasDelPlano.get(fila).getAsString().toCharArray();
            }
        } catch (JsonParseException | IllegalStateException | UnsupportedOperationException e) {
            throw new IllegalArgumentException("El JSON del plano tiene un formato inválido", e);
        }
    }

    /**
     * Devuelve el campo pedido, o lanza una excepción si no está.
     */
    private JsonElement obtener(JsonObject objeto, String nombre) {
        JsonElement campo = objeto.get(nombre);
        if (campo == null || campo.isJsonNull()) {
            throw new IllegalArgumentException("Falta el campo: " + nombre);
        }
        return campo;
    }

    /**
     * Convierte {"fila": f, "columna": c} (base 1) en una Posicion (base 0).
     */
    private Posicion leerPosicion(JsonElement elemento) {
        JsonObject coordenadas = elemento.getAsJsonObject();
        int fila = obtener(coordenadas, "fila").getAsInt();
        int columna = obtener(coordenadas, "columna").getAsInt();
        return new Posicion(fila - 1, columna - 1);
    }
}
