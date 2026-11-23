package ar.uba.fi.cb100.tp2.modelo.eventos;

import ar.uba.fi.cb100.tp2.modelo.partida.Partida;

/**
 * Algo que pasa en un paso futuro: un efecto que vence, una trampa que se
 * rearma, un monstruo que despierta. Lo programa quien lo causa (un cofre,
 * un monstruo) y lo ejecuta la Agenda cuando llega su paso.
 *
 * Para poder guardar la partida, un evento guarda datos (posiciones, cantidades,
 * ids), no referencias a objetos del juego: al cargar, los objetos son nuevos.
 */
public interface Evento {

    /**
     * @param partida la partida en curso, para llegar al héroe, al plano y a los monstruos
     */
    void ejecutar(Partida partida);
}
