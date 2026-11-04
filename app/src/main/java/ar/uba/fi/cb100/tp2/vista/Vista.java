package ar.uba.fi.cb100.tp2.vista;

public interface Vista {/**
 * Renderiza o exporta el estado actual en un paso determinado de la simulación.
 *
 * @param paso   Número del paso o iteración actual.
 * @param matriz Matriz de datos que representa el estado del tablero o mapa.
 */
void renderizar(int paso, int[][] matriz);
}
