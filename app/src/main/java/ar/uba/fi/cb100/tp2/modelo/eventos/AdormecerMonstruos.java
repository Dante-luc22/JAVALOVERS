package ar.uba.fi.cb100.tp2.modelo.eventos;

import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;

/**
 * Duerme a los monstruos del plano donde se abrió el somnífero, aunque el
 * héroe ya esté en otro piso cuando el evento se ejecuta.
 *
 * @param tablero el tablero del plano afectado, desde 1
 * @param piso    el piso del plano afectado, desde 1
 */
public record AdormecerMonstruos(int tablero, int piso) implements Evento {

    public AdormecerMonstruos {
        if (tablero < 1 || piso < 1) {
            throw new IllegalArgumentException("Tablero y piso empiezan en 1");
        }
    }

    @Override
    public void ejecutar(Partida partida) {
        for (Entidad entidad : partida.getMazmorra().getPlano(this.tablero, this.piso).getEntidades()) {
            entidad.adormecer();
        }
    }
}