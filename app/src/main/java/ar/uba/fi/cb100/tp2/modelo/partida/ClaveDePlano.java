package ar.uba.fi.cb100.tp2.modelo.partida;

/**
 * Identifica un plano de la mazmorra: su tablero y su piso (base 1).
 * Es la clave de la tabla de celdas visitadas: como es un record, trae
 * equals y hashCode por sus dos campos.
 */
public record ClaveDePlano(int tablero, int piso) {

    /**
     * @throws IllegalArgumentException si el tablero o el piso son menores a 1
     */
    public ClaveDePlano {
        if (tablero < 1 || piso < 1) {
            throw new IllegalArgumentException("Tablero y piso empiezan en 1: " + tablero + ", " + piso);
        }
    }
}