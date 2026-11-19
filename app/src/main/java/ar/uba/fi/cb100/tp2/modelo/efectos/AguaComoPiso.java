package ar.uba.fi.cb100.tp2.modelo.efectos;

import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;

public class AguaComoPiso extends Efecto{

    /**
     * Constructor de la clase, usa super para enviar la duracion
     * al padre.
     *
     * @param duracion del efecto
     */
    public AguaComoPiso(int duracion) {
        super(duracion);
    }

    /**
     * Metodo para ajustar el costo del terreno
     *
     * @param terreno      donde camina el heroe
     * @param costoActual  del terreno.
     * @param costoDelPiso lo que le saca el piso al heroe
     * @return el costo actual del agua.
     */
    @Override
    public int ajustarCosto(Terreno terreno, int costoActual, int costoDelPiso) {
        if (terreno == Terreno.AGUA) {
            return costoDelPiso;
        } else  {
            return costoActual;
        }
    }
}
