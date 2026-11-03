package ar.uba.fi.cb100.tp2.modelo.mundo;

import java.util.LinkedList;
import java.util.List;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

 /**
 * Un piso de la mazmorra: la grilla de terreno más las entidades
 * (monstruos y cofres) que hay adentro, la entrada y la salida.
 */
public class Plano {

    private final Tablero tablero;
    private final List<Entidad> entidades;
    private final Posicion entrada;
    private final Posicion salida;

    /**
     * Crea un plano sin entidades.
     * @param tablero la grilla de terreno
     * @param entrada dónde aparece el héroe (la E del generador)
     * @param salida  dónde está la salida (la S), o null si este plano no tiene
     */
    public Plano(Tablero tablero, Posicion entrada, Posicion salida) {
        this.tablero = tablero;
        this.entidades = new LinkedList<>();
        this.entrada = entrada;
        this.salida = salida;
    }

    /** @return la grilla de terreno */
    public Tablero getTablero() {
        return this.tablero;
    }

    /** @return dónde aparece el héroe */
    public Posicion getEntrada() {
        return this.entrada;
    }

    /** @return dónde está la salida, o null si este plano no la tiene */
    public Posicion getSalida() {
        return this.salida;
    }
}
