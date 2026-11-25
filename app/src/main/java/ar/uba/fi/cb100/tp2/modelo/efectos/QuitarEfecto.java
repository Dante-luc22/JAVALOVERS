package ar.uba.fi.cb100.tp2.modelo.efectos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Evento;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;

/**
 * Evento que quita del heroe un efecto de cierto tipo cuando vence.
 * Guarda el tipo y no el objeto, para que siga funcionando despues
 * de guardar y retomar la partida.
 */
public class QuitarEfecto implements Evento {

    private final TipoDeEfecto tipo;

    /**
     * @param tipo de efecto
     * @throws IllegalArgumentException si el tipo es nulo
     */
    public QuitarEfecto(TipoDeEfecto tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de efecto no puede ser nulo.");
        }
        this.tipo = tipo;
    }

    /**
     * @param partida la partida en curso, para llegar al héroe.
     */
    @Override
    public void ejecutar(Partida partida) {
        partida.getHeroe().quitarEfecto(this.tipo);
    }

    /**
     * @return el tipo de efecto.
     */
    public TipoDeEfecto getTipo() {
        return this.tipo;
    }
}
