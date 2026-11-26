package ar.uba.fi.cb100.material.i11_grafos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.material.i11_grafos.Grafo.Arista;

/**
 * Tests del grafo con lista de adyacencia de la cátedra, que el juego usa para
 * representar cada plano (celda transitable = vértice, movimiento = arista con
 * costo).
 */
class GrafoTest {

    // =================== CREACIÓN =================== //

    @Test
    void unGrafoNuevoTieneLosVerticesYNingunaArista() {
        Grafo grafo = new Grafo(4, false);

        assertEquals(4, grafo.cantidadDeVertices());
        assertEquals(0, grafo.cantidadDeAristas());
        assertFalse(grafo.esDirigido());
        for (int v = 0; v < 4; v++) {
            assertEquals(0, grafo.grado(v));
            assertTrue(grafo.vecinos(v).isEmpty());
        }
    }

    @Test
    void sePuedeCrearUnGrafoSinVertices() {
        assertEquals(0, new Grafo(0, true).cantidadDeVertices());
    }

    @Test
    void noSePuedeCrearConCantidadNegativaDeVertices() {
        assertThrows(IllegalArgumentException.class, () -> new Grafo(-1, false));
    }

    // =================== NO DIRIGIDO =================== //

    @Test
    void enUnGrafoNoDirigidoLaAristaSeGuardaEnAmbosSentidos() {
        Grafo grafo = new Grafo(3, false);
        grafo.agregarArista(0, 1, 5);

        assertTrue(grafo.existeArista(0, 1));
        assertTrue(grafo.existeArista(1, 0));
        assertEquals(List.of(new Arista(1, 5)), grafo.vecinos(0));
        assertEquals(List.of(new Arista(0, 5)), grafo.vecinos(1));
    }

    @Test
    void laAristaNoDirigidaCuentaUnaSolaVez() {
        Grafo grafo = new Grafo(3, false);
        grafo.agregarArista(0, 1);
        grafo.agregarArista(1, 2);

        assertEquals(2, grafo.cantidadDeAristas());
    }

    @Test
    void elGradoEsLaCantidadDeVecinos() {
        Grafo grafo = new Grafo(4, false);
        grafo.agregarArista(0, 1);
        grafo.agregarArista(0, 2);
        grafo.agregarArista(0, 3);

        assertEquals(3, grafo.grado(0));
        assertEquals(1, grafo.grado(1));
    }

    // =================== DIRIGIDO =================== //

    @Test
    void enUnGrafoDirigidoLaAristaVaEnUnSoloSentido() {
        Grafo grafo = new Grafo(3, true);
        grafo.agregarArista(0, 1, 2);

        assertTrue(grafo.esDirigido());
        assertTrue(grafo.existeArista(0, 1));
        assertFalse(grafo.existeArista(1, 0));
        assertTrue(grafo.vecinos(1).isEmpty());
        assertEquals(1, grafo.cantidadDeAristas());
    }

    // =================== PESOS =================== //

    @Test
    void laAristaSinPesoPesaUno() {
        Grafo grafo = new Grafo(2, true);
        grafo.agregarArista(0, 1);

        assertEquals(1, grafo.vecinos(0).get(0).peso());
    }

    @Test
    void losVecinosSalenEnElOrdenEnQueSeAgregaron() {
        Grafo grafo = new Grafo(4, true);
        grafo.agregarArista(0, 3, 7);
        grafo.agregarArista(0, 1, 2);
        grafo.agregarArista(0, 2, 9);

        assertEquals(List.of(new Arista(3, 7), new Arista(1, 2), new Arista(2, 9)), grafo.vecinos(0));
    }

    @Test
    void sePuedeAgregarUnLazoYUnaAristaParalela() {
        Grafo grafo = new Grafo(2, true);
        grafo.agregarArista(0, 0, 1);
        grafo.agregarArista(0, 1, 4);
        grafo.agregarArista(0, 1, 2);

        assertEquals(3, grafo.grado(0));
        assertEquals(3, grafo.cantidadDeAristas());
    }

    // =================== ENCAPSULAMIENTO =================== //

    @Test
    void laListaDeVecinosEsUnaCopia() {
        Grafo grafo = new Grafo(2, true);
        grafo.agregarArista(0, 1);

        List<Arista> vecinos = grafo.vecinos(0);

        assertThrows(UnsupportedOperationException.class, () -> vecinos.add(new Arista(0, 1)));
        assertEquals(1, grafo.grado(0));
    }

    // =================== VÉRTICES INVÁLIDOS =================== //

    @Test
    void unVerticeFueraDeRangoEsInvalido() {
        Grafo grafo = new Grafo(3, false);

        assertThrows(IllegalArgumentException.class, () -> grafo.agregarArista(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> grafo.agregarArista(0, 3));
        assertThrows(IllegalArgumentException.class, () -> grafo.vecinos(3));
        assertThrows(IllegalArgumentException.class, () -> grafo.grado(-1));
        assertThrows(IllegalArgumentException.class, () -> grafo.existeArista(0, 9));
    }

    @Test
    void unaAristaInvalidaNoDejaElGrafoAMedias() {
        Grafo grafo = new Grafo(3, false);

        assertThrows(IllegalArgumentException.class, () -> grafo.agregarArista(0, 5));

        assertEquals(0, grafo.cantidadDeAristas());
        assertEquals(0, grafo.grado(0));
    }
}
