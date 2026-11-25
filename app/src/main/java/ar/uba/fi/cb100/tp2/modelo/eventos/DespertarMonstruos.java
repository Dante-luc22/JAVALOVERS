package ar.uba.fi.cb100.tp2.modelo.eventos;


import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;

public class DespertarMonstruos implements Evento {

    /**
     * Despierta a los monstruos del plano donde está el heroe.
     *
     * @param partida la partida en curso, para llegar al héroe, al plano y a los monstruos
     */
    @Override
    public void ejecutar(Partida partida) {
        for (Entidad e : partida.getPlanoActual().getEntidades()) {
            e.despertar();
        }
    }
}
