package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

public abstract class Monstruo extends Entidad {

    private final Posicion posicionInicial;
    private final int danio;

    public Monstruo(Posicion posicionInicial, int danio) {
        if  (posicionInicial == null) {
            throw new IllegalArgumentException(
                    "La posicion inicial no puede ser nula");
        }
        if (danio <= 0) {
            throw new IllegalArgumentException(
                    "El danio debe ser mayor que cero, se recibio: " + danio);
        }
        this.posicionInicial = posicionInicial;
        this.danio = danio;
    }


    protected abstract decidirMovimiento(Plano plano, Posicion heroe);

    protected abstract alAlcanzarAlHeroe(Heroe heroe, Agenda agenda);

    public void actuar(Plano plano, Heroe heroe){}

    public void alEncontrarseConHeroe(Heroe heroe, Agenda agenda){
        if (heroe == null) {
            throw new IllegalArgumentException(
                    "El heroe recibido no puede ser nulo");
        }
        heroe.restarEnergia(danio);
    }

    protected void volverAlInicio(){
    }


    // ================ GETTERS SIMPLES ================= //

    public Posicion getPosicionInicial() {
        return posicionInicial;
    }

    public int getDanio() {
        return danio;
    }

    public abstract void decidirMovimiento()
}
