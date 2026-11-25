package ar.uba.fi.cb100.tp2.modelo.monstruos;

/**
 * Los tipos de monstruo del juego. En el plano todos son una 'M': el tipo se
 * sortea al cargarlo, y este enum es lo que permite saber después cuál tocó
 * (para elegir su imagen, y para guardar y retomar la partida).
 *
 * El modelo sólo conoce el nombre del tipo, no su imagen: de buscarla se
 * encarga la vista.
 */
public enum TipoDeMonstruo {
    ERRANTE,
    CAZADOR,
    DORMILON,
    FANTASMA,
    LADRON
}
