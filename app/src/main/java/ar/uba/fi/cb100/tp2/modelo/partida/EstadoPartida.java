package ar.uba.fi.cb100.tp2.modelo.partida;

/**
*  Indica el estado en el cual se encuentra la partida.
*  La misma puede estar "En curso", "Ganada" o "Perdida".
*  No se incluye la regla de pausar una partida.
* */

public enum EstadoPartida {
    EN_CURSO,
    GANADA,
    PERDIDA
}
