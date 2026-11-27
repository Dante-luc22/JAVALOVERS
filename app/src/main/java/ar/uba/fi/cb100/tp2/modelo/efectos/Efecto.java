package ar.uba.fi.cb100.tp2.modelo.efectos;

import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.material.ValidacionesUtiles;

public abstract class Efecto {

    private int pasosRestantes;

    /**
     * Crea un efecto con una cierta duracion.
     *
     * @param duracion del efecto.
     * @throws IllegalArgumentException si la duracion es menor que cero.
     */
    protected Efecto(int duracion) {
        ValidacionesUtiles.esMayorQueCero(duracion, "duracion");
        this.pasosRestantes = duracion;
    }

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
     * Metodo para restarle 1 a la cantidad de pasos restantes con el efecto.
     */
    public void pasarUnPaso(){
        this.pasosRestantes--;
        if (pasosRestantes < 0) {
            pasosRestantes = 0;
        }
    }

    /**
     * @return true si el heroe se quedo sin el efecto.
     */
    public boolean termino() {
        return pasosRestantes == 0;
    }

    /**
     * @return pasos que le quedan al heroe con el efecto activo.
     */
    public int  getPasosRestantes() {
        return pasosRestantes;
    }
}
