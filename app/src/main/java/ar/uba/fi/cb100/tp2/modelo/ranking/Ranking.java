package ar.uba.fi.cb100.tp2.modelo.ranking;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.TreeSet;
import java.time.LocalDateTime;

import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;
import ar.uba.fi.cb100.material.ValidacionesUtiles;

/**
 * El ranking de puntajes, con un árbol binario de búsqueda por dificultad.
 *
 * Cada árbol es un TreeSet ordenado según EntradaDeRanking, así que
 * recorrerlo en orden da las partidas de la mejor a la peor, y el top 10 son
 * los primeros diez del recorrido. Por dentro el TreeSet es un árbol
 * rojo-negro: se mantiene balanceado aunque los puntajes lleguen ordenados,
 * que es justo el caso en que un ABB común se degenera en una lista.
 *
 * Guarda todas las partidas, no sólo las diez mejores de cada dificultad.
 */
public class Ranking {

    /** Cuántas partidas muestra el top de cada dificultad. */
    public static final int TAMANIO_DEL_TOP = 10;

    private final EnumMap<Dificultad, TreeSet<EntradaDeRanking>> arbolesPorDificultad;

    /**
     * Crea un ranking vacío, con un árbol por dificultad.
     */
    public Ranking() {
        this.arbolesPorDificultad = new EnumMap<>(Dificultad.class);
        for (Dificultad dificultad : Dificultad.values()) {
            this.arbolesPorDificultad.put(dificultad, new TreeSet<>());
        }
    }

    /**
     * Agrega una partida terminada al árbol de su dificultad. Si ya estaba
     * exactamente la misma entrada (mismo nombre, puntaje, dificultad y fecha),
     * queda una sola: es la misma partida.
     *
     * @param entrada la partida a agregar
     * @throws IllegalArgumentException si la entrada es nula
     */
    public void registrar(EntradaDeRanking entrada) {
        ValidacionesUtiles.validarDistintoDeNull(entrada, "entrada");
        this.arbolesPorDificultad.get(entrada.dificultad()).add(entrada);
    }

    /** Registra una partida terminada con un puntaje mínimo de cero. */
    public void registrarPartida(
            String nombre, int puntaje, Dificultad dificultad, LocalDateTime fecha) {
        registrar(new EntradaDeRanking(nombre, Math.max(0, puntaje), dificultad, fecha));
    }

    /**
     * Las mejores partidas de una dificultad, de la mejor a la peor: recorre el
     * árbol en orden y corta al llegar a TAMANIO_DEL_TOP.
     *
     * @param dificultad la dificultad pedida
     * @return como mucho diez partidas, todas de esa dificultad; vacía si nadie
     *         jugó en ella. Es una copia: cambiarla no cambia el ranking
     * @throws IllegalArgumentException si la dificultad es nula
     */
    public List<EntradaDeRanking> obtenerTop10(Dificultad dificultad) {
        ValidacionesUtiles.validarDistintoDeNull(dificultad, "dificultad");
        List<EntradaDeRanking> top = new ArrayList<>();
        for (EntradaDeRanking entrada : this.arbolesPorDificultad.get(dificultad)) {
            if (top.size() == TAMANIO_DEL_TOP) {
                break;
            }
            top.add(entrada);
        }
        return top;
    }

    /**
     * Todas las partidas registradas, para poder guardarlas: primero las de
     * Fácil, después las de Medio y después las de Difícil, cada grupo en el
     * orden del ranking.
     *
     * @return todas las partidas; es una copia, cambiarla no cambia el ranking
     */
    public List<EntradaDeRanking> obtenerTodas() {
        List<EntradaDeRanking> todas = new ArrayList<>();
        for (TreeSet<EntradaDeRanking> arbol : this.arbolesPorDificultad.values()) {
            todas.addAll(arbol);
        }
        return todas;
    }

    /**
     * @return cuántas partidas tiene el ranking, sumando todas las dificultades
     */
    public int cantidadDePartidas() {
        int cantidad = 0;
        for (TreeSet<EntradaDeRanking> arbol : this.arbolesPorDificultad.values()) {
            cantidad += arbol.size();
        }
        return cantidad;
    }
}
