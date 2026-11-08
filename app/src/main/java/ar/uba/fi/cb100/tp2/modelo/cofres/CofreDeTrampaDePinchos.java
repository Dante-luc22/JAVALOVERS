package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Clase CofreDeTrampaDePinchos, al encontrarse con el heroe y ser abierto,
 * le resta la cantidad de energia dada.
 */
public class CofreDeTrampaDePinchos extends Cofre{

    private final int danio;

    /**
     * Constructor del cofre de trampa de pinchos, pide la posicion donde se ubica,
     * y la cantidad de danio que resta.
     *
     * @param posicion donde se ubica el cofre en el plano.
     * @param danio    que hace el cofre al heroe.
     * @throws IllegalArgumentException si el danio dado no es mayor a 0
     */
    public CofreDeTrampaDePinchos(Posicion posicion, int danio) {
        if (danio <= 0) {
            throw new IllegalArgumentException(
                    "El danio que realiza debe ser mayor que 0, se recibio: " + danio);
        }
        super(posicion);
        this.danio = danio;
    }

    /**
     * Metodo que dice que sucede cuando el heroe abre el cofre
     *
     * @param heroe  el que abre el cofre;
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    protected void abrir(Heroe heroe, Agenda agenda){
        heroe.restarEnergia(danio);
    }
}
