package ar.uba.fi.cb100.tp2.modelo.partida;

/**
 * Lo que pasaría si el jugador interactúa (tecla E) en la celda donde está
 * parado el héroe. La vista lo usa para avisarle al jugador que puede hacerlo.
 */
public enum Interaccion {
    /** No hay nada con qué interactuar. */
    NINGUNA,
    /** Hay un cofre cerrado. */
    ABRIR_COFRE,
    /** Hay una escalera: lleva a otro piso del mismo tablero. */
    USAR_ESCALERA,
    /** Hay un pasillo: lleva a otro tablero. */
    USAR_PASILLO
}
