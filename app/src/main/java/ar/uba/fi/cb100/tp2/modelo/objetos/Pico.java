package ar.uba.fi.cb100.tp2.modelo.objetos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.RomperPared;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;


/**
 * Objeto que se guarda en el inventario. Al usarlo, rompe UNA pared vecina al
 * héroe: la primera en el orden arriba, abajo, izquierda, derecha que no sea
 * borde. Se gasta aunque no haya ninguna pared para romper.
 */
public class Pico extends Objeto {

    public static final String ID = "PICO";

    /**
     * Constructor de la clase, le envia el ID al padre.
     */
    public Pico() {
        super(ID);
    }

    /**
     * Cuando el heroe usa el objeto, rompe la pared mas cercana que
     * no sea borde.
     *
     * @param heroe  heroe que usa el objeto.
     * @param agenda la agenda, para efectos que vencen más adelante.
     */
    @Override
    public void usar(Heroe heroe, Agenda agenda) {
        agenda.programar(1, new RomperPared());
    }
}
