package ar.uba.fi.cb100.tp2.modelo.mundo;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Todos los planos del juego. En Fácil es uno solo, en Medio un tablero de
 * tres pisos y en Difícil tres tableros de tres pisos.
 * Los planos se guardan en una sola lista: primero los pisos del tablero 0,
 * después los del tablero 2, y así. Tablero y piso empiezan en 1, como en el enunciado.
 */
public class Mazmorra {

    private final List<Plano> planos;
    private final int pisosPorTablero;

    /** Para cada celda con escalera o pasillo, adónde lleva. Tiene los dos sentidos. */
    private final Map<Ubicacion, Ubicacion> destinos = new HashMap<>();

    /** Cada conexión una sola vez, en el orden en que se conectó. */
    private final List<Conexion> conexiones = new ArrayList<>();

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
     * @param ubicacion tablero y piso (desde 1) del plano buscado
     * @return el plano que está en esa ubicación
     * @throws IllegalArgumentException si el tablero o el piso no existen
     */
    public Plano getPlano(Ubicacion ubicacion) {
        int tablero = ubicacion.tablero();
        int piso = ubicacion.piso();
        if (tablero < 1 || piso < 1 || piso > this.pisosPorTablero) {
            throw new IllegalArgumentException("No existe el plano de: " + ubicacion);
        }
        int indice = (tablero - 1) * this.pisosPorTablero + (piso - 1);
        if (indice >= this.planos.size()) {
            throw new IllegalArgumentException("No existe el plano de: " + ubicacion);
        }
        return this.planos.get(indice);
    }

    /**
     * El héroe siempre empieza en el primer plano: tablero 1, piso 1.
     * @return dónde aparece el héroe al empezar
     */
    public Ubicacion getEntrada() {
        return new Ubicacion(1, 1, this.planos.getFirst().getEntrada());
    }

    /**
     * @return dónde está la salida, con el tablero y el piso del plano que la tiene
     * @throws IllegalStateException si ningún plano tiene salida
     */
    public Ubicacion getSalida() {
        for (int i = 0; i < this.planos.size(); i++) {
            Plano plano = this.planos.get(i);
            if (plano.getSalida() != null) {
                return new Ubicacion(i / this.pisosPorTablero + 1, i % this.pisosPorTablero + 1, plano.getSalida());
            }
        }
        throw new IllegalStateException("La mazmorra no tiene salida");
    }

        /**
     * Une dos celdas de planos distintos con una escalera o un pasillo, en los
     * dos sentidos: pisar cualquiera de las dos lleva a la otra.
     *
     * @param a una punta
     * @param b la otra punta, en otro plano
     * @throws IllegalArgumentException si alguna punta es nula, si están en el
     *         mismo plano, si el plano no existe, si la celda no es transitable
     *         o si ya tiene una conexión
     */
    public void conectar(Ubicacion a, Ubicacion b) {
        Conexion conexion = new Conexion(a, b);
        validarPunta(a);
        validarPunta(b);
        this.destinos.put(a, b);
        this.destinos.put(b, a);
        this.conexiones.add(conexion);
    }

    /**
     * @param ubicacion la celda donde está el héroe
     * @return adónde lleva la escalera o el pasillo de esa celda, o vacío si no hay
     */
    public Optional<Ubicacion> destinoDesde(Ubicacion ubicacion) {
        return Optional.ofNullable(this.destinos.get(ubicacion));
    }

    /**
     * @param tablero el tablero del plano, desde 1
     * @param piso    el piso del plano, desde 1
     * @return las conexiones que salen de ese plano, para dibujarlas
     */
    public List<Conexion> conexionesDelPlano(int tablero, int piso) {
        List<Conexion> delPlano = new ArrayList<>();
        for (Map.Entry<Ubicacion, Ubicacion> entrada : this.destinos.entrySet()) {
            Ubicacion origen = entrada.getKey();
            if (origen.tablero() == tablero && origen.piso() == piso) {
                delPlano.add(new Conexion(origen, entrada.getValue()));
            }
        }
        return delPlano;
    }

    /**
     * @return cada conexión una sola vez, en el orden en que se conectó. Para
     *         rearmarlas alcanza con llamar a conectar(origen, destino) por cada una.
     */
    public List<Conexion> getConexiones() {
        return new ArrayList<>(this.conexiones);
    }

    private void validarPunta(Ubicacion punta) {
        Plano plano = getPlano(punta);
        if (!plano.getTablero().esTransitable(punta.posicion())) {
            throw new IllegalArgumentException("Una escalera o pasillo no puede estar en una pared: " + punta);
        }
        if (this.destinos.containsKey(punta)) {
            throw new IllegalArgumentException("Esa celda ya tiene una conexión: " + punta);
        }
    }

    /**
     * Devuelve una copia de la lista de planos,
     * conservando el orden original.
     *
     * @return planos ordenados por tablero y piso
     */
    public List<Plano> getPlanos() {
        return new ArrayList<>(this.planos);
    }

    /**
     * Devuelve la cantidad de pisos que tiene cada tablero.
     *
     * @return pisos por tablero
     */
    public int getPisosPorTablero() {
        return this.pisosPorTablero;
    }

}
