package ar.uba.fi.cb100.tp2.modelo.mundo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Caminos de costo mínimo dentro de un tablero, con el costo de cada terreno.
 *
 * El tablero es un grafo con pesos: cada celda transitable es un vértice y
 * moverse a una celda vecina (arriba, abajo, izquierda o derecha) es una arista
 * cuyo costo es el del terreno al que se entra, igual que cuando el héroe paga
 * la energía en la partida. La pared no es un vértice.
 *
 * A diferencia del BFS, que cuenta pasos, Dijkstra cuenta costo: un camino con
 * menos pasos puede costar más si cruza agua o lava, y entonces conviene el
 * rodeo. Usa una cola de prioridad con el próximo vértice de menor costo.
 */
public final class Dijkstra {

    private Dijkstra() {
    }

    /** Un vértice pendiente de la cola de prioridad, con el costo con el que se lo alcanzó. */
    private record Pendiente(Posicion posicion, int costo) {
    }

    /**
     * Cuánto cuesta ir desde cada celda hasta el destino por el camino más barato.
     * Es la misma forma de respuesta que el BFS del cazador (un mapa de celda a
     * número), así que se puede usar en su lugar.
     *
     * El costo de una celda es la suma de los costos de las celdas a las que se
     * entra en el camino; la celda donde se empieza no se paga, y la del destino
     * sí. El destino mismo cuesta 0.
     *
     * @param tablero el tablero a recorrer
     * @param costos  el costo de entrar a PISO, AGUA y LAVA
     * @param destino la celda a la que se quiere llegar
     * @return un mapa celda → costo mínimo hasta el destino; las celdas desde
     *         las que no se puede llegar y las paredes no aparecen
     * @throws IllegalArgumentException si algún argumento es nulo, si falta el
     *                                  costo de algún terreno transitable o no es
     *                                  positivo, o si el destino no es transitable
     */
    public static Map<Posicion, Integer> costosHacia(
            Tablero tablero, Map<Terreno, Integer> costos, Posicion destino) {
        validar(tablero, costos, destino);

        Map<Posicion, Integer> mejores = new HashMap<>();
        PriorityQueue<Pendiente> cola = new PriorityQueue<>(
                (a, b) -> Integer.compare(a.costo(), b.costo()));
        mejores.put(destino, 0);
        cola.add(new Pendiente(destino, 0));

        while (!cola.isEmpty()) {
            Pendiente actual = cola.poll();
            if (actual.costo() > mejores.get(actual.posicion())) {
                // ya se había llegado a esta celda por un camino más barato
                continue;
            }
            // para una vecina, ir hasta el destino pasando por la actual cuesta
            // entrar a la actual más lo que cuesta seguir desde la actual
            int costoDeEntrarAqui = costos.get(tablero.getTerreno(actual.posicion()));
            for (Posicion vecina : tablero.vecinasTransitables(actual.posicion())) {
                int costoPorAqui = actual.costo() + costoDeEntrarAqui;
                Integer conocido = mejores.get(vecina);
                if (conocido == null || costoPorAqui < conocido) {
                    mejores.put(vecina, costoPorAqui);
                    cola.add(new Pendiente(vecina, costoPorAqui));
                }
            }
        }
        return mejores;
    }

    /**
     * El camino de costo mínimo entre dos celdas. Si hay varios igual de
     * baratos, elige siempre el mismo: sigue el orden de {@code Direccion}.
     *
     * @param tablero el tablero a recorrer
     * @param costos  el costo de entrar a PISO, AGUA y LAVA
     * @param origen  la celda desde la que se empieza
     * @param destino la celda a la que se quiere llegar
     * @return las celdas del camino, desde el origen hasta el destino, ambos
     *         incluidos; vacía si no hay camino. Si origen y destino son la
     *         misma celda, tiene solo esa
     * @throws IllegalArgumentException por las mismas razones que
     *                                  {@link #costosHacia}, o si el origen es nulo
     */
    public static List<Posicion> caminoMinimo(
            Tablero tablero, Map<Terreno, Integer> costos, Posicion origen, Posicion destino) {
        if (origen == null) {
            throw new IllegalArgumentException("El origen no puede ser nulo.");
        }
        Map<Posicion, Integer> hastaElDestino = costosHacia(tablero, costos, destino);
        List<Posicion> camino = new ArrayList<>();
        if (!hastaElDestino.containsKey(origen)) {
            return camino;
        }

        Posicion actual = origen;
        camino.add(actual);
        while (!actual.equals(destino)) {
            Posicion siguiente = null;
            int mejor = Integer.MAX_VALUE;
            for (Posicion vecina : tablero.vecinasTransitables(actual)) {
                Integer restante = hastaElDestino.get(vecina);
                if (restante == null) {
                    continue;
                }
                int total = costos.get(tablero.getTerreno(vecina)) + restante;
                if (total < mejor) {
                    mejor = total;
                    siguiente = vecina;
                }
            }
            camino.add(siguiente);
            actual = siguiente;
        }
        return camino;
    }

    /**
     * El costo total de un camino mínimo: lo que se paga al entrar a cada celda
     * del camino menos la primera.
     *
     * @param tablero el tablero a recorrer
     * @param costos  el costo de entrar a PISO, AGUA y LAVA
     * @param origen  la celda desde la que se empieza
     * @param destino la celda a la que se quiere llegar
     * @return el costo mínimo, o -1 si no se puede llegar
     * @throws IllegalArgumentException por las mismas razones que {@link #caminoMinimo}
     */
    public static int costoMinimo(
            Tablero tablero, Map<Terreno, Integer> costos, Posicion origen, Posicion destino) {
        if (origen == null) {
            throw new IllegalArgumentException("El origen no puede ser nulo.");
        }
        Integer costo = costosHacia(tablero, costos, destino).get(origen);
        return costo == null ? -1 : costo;
    }

    /**
     * Verifica los argumentos comunes.
     */
    private static void validar(Tablero tablero, Map<Terreno, Integer> costos, Posicion destino) {
        if (tablero == null || costos == null || destino == null) {
            throw new IllegalArgumentException("El tablero, los costos y el destino no pueden ser nulos.");
        }
        for (Terreno terreno : new Terreno[]{Terreno.PISO, Terreno.AGUA, Terreno.LAVA}) {
            Integer costo = costos.get(terreno);
            if (costo == null) {
                throw new IllegalArgumentException("Falta el costo del terreno " + terreno + ".");
            }
            if (costo < 1) {
                // con costo 0 habría celdas "gratis" y el camino podría dar vueltas sin avanzar
                throw new IllegalArgumentException(
                        "El costo de " + terreno + " tiene que ser positivo, se recibió: " + costo);
            }
        }
        if (!tablero.esTransitable(destino)) {
            throw new IllegalArgumentException("El destino no es una celda transitable: " + destino);
        }
    }
}
