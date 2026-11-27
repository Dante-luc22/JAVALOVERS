package ar.uba.fi.cb100.tp2.modelo.partida;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * El log de la partida: todo lo que fue pasando, en orden. Es una lista
 * (LinkedList), como pide el enunciado: se agrega siempre al final y se
 * recorre para mostrar lo que pasó.
 */
public class LogDePartida {

    private final LinkedList<EntradaDelLog> entradas = new LinkedList<>();

    /**
     * @param paso  el paso en que pasó
     * @param texto qué pasó
     */
    public void anotar(int paso, String texto) {
        this.entradas.addLast(new EntradaDelLog(paso, texto));
    }

    /**
     * Lo que pasó en un paso. Recorre desde el final, porque lo que se busca
     * casi siempre es lo último que pasó, y corta apenas encuentra un paso anterior.
     *
     * @param paso el paso buscado
     * @return las entradas de ese paso, en el orden en que pasaron
     */
    public List<EntradaDelLog> entradasDelPaso(int paso) {
        LinkedList<EntradaDelLog> delPaso = new LinkedList<>();
        Iterator<EntradaDelLog> desdeElFinal = this.entradas.descendingIterator();
        while (desdeElFinal.hasNext()) {
            EntradaDelLog entrada = desdeElFinal.next();
            if (entrada.paso() < paso) {
                break;
            }
            if (entrada.paso() == paso) {
                delPaso.addFirst(entrada);
            }
        }
        return delPaso;
    }

    /**
     * @return todas las entradas, en orden. Es una copia.
     */
    public List<EntradaDelLog> getEntradas() {
        return new ArrayList<>(this.entradas);
    }

    /**
     * @return cuántas entradas tiene
     */
    public int cantidad() {
        return this.entradas.size();
    }
}