package ar.uba.fi.cb100.tp2.modelo.partida;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Las celdas que pisó el héroe, separadas por plano. Es una tabla hash
 * (plano → conjunto de posiciones), como pide el enunciado: marcar y
 * preguntar se hacen en O(1) en cada paso.
 */
public class CeldasVisitadas {

    private final Map<ClaveDePlano, Set<Posicion>> porPlano = new HashMap<>();

    /**
     * @param ubicacion donde está el héroe
     * @return true si es la primera vez que la pisa
     */
    public boolean visitar(Ubicacion ubicacion) {
        ClaveDePlano clave = new ClaveDePlano(ubicacion.tablero(), ubicacion.piso());
        return this.porPlano.computeIfAbsent(clave, c -> new HashSet<>()).add(ubicacion.posicion());
    }

    /**
     * @param ubicacion la celda a consultar
     * @return si el héroe ya la pisó
     */
    public boolean fueVisitada(Ubicacion ubicacion) {
        Set<Posicion> celdas = this.porPlano.get(new ClaveDePlano(ubicacion.tablero(), ubicacion.piso()));
        return celdas != null && celdas.contains(ubicacion.posicion());
    }

    /**
     * @return cuántas celdas distintas pisó en ese plano
     */
    public int cantidadEn(int tablero, int piso) {
        Set<Posicion> celdas = this.porPlano.get(new ClaveDePlano(tablero, piso));
        return celdas == null ? 0 : celdas.size();
    }

    /**
     * @return cuántas celdas distintas pisó en toda la mazmorra
     */
    public int total() {
        int total = 0;
        for (Set<Posicion> celdas : this.porPlano.values()) {
            total += celdas.size();
        }
        return total;
    }
}