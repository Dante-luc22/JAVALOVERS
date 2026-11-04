package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Interfaz que permite generar un cofre en una posicion dada.
 */
@FunctionalInterface
public interface FabricaDeCofre {

    /**
     * Metodo que crea un cofre en la posicion dada,
     *
     * @param p posicion donde aparece.
     * @return un Cofre nuevo en esa posicion.
     */
    Cofre crear(Posicion p);
}
