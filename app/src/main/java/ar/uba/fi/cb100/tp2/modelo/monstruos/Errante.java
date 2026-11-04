package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.Random;

public class Errante extends Monstruo {

    private final Random azar;

    public Errante(Posicion posicionInicial, int danio, Random azar) {
        if (posicionInicial == null) {
            throw new IllegalArgumentException(
                    "La posicion inicial no puede ser nula.");
        }
        if (danio <= 0) {
            throw new IllegalArgumentException(
                    "El danio debe ser mayor qe cero.");
        }
        super(posicionInicial, danio);
        if (azar == null) {
            throw new NullPointerException("azar == null");
        }
        this.azar = azar;
    }


    @Override
    protected Posicion decidirMovimiento(Plano plano, Posicion heroe) {
        Plano vecinas = (Plano) plano.getTablero().vecinasTransitables(getPosicion());
        if (plano.getTablero().vecinasTransitables(getPosicion()).isEmpty()) {
            return getPosicion();
        } else {
            return vecinas.getSalida();
        }
    }

    @Override
    public void alalcanzarAlHeroe(Heroe heroe, Agenda agenda) {
        atacarYVolver(heroe);
    }

}
