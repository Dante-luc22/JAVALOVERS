package ar.uba.fi.cb100.tp2.modelo.efectos;

import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;

public abstract class Efecto {

    /**
     * Metodo para ajustar el costo del efecto en el terreno
     *
     * @param terreno      donde camina el heroe
     * @param costoActual  del terreno.
     * @param costoDelPiso lo que le saca el piso al heroe
     * @return el costo ajustado
     */
    public abstract int ajustarCosto(Terreno terreno, int costoActual, int costoDelPiso);

    /**
     * Cada efecto dice de que tipo es. Permite quitarlo por tipo
     * cuando vence, y guardarlo.
     *
     * @return el tipo de este efecto.
     */
    public abstract TipoDeEfecto getTipo();

    /**
     * Indica si el efecto hace que un terreno cueste como otro (por
     * ejemplo, el agua como piso). Estos efectos se aplican antes que
     * los que suman o restan, para que no pisen su resultado
     *
     * @return true si reemplaza el costo de un terreno; false por
     * defecto
     */
    public boolean cambiaElTerreno() {
        return false;
    }

}
