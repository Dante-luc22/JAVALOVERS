package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Monstruo;

public abstract class Cofre extends Entidad {

    private Cofre(){}

    protected abstract abrir(Heroe heroe, Agenda agenda);

    public void actuar(Plano plano, Heroe heroe){}

    public void alEncontrarseConHeroe(Heroe heroe, Agenda agenda){}
}
