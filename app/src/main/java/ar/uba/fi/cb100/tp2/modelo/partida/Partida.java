package ar.uba.fi.cb100.tp2.modelo.partida;

/**
 * Una partida en curso: lleva la cuenta de los pasos y de los cofres abiertos,
 * y con eso calcula el puntaje (regla 9 del enunciado).
 * Por ahora no sabe nada del héroe ni de la mazmorra: se completa a medida
 * que esas clases existan.
 */
public class Partida {

    private static final int PUNTOS_POR_COFRE = 100;
    private static final int PUNTOS_POR_ENERGIA = 2;

    private int pasos;
    private int cofresAbiertos;

    /** Crea una partida recién empezada: cero pasos y cero cofres abiertos. */
    public Partida() {
        this.pasos = 0;
        this.cofresAbiertos = 0;
    }

    /** @return cuántos pasos se dieron; las acciones inválidas no cuentan */
    public int getPasos() {
        return this.pasos;
    }

    /** @return cuántos cofres abrió el héroe */
    public int getCofresAbiertos() {
        return this.cofresAbiertos;
    }

    /** Suma un paso. Se llama una vez por cada acción válida del héroe. */
    public void registrarPaso() {
        this.pasos++;
    }

    /** Suma un cofre abierto. */
    public void registrarCofreAbierto() {
        this.cofresAbiertos++;
    }

    /**
     * Puntaje según la regla 9: cofres abiertos × 100 + energía restante × 2 - pasos.
     * Es estática porque no usa el estado de ninguna partida: solo hace la cuenta
     * con los números que recibe, y así se puede probar sola.
     *
     * @param cofresAbiertos cuántos cofres se abrieron
     * @param energiaRestante la energía que le queda al héroe
     * @param pasos cuántos pasos se dieron
     * @return el puntaje, que puede ser negativo si se dieron muchos pasos
     */
    public static int calcularPuntaje(int cofresAbiertos, int energiaRestante, int pasos) {
        return cofresAbiertos * PUNTOS_POR_COFRE + energiaRestante * PUNTOS_POR_ENERGIA - pasos;
    }
}
