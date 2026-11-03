package ar.uba.fi.cb100.tp2.modelo.valores;

/**
 * Las cuatro direcciones en las que se puede mover un personaje.
 * Cada una sabe cuánto cambia la fila y la columna.
 */
public enum Direccion {
    ARRIBA(-1, 0),
    ABAJO(1, 0),
    IZQUIERDA(0, -1),
    DERECHA(0, 1);

    private final int deltaFila;
    private final int deltaColumna;

    /**
     * @param deltaFila    cuánto cambia la fila al moverse
     * @param deltaColumna cuánto cambia la columna al moverse
     */
    Direccion(int deltaFila, int deltaColumna) {
        this.deltaFila = deltaFila;
        this.deltaColumna = deltaColumna;
    }

    /** @return cuánto cambia la fila */
    public int getDeltaFila() {
        return this.deltaFila;
    }

    /** @return cuánto cambia la columna */
    public int getDeltaColumna() {
        return this.deltaColumna;
    }
}