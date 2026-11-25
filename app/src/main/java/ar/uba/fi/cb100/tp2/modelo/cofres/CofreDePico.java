package ar.uba.fi.cb100.tp2.modelo.cofres;


import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.objetos.Pico;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;


/**
 * Al abrirse el cofre, deja el pico en el inventario del heroe,
 * y sin romper nada en el momento en que se abre.
 */
public class CofreDePico extends Cofre {

    /**
     * Constructor del cofre de Pico, recibe la posicion
     * donde va a estar.
     * @param posicion del cofre.
     */
    public CofreDePico(Posicion posicion){
        super(posicion);
    }

    /**
     * Al abrirse el cofre, se aniade el objeto Pico al inventario
     * del heroe.
     *
     * @param heroe  el que abre el cofre;
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    protected void abrir(Heroe heroe, Agenda agenda){
        heroe.getInventario().agregar(new Pico());
    }
}
