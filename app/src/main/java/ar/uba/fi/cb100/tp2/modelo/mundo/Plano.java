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

    /**
     * @param entidad el monstruo o cofre a poner en el plano
     */
    public void agregarEntidad(Entidad entidad) {
        this.entidades.add(entidad);
    }

    /**
     * Saca una entidad del plano, por ejemplo un cofre que ya se abrió.
     * @param entidad la entidad a sacar
     */
    public void quitarEntidad(Entidad entidad) {
        this.entidades.remove(entidad);
    }

    /**
     * Las entidades que hay en una celda. Puede haber más de una
     * (dos monstruos en la misma celda) o ninguna.
     * @param posicion la celda, de 1 a n
     * @return las entidades en esa celda; vacía si no hay ninguna
     */
    public List<Entidad> obtenerEntidades(Posicion posicion) {
        List<Entidad> encontradas = new LinkedList<>();
        for (Entidad entidad : this.entidades) {
            if (entidad.getPosicion().equals(posicion)) {
                encontradas.add(entidad);
            }
        }
        return encontradas;
    }

    /**
     * Una copia de todas las entidades, para recorrerlas en cada paso.
     * Es una copia para que se pueda quitar una entidad del plano
     * mientras se recorre sin que se rompa el recorrido.
     * @return todas las entidades del plano
     */
    public List<Entidad> getEntidades() {
        return new LinkedList<>(this.entidades);
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
