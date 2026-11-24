package ar.uba.fi.cb100.tp2.modelo.efectos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Evento;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;

public class QuitarEfecto implements Evento {

    private final Efecto efecto;

    public QuitarEfecto(Efecto efecto) {
        if (efecto == null) {
            throw new IllegalArgumentException("El efecto no puede ser nulo.");
        }
        this.efecto = efecto;
    }

    @Override
    public void ejecutar(Partida partida) {
        partida.getHeroe().quitarEfecto(efecto);
    }
}
