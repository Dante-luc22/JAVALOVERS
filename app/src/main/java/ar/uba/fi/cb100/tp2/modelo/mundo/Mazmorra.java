package ar.uba.fi.cb100.tp2.modelo.mundo;

import java.util.ArrayList;
import java.util.List;

import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Todos los planos del juego. En Fácil es uno solo, en Medio un tablero de
 * tres pisos y en Difícil tres tableros de tres pisos.
 * Los planos se guardan en una sola lista: primero los pisos del tablero 0,
 * después los del tablero 1, y así. Tablero y piso empiezan en 0.
 */
public class Mazmorra {

    private final List<Plano> planos;
    private final int pisosPorTablero;

    /**
     * @param planos          todos los planos, ordenados por tablero y después por piso
     * @param pisosPorTablero cuántos pisos tiene cada tablero
     * @throws IllegalArgumentException si no hay planos, si los pisos por tablero no
     *                                  son positivos o si los planos no completan
     *                                  una cantidad exacta de tableros
     */
    public Mazmorra(List<Plano> planos, int pisosPorTablero) {
        if (planos.isEmpty()) {
            throw new IllegalArgumentException("La mazmorra necesita al menos un plano");
        }
        if (pisosPorTablero < 1) {
            throw new IllegalArgumentException("Los pisos por tablero tienen que ser positivos");
        }
        if (planos.size() % pisosPorTablero != 0) {
            throw new IllegalArgumentException(
                    "Con " + pisosPorTablero + " pisos por tablero no entran " + planos.size() + " planos");
        }
        this.planos = new ArrayList<>(planos);
        this.pisosPorTablero = pisosPorTablero;
    }

    /**
     * @param ubicacion tablero y piso (desde 0) del plano buscado
     * @return el plano que está en esa ubicación
     * @throws IllegalArgumentException si el tablero o el piso no existen
     */
    public Plano getPlano(Ubicacion ubicacion) {
        int tablero = ubicacion.tablero();
        int piso = ubicacion.piso();
        if (tablero < 0 || piso < 0 || piso >= this.pisosPorTablero
                || tablero * this.pisosPorTablero + piso >= this.planos.size()) {
            throw new IllegalArgumentException("No existe el plano de: " + ubicacion);
        }
        return this.planos.get(tablero * this.pisosPorTablero + piso);
    }

    /**
     * El héroe siempre empieza en el primer plano: tablero 0, piso 0.
     * @return dónde aparece el héroe al empezar
     */
    public Ubicacion getEntrada() {
        return new Ubicacion(0, 0, this.planos.getFirst().getEntrada());
    }

    /**
     * @return dónde está la salida, con el tablero y el piso del plano que la tiene
     * @throws IllegalStateException si ningún plano tiene salida
     */
    public Ubicacion getSalida() {
        for (int i = 0; i < this.planos.size(); i++) {
            Plano plano = this.planos.get(i);
            if (plano.getSalida() != null) {
                return new Ubicacion(i / this.pisosPorTablero, i % this.pisosPorTablero, plano.getSalida());
            }
        }
        throw new IllegalStateException("La mazmorra no tiene salida");
    }
}
