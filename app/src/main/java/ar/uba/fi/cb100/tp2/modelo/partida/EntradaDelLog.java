package ar.uba.fi.cb100.tp2.modelo.partida;

/**
 * Una línea del log de la partida: qué pasó y en qué paso.
 *
 * @param paso  el paso en que pasó (0 = antes del primer paso)
 * @param texto qué pasó, en palabras para el jugador
 */
public record EntradaDelLog(int paso, String texto) {

    /**
     * @throws IllegalArgumentException si el paso es negativo o el texto está vacío
     */
    public EntradaDelLog {
        if (paso < 0) {
            throw new IllegalArgumentException("El paso no puede ser negativo: " + paso);
        }
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto del log no puede estar vacío");
        }
    }
}