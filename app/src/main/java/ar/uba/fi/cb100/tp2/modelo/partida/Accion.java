package ar.uba.fi.cb100.tp2.modelo.partida;

/**
 * Lo que el jugador puede pedir en un paso. Cada tecla de la consola
 * (w a s d u e g q) se traduce a una de estas acciones, y la Partida
 * no tiene que saber nada de teclas.
 */
public enum Accion {
    MOVER_ARRIBA,
    MOVER_ABAJO,
    MOVER_IZQUIERDA,
    MOVER_DERECHA,
    USAR,
    ESPERAR,
    GUARDAR,
    SALIR
}
