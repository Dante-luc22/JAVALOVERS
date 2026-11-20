package ar.uba.fi.cb100.tp2.modelo.efectos;


import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;

/**
 * Clase envenenado, si el heroe esta envenenado, el costo del
 * terreno se le suma 1.
 */
public class Envenenado extends Efecto {

    /**
     * Constructor de Envenenado, inicia con la duracion
     * del efecto.
     *
     * @param duracion del efecto.
     */
    public Envenenado(int duracion) {
        super(duracion);
    }

    /**
     * Metodo para ajustar el costo al tener este efecto.
     *
     * @param terreno      donde camina el heroe
     * @param costoActual  del terreno.
     * @param costoDelPiso lo que le saca el piso al heroe
     * @return el costo actual + 1 en cualquier terreno.
     */
    @Override
    public int ajustarCosto(Terreno terreno, int costoActual, int costoDelPiso){
        return costoActual + 1;
    }
}
