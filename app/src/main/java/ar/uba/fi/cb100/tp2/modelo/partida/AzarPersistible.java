package ar.uba.fi.cb100.tp2.modelo.partida;

import java.util.Random;

/**
 * Generador aleatorio compatible con java.util.Random
 * que permite consultar y restaurar su estado interno.
 *
 * Utiliza el mismo algoritmo LCG de 48 bits que Random.
 * Esto permite continuar la secuencia aleatoria después
 * de guardar y cargar una partida.
 */
public final class AzarPersistible extends Random {

    private static final long MULTIPLICADOR = 0x5DEECE66DL;
    private static final long SUMANDO = 0xBL;
    private static final long MASCARA = (1L << 48) - 1;

    /*
     * Estado interno de 48 bits del generador.
     *
     * No es la semilla original: es el estado que utiliza
     * el algoritmo para producir el próximo número.
     */
    private long estadoInterno;

    /**
     * Crea un generador a partir de una semilla inicial.
     *
     * @param semilla semilla original del generador
     */
    public AzarPersistible(long semilla) {
        super(semilla);
    }

    /**
     * Constructor privado para reconstruir un generador
     * desde un estado interno previamente guardado.
     *
     * @param estadoInterno estado interno de 48 bits
     */
    private AzarPersistible(long estadoInterno, boolean restauracion) {
        super(0L);
        this.estadoInterno = validarEstado(estadoInterno);
    }

    /**
     * Reconstruye un generador desde su estado interno.
     *
     * @param estadoInterno estado previamente guardado
     * @return generador restaurado
     */
    public static AzarPersistible desdeEstado(long estadoInterno) {
        return new AzarPersistible(estadoInterno, true);
    }

    /**
     * Devuelve el estado interno actual del generador.
     *
     * @return estado interno de 48 bits
     */
    public synchronized long getEstadoInterno() {
        return this.estadoInterno;
    }

    /**
     * Restaura el estado interno de un generador existente.
     *
     * @param estadoInterno estado que se desea establecer
     */
    public synchronized void restaurarEstado(long estadoInterno) {
        this.estadoInterno = validarEstado(estadoInterno);
        super.setSeed(0L);
    }

    /**
     * Inicializa el generador utilizando la misma transformación
     * de semilla que java.util.Random.
     *
     * @param semilla semilla original
     */
    @Override
    public synchronized void setSeed(long semilla) {
        super.setSeed(semilla);
        this.estadoInterno = (semilla ^ MULTIPLICADOR) & MASCARA;
    }

    /**
     * Genera los siguientes bits utilizando el algoritmo
     * lineal congruencial de java.util.Random.
     *
     * @param bits cantidad de bits solicitados
     * @return valor generado
     */
    @Override
    protected synchronized int next(int bits) {
        this.estadoInterno =
                (this.estadoInterno * MULTIPLICADOR + SUMANDO) & MASCARA;

        return (int) (this.estadoInterno >>> (48 - bits));
    }

    /**
     * Comprueba que el estado corresponda a un valor
     * válido de 48 bits.
     */
    private static long validarEstado(long estadoInterno) {

        if (estadoInterno < 0 || estadoInterno > MASCARA) {
            throw new IllegalArgumentException(
                    "El estado del generador aleatorio debe estar "
                            + "comprendido entre 0 y " + MASCARA + "."
            );
        }

        return estadoInterno;
    }
}
