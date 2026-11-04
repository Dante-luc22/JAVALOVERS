package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

public class CofreDePuntos extends Cofre{

    /**
     * Constructor del cofre de puntos, recibe la posicion
     * done va a estar
     *
     * @param posicion del cofre
     */
    public CofreDePuntos(Posicion posicion) {
        super(posicion);
    }

    /**
     * Metodo vacio ya que los 100 puntos ya los suma el cofre
     */
    @Override
    protected void abrir(Heroe heroe, Agenda agenda){
    }
}
