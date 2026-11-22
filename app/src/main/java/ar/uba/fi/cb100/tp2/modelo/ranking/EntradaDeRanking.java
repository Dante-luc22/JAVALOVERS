package ar.uba.fi.cb100.tp2.modelo.ranking;

import java.time.LocalDateTime;

import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

/**
 * Una partida terminada, tal como entra al ranking: quién la jugó, cuántos
 * puntos sacó, en qué dificultad y cuándo terminó. Es uno de los records que
 * pide el enunciado.
 *
 * Su orden natural es el del ranking: primero el puntaje más alto y, si dos
 * empatan, primero la que se logró antes. El nombre y la dificultad desempatan
 * al final sólo para que dos entradas distintas nunca comparen como iguales:
 * el ranking guarda las entradas en un TreeSet, y un TreeSet descarta la
 * segunda de dos entradas que comparan 0, así que se perdería una partida.
 * Con los cuatro campos, compareTo da 0 sólo cuando equals da true.
 *
 * @param nombre     quién jugó la partida
 * @param puntaje    los puntos que sacó; nunca son negativos, el puntaje tiene piso 0
 * @param dificultad la dificultad en la que jugó
 * @param fecha      cuándo terminó la partida
 */
public record EntradaDeRanking(String nombre, int puntaje, Dificultad dificultad, LocalDateTime fecha)
        implements Comparable<EntradaDeRanking> {

    /**
     * Verifica que la entrada esté completa.
     *
     * @throws IllegalArgumentException si el nombre es nulo o vacío, si el
     *                                  puntaje es negativo, o si la dificultad
     *                                  o la fecha son nulas
     */
    public EntradaDeRanking {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo ni vacío.");
        }
        if (puntaje < 0) {
            throw new IllegalArgumentException(
                    "El puntaje no puede ser negativo, se recibió: " + puntaje);
        }
        if (dificultad == null) {
            throw new IllegalArgumentException("La dificultad no puede ser nula.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula.");
        }
    }
    
    /**
     * Compara dos entradas según su lugar en el ranking.
     *
     * @param otra la entrada con la que se compara
     * @return negativo si esta entrada va antes que la otra en el ranking,
     *         positivo si va después, y 0 sólo si son la misma entrada
     */
    @Override
    public int compareTo(EntradaDeRanking otra) {
        if (this.puntaje != otra.puntaje) {
            // al revés que lo usual: el puntaje más alto va primero
            return Integer.compare(otra.puntaje, this.puntaje);
        }
        int porFecha = this.fecha.compareTo(otra.fecha);
        if (porFecha != 0) {
            // con el mismo puntaje, el que lo logró antes va primero
            return porFecha;
        }
        int porNombre = this.nombre.compareTo(otra.nombre);
        if (porNombre != 0) {
            return porNombre;
        }
        return this.dificultad.compareTo(otra.dificultad);
    }
}
