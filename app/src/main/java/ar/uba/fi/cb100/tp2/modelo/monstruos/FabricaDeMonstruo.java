package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Interfaz que permite generar un monstruo en una posicion dada
 */
@FunctionalInterface
public interface FabricaDeMonstruo {

    /**
     * Metodo que crea un monstruo en la posicion dada.
     *
     * @param p posicion donde aparece
     * @return un monstruo nuevo en esa posicion
     */
    Monstruo crear(Posicion p);
}
