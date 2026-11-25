package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.efectos.AguaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.efectos.Envenenado;
import ar.uba.fi.cb100.tp2.modelo.efectos.QuitarEfecto;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Clase cofre de veneno, es una trampa con efecto temporal, al
 * abrirse el costo del terreno aumenta en 1.
 */
public class CofreDeVeneno extends Cofre{

    private static final int DURACION = 10;

    /**
     * Constructor del cofre de veneneo, inicia con la posicion del
     * cofre
     *
     * @param posicion donde esta el cofre
     */
    public CofreDeVeneno(Posicion posicion) {super(posicion);}

    /**
     * Metodo para indicar que pasa al abrir el cofre
     *
     * @param heroe  el que abre el cofre;
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    protected void abrir(Heroe heroe, Agenda agenda){
        Efecto efecto = new Envenenado();
        heroe.agregarEfecto(efecto);
        agenda.programar(DURACION, new QuitarEfecto(efecto));
    }
}
