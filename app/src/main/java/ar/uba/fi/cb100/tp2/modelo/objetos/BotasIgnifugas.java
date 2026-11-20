package ar.uba.fi.cb100.tp2.modelo.objetos;

import ar.uba.fi.cb100.tp2.modelo.efectos.LavaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;

public class BotasIgnifugas extends Objeto {

    public static final String ID = "BOTAS_IGNIFUGAS";
    private static final int DURACION = 20;

    public BotasIgnifugas() {
        super(ID);
    }

    @Override
    public void usar(Heroe heroe, Agenda agenda) {
        heroe.agregarEfecto(new LavaComoPiso(DURACION));
    }
}
