package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.RepelerMonstruos;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;


/**
 * Cofre que afecta a los monstruos: al abrirse programa en la agenda que
 * todos los monstruos del plano vuelvan a su posicion inicial. Actua
 * al empezar el proximo paso, no en el momento que se abre.
 */
public class CofreDeRepelente extends Cofre{

    /**
     * Constructor del cofre de repelente, recibe la posicion
     * donde va a estar
     *
     * @param posicion del cofre
     */
    public CofreDeRepelente(Posicion posicion) {
        super(posicion);
    }

    /**
     * Programa para el próximo paso que los monstruos del plano
     * vuelvan a su inicio
     *
     * @param heroe  el que abre el cofre;
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    protected void abrir(Heroe heroe, Agenda agenda){
        agenda.programar(1, new RepelerMonstruos());
    }
}
