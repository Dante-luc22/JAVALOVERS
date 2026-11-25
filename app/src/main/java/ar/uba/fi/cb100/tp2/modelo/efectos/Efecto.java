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

}
