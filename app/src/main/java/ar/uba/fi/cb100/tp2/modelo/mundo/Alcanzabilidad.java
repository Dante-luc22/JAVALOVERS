package ar.uba.fi.cb100.tp2.modelo.mundo;

import java.util.ArrayDeque;
import java.util.Queue;

import ar.uba.fi.cb100.material.i11_grafos.Grafo;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Verifica que la salida se pueda alcanzar desde la entrada a través de las
 * escaleras y los pasillos.
 *
 * El mundo es un grafo: cada plano es un vértice y cada conexión (escalera o
 * pasillo) es una arista. En Fácil hay un vértice, en Medio tres y en Difícil
 * nueve. Como las conexiones valen en los dos sentidos, el grafo no es
 * dirigido; y en Difícil puede tener ciclos.
 *
 * Se trabaja con planos enteros y no con celdas porque todo plano válido es
 * conexo (regla 5 del validador): si el héroe llega a un plano, llega a
 * cualquiera de sus celdas, y entonces también a la escalera o al pasillo que
 * lo lleva al siguiente.
 */
public final class Alcanzabilidad {

    private Alcanzabilidad() {
    }

    /**
     * Arma el grafo de los planos de la mazmorra. El plano del tablero t y el
     * piso p es el vértice {@code (t - 1) * pisosPorTablero + (p - 1)}, el
     * mismo orden en que la mazmorra guarda los planos.
     *
     * @param mazmorra el mundo a recorrer
     * @return un grafo no dirigido con un vértice por plano y una arista por conexión
     */
    
    public static Grafo grafoDeLosPlanos(Mazmorra mazmorra) {
        int pisos = mazmorra.getPisosPorTablero();
        Grafo grafo = new Grafo(mazmorra.getCantidadDePlanos(), false);
        for (Conexion conexion : mazmorra.getConexiones()) {
            grafo.agregarArista(vertice(conexion.origen(), pisos), vertice(conexion.destino(), pisos));
        }
        return grafo;
    }

    /**
     * Dice si el héroe, empezando en la entrada, puede llegar a la salida
     * cruzando escaleras y pasillos. Hace un BFS sobre el grafo de los planos.
     *
     * @param mazmorra el mundo a verificar
     * @return true si el plano de la salida se alcanza desde el plano de la entrada
     * @throws IllegalStateException si ningún plano tiene salida
     */
    public static boolean salidaEsAlcanzable(Mazmorra mazmorra) {
        int pisos = mazmorra.getPisosPorTablero();
        Grafo grafo = grafoDeLosPlanos(mazmorra);
        int entrada = vertice(mazmorra.getEntrada(), pisos);
        int salida = vertice(mazmorra.getSalida(), pisos);
        return visitadosDesde(grafo, entrada)[salida];
    }

    /**
     * BFS: visita primero los vértices más cercanos al origen, por capas.
     *
     * @return para cada vértice, si se llega a él desde el origen
     */
    private static boolean[] visitadosDesde(Grafo grafo, int origen) {
        boolean[] visitado = new boolean[grafo.cantidadDeVertices()];
        Queue<Integer> cola = new ArrayDeque<>();
        visitado[origen] = true;
        cola.add(origen);
        while (!cola.isEmpty()) {
            int actual = cola.poll();
            for (Grafo.Arista arista : grafo.vecinos(actual)) {
                if (!visitado[arista.destino()]) {
                    visitado[arista.destino()] = true;
                    cola.add(arista.destino());
                }
            }
        }
        return visitado;
    }

    /** El número de vértice del plano donde está una ubicación. */
    private static int vertice(Ubicacion ubicacion, int pisosPorTablero) {
        return (ubicacion.tablero() - 1) * pisosPorTablero + (ubicacion.piso() - 1);
    }
}
