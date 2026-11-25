package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;


/**
 * Clase de cofre de escudo. El escudo se activa al abrirse, sin pasar
 * por el inventario; bloquea el proximo encuentro con un monstruo,
 * sea ataque o robo, y el monstruo vuelve a su inicio; no protege
 * de las trampas ni del costo del terreno; los escudos se acumulan,
 * dos cofres bloquean dos encuentros.
 */
public class CofreDeEscudo extends Cofre {

    /**
     * Constructor del cofre de Escudo, recibe la posicion
     * donde va a estar.
     * @param posicion del cofre.
     */
    public CofreDeEscudo(Posicion posicion) {
        super(posicion);
    }

    /**
     * Al abrirse el cofre, se aniade escudo al heroe.
     *
     * @param heroe  el que abre el cofre;
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    protected void abrir(Heroe heroe, Agenda agenda) {
        heroe.agregarEscudo();
    }
}
