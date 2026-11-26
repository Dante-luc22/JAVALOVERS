package ar.uba.fi.cb100.tp2.modelo.efectos;

import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;

public class AguaComoPiso extends Efecto{


    /**
     * Metodo para ajustar el costo del terreno
     *
     * @param terreno      donde camina el heroe
     * @param costoActual  del terreno.
     * @param costoDelPiso lo que le saca el piso al heroe
     * @return el costo del piso si el terreno es agua; si no,
     * costo actual sin cambios.
     */
    @Override
    public int ajustarCosto(Terreno terreno, int costoActual, int costoDelPiso) {
        if (terreno == Terreno.AGUA) {
            return costoDelPiso;
        } else  {
            return costoActual;
        }
    }

    /**
     * @return "AGUA_COMO_PISO"
     */
    @Override
    public TipoDeEfecto getTipo() {
        return TipoDeEfecto.AGUA_COMO_PISO;
    }
}
