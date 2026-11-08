package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Clase CofreDePocion, al encontrarse con el heroe y ser abierto,
 * se le suma la cantidad indicada.
 */
public class CofreDePocion extends Cofre{

    private final int cantidad;

    /**
     * Constructor del cofre de pocion, pide la posicion donde se ubica,
     * y la cantidad de energia que suma.
     *
     * @param posicion posicion en el plano del cofre.
     * @param cantidad de energia que le suma al heroe
     */
    public CofreDePocion(Posicion posicion, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a 0, se recibio: " + cantidad);
        }
        super(posicion);

        this.cantidad = cantidad;
    }

    /**
     * Metodo que dice que sucede al abrirse el cofre
     *
     * @param heroe  el que abre el cofre;
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    protected void abrir(Heroe heroe, Agenda agenda) {
        heroe.sumarEnergia(cantidad);
    }
}
