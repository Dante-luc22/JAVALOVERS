package ar.uba.fi.cb100.tp2.modelo.eventos;

import ar.uba.fi.cb100.tp2.modelo.monstruos.Monstruo;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;


/**
 * Clase Repeler Monstruos, sirve para indicarle al evento que
 * si una entidad es un monstruo, lo envie al inicio con el
 * metodo repeler()
 */
public class RepelerMonstruos implements Evento {

    /**
     * Manda los monstruos del plano donde esta el heroe
     * a su posicion inicial (los cofres no se tocan).
     * @param partida la partida en curso, para llegar al héroe, al plano y a los monstruos
     */
    @Override
    public void ejecutar(Partida partida) {
        for (Entidad e : partida.getPlanoActual().getEntidades()) {
            if (e instanceof Monstruo monstruo) {
                monstruo.repeler();
            }
        }
    }
}
