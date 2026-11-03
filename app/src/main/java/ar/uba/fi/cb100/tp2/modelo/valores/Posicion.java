package ar.uba.fi.cb100.tp2.modelo.valores;

/**
 * Una celda dentro de un plano. Fila y columna van de 1 a n.
 * Al ser record, equals y hashCode ya vienen bien hechos.
 */
public record Posicion(int fila, int columna) {

    /**
     * @param direccion hacia dónde moverse
     * @return la posición vecina en esa dirección
     */
    public Posicion mover(Direccion direccion) {
        return new Posicion(this.fila + direccion.getDeltaFila(),
                            this.columna + direccion.getDeltaColumna());
    }
}
