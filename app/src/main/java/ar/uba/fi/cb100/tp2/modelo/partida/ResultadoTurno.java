package ar.uba.fi.cb100.tp2.modelo.partida;

/**
 * Lo que devuelve la partida después de intentar una acción.
 * @param accionValida false si la acción no se pudo hacer, por ejemplo moverse
 *                     contra una pared: esa acción no cuenta como paso
 * @param estado       cómo quedó la partida después de la acción
 */
public record ResultadoTurno(boolean accionValida, EstadoPartida estado) {

}
