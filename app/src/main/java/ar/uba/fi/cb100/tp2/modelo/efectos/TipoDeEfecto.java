package ar.uba.fi.cb100.tp2.modelo.efectos;

/**
 * Los tipos de efecto del juego. Permiten quitar un efecto por su tipo
 * cuando vence, y guardar y retomar los efectos activos sin depender
 * de un objeto en particular.
 */
public enum TipoDeEfecto {
    AGUA_COMO_PISO,
    LAVA_COMO_PISO,
    ENVENENADO
}
