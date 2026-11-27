package ar.uba.fi.cb100.tp2.modelo.mundo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import ar.uba.fi.cb100.material.i11_grafos.Grafo;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.material.ValidacionesUtiles;

/**
 * Caminos de costo mínimo dentro de un tablero, con el costo de cada terreno.
 *
 * El tablero es un grafo con pesos: cada celda transitable es un vértice y
 * moverse a una celda vecina (arriba, abajo, izquierda o derecha) es una arista
 * cuyo costo es el del terreno al que se entra, igual que cuando el héroe paga
 * la energía en la partida. La pared no es un vértice. El grafo es el de la
 * cátedra ({@link Grafo}); acá se lo arma a partir del tablero.
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

        Grafo grafo = armarGrafoInverso(tablero, costos);
        int columnas = tablero.getColumnas();

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
            for (Grafo.Arista arista : grafo.vecinos(vertice(actual.posicion(), columnas))) {
                Posicion vecina = posicion(arista.destino(), columnas);
                int costoPorAqui = actual.costo() + arista.peso();
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
     * Arma el grafo del tablero con las aristas al revés: la que va de la celda
     * actual a una vecina pesa lo que cuesta entrar a la actual. Así, partiendo
     * del destino, se obtiene lo que cuesta ir desde cada celda hasta él.
     * Cada celda es el vértice {@code (fila - 1) * columnas + (columna - 1)}.
     */
    private static Grafo armarGrafoInverso(Tablero tablero, Map<Terreno, Integer> costos) {
        int columnas = tablero.getColumnas();
        Grafo grafo = new Grafo(tablero.getFilas() * columnas, true);
        for (int fila = 1; fila <= tablero.getFilas(); fila++) {
            for (int columna = 1; columna <= columnas; columna++) {
                Posicion celda = new Posicion(fila, columna);
                if (!tablero.esTransitable(celda)) {
                    continue;
                }
                int costoDeEntrarAqui = costos.get(tablero.getTerreno(celda));
                for (Posicion vecina : tablero.vecinasTransitables(celda)) {
                    grafo.agregarArista(vertice(celda, columnas), vertice(vecina, columnas), costoDeEntrarAqui);
                }
            }
        }
        return grafo;
    }

    /** El número de vértice de una celda. */
    private static int vertice(Posicion posicion, int columnas) {
        return (posicion.fila() - 1) * columnas + (posicion.columna() - 1);
    }

    /** La celda que corresponde a un número de vértice. */
    private static Posicion posicion(int vertice, int columnas) {
        return new Posicion(vertice / columnas + 1, vertice % columnas + 1);
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
        ValidacionesUtiles.validarDistintoDeNull(origen, "origen");
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
        ValidacionesUtiles.validarDistintoDeNull(origen, "origen");
        Integer costo = costosHacia(tablero, costos, destino).get(origen);
        return costo == null ? -1 : costo;
    }

    /**
     * Verifica los argumentos comunes.
     */
    private static void validar(Tablero tablero, Map<Terreno, Integer> costos, Posicion destino) {
        ValidacionesUtiles.validarDistintoDeNull(tablero, "tablero");
        ValidacionesUtiles.validarDistintoDeNull(costos, "costos");
        ValidacionesUtiles.validarDistintoDeNull(destino, "destino");
        for (Terreno terreno : new Terreno[]{Terreno.PISO, Terreno.AGUA, Terreno.LAVA}) {
            Integer costo = costos.get(terreno);
            ValidacionesUtiles.validarDistintoDeNull(costo, "costo del terreno", terreno.toString());
            // con costo 0 habría celdas "gratis" y el camino podría dar vueltas sin avanzar
            ValidacionesUtiles.esMayorQueCero(costo, "costo del terreno", terreno.toString());
        }
        ValidacionesUtiles.validarFalso(!tablero.esTransitable(destino),
                "El destino no es una celda transitable: " + destino);
    }
}
