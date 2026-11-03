package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.Random;

public class Errante extends Monstruo{

    private Random azar;


    public Errante(Posicion posicionInicial, int danioInicial){
        super(posicionInicial, danioInicial);
    }


    @Override
    public void decidirMovimiento(Plano plano, Posicion heroe) {

    }

    @Override
    public void alcanzarAlHeroe(Heroe heroe, Agenda agenda) {}
}
