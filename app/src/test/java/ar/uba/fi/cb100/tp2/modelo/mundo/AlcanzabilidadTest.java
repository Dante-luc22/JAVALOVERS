package ar.uba.fi.cb100.tp2.modelo.mundo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.material.i11_grafos.Grafo;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de la alcanzabilidad de la salida a través de las conexiones, con los
 * tres mundos del juego: un plano (Fácil), un tablero de tres pisos (Medio) y
 * tres tableros de tres pisos (Difícil).
 *
 * Todos los planos son un 6x6 con el interior de piso. La salida está siempre
 * en el último plano, como lo arma el CreadorPartida.
 */
class AlcanzabilidadTest {

    /** Cuántas conexiones se hicieron: cada una usa su propia celda en sus dos puntas. */
    private int conexionesHechas = 0;

    // =================== AYUDAS =================== //

    /** Un mundo con la cantidad de planos dada y la salida en el último. */
    private static Mazmorra mundo(int cantidadDePlanos, int pisosPorTablero) {
        List<Plano> planos = new ArrayList<>();
        for (int i = 0; i < cantidadDePlanos; i++) {
            Tablero tablero = new Tablero(6, 6);
            for (int fila = 2; fila <= 5; fila++) {
                for (int columna = 2; columna <= 5; columna++) {
                    tablero.setTerreno(new Posicion(fila, columna), Terreno.PISO);
                }
            }
            Posicion salida = (i == cantidadDePlanos - 1) ? new Posicion(5, 5) : null;
            planos.add(new Plano(tablero, new Posicion(2, 2), salida));
        }
        return new Mazmorra(planos, pisosPorTablero);
    }

    /** Une dos planos con una escalera o pasillo en una celda libre de los dos. */
    private void unir(Mazmorra mazmorra, int tableroA, int pisoA, int tableroB, int pisoB) {
        this.conexionesHechas++;
        Posicion celda = new Posicion(2 + this.conexionesHechas / 4, 2 + this.conexionesHechas % 4);
        mazmorra.conectar(new Ubicacion(tableroA, pisoA, celda), new Ubicacion(tableroB, pisoB, celda));
    }

    /** Las escaleras 1-2 y 2-3 de un tablero. */
    private void unirPisos(Mazmorra mazmorra, int tablero) {
        unir(mazmorra, tablero, 1, tablero, 2);
        unir(mazmorra, tablero, 2, tablero, 3);
    }

    // =================== FÁCIL =================== //

    @Test
    void enFacilLaSalidaEstaEnElMismoPlanoQueLaEntrada() {
        Mazmorra facil = mundo(1, 1);

        assertTrue(Alcanzabilidad.salidaEsAlcanzable(facil));
    }

    // =================== MEDIO =================== //

    @Test
    void enMedioLaSalidaSeAlcanzaPorLasEscaleras() {
        Mazmorra medio = mundo(3, 3);
        unirPisos(medio, 1);

        assertTrue(Alcanzabilidad.salidaEsAlcanzable(medio));
    }

    @Test
    void enMedioSinLasEscalerasLaSalidaNoSeAlcanza() {
        Mazmorra medio = mundo(3, 3);

        assertFalse(Alcanzabilidad.salidaEsAlcanzable(medio));
    }

    @Test
    void enMedioConUnaSolaEscaleraLaSalidaNoSeAlcanza() {
        Mazmorra medio = mundo(3, 3);
        unir(medio, 1, 1, 1, 2);

        assertFalse(Alcanzabilidad.salidaEsAlcanzable(medio));
    }

    @Test
    void enMedioSiFaltaLaEscaleraDelPrimerPisoNoAlcanzaConTenerLaOtra() {
        Mazmorra medio = mundo(3, 3);
        unir(medio, 1, 2, 1, 3);

        assertFalse(Alcanzabilidad.salidaEsAlcanzable(medio));
    }

    // =================== DIFÍCIL =================== //

    @Test
    void enDificilLaSalidaSeAlcanzaPorEscalerasYPasillos() {
        Mazmorra dificil = mundo(9, 3);
        unirPisos(dificil, 1);
        unirPisos(dificil, 2);
        unirPisos(dificil, 3);
        unir(dificil, 1, 3, 2, 1);      // pasillo entre el tablero 1 y el 2
        unir(dificil, 2, 3, 3, 1);      // pasillo entre el tablero 2 y el 3

        assertTrue(Alcanzabilidad.salidaEsAlcanzable(dificil));
    }

    @Test
    void enDificilSinLosPasillosLaSalidaNoSeAlcanza() {
        Mazmorra dificil = mundo(9, 3);
        unirPisos(dificil, 1);
        unirPisos(dificil, 2);
        unirPisos(dificil, 3);

        assertFalse(Alcanzabilidad.salidaEsAlcanzable(dificil));
    }

    @Test
    void enDificilUnTableroAisladoDejaLaSalidaSinAlcanzar() {
        Mazmorra dificil = mundo(9, 3);
        unirPisos(dificil, 1);
        unirPisos(dificil, 2);
        unirPisos(dificil, 3);
        unir(dificil, 1, 3, 2, 1);      // el tablero 3 queda sin pasillo

        assertFalse(Alcanzabilidad.salidaEsAlcanzable(dificil));
    }

    @Test
    void enDificilSeAlcanzaPorUnCaminoAunqueOtrasConexionesFormenCiclos() {
        Mazmorra dificil = mundo(9, 3);
        unirPisos(dificil, 1);
        unirPisos(dificil, 2);
        unirPisos(dificil, 3);
        unir(dificil, 1, 3, 2, 1);
        unir(dificil, 2, 3, 3, 1);
        unir(dificil, 1, 1, 3, 3);      // un tercer pasillo cierra un ciclo entre los tableros

        assertTrue(Alcanzabilidad.salidaEsAlcanzable(dificil));
    }

    @Test
    void lasConexionesSeRecorrenEnLosDosSentidos() {
        Mazmorra medio = mundo(3, 3);
        // se declaran "al revés": de arriba hacia abajo
        unir(medio, 1, 3, 1, 2);
        unir(medio, 1, 2, 1, 1);

        assertTrue(Alcanzabilidad.salidaEsAlcanzable(medio));
    }

    // =================== EL GRAFO DE LOS PLANOS =================== //

    @Test
    void elGrafoDeDificilTieneUnVerticePorPlano() {
        Mazmorra dificil = mundo(9, 3);

        Grafo grafo = Alcanzabilidad.grafoDeLosPlanos(dificil);

        assertEquals(9, grafo.cantidadDeVertices());
        assertEquals(0, grafo.cantidadDeAristas());
        assertFalse(grafo.esDirigido());
    }

    @Test
    void elGrafoTieneUnaAristaPorConexion() {
        Mazmorra dificil = mundo(9, 3);
        unirPisos(dificil, 1);
        unir(dificil, 1, 3, 2, 1);

        Grafo grafo = Alcanzabilidad.grafoDeLosPlanos(dificil);

        assertEquals(3, grafo.cantidadDeAristas());
        assertTrue(grafo.existeArista(0, 1));       // tablero 1, piso 1 con piso 2
        assertTrue(grafo.existeArista(1, 2));       // tablero 1, piso 2 con piso 3
        assertTrue(grafo.existeArista(2, 3));       // el pasillo: tablero 1, piso 3 con tablero 2, piso 1
        assertTrue(grafo.existeArista(3, 2));       // y al revés
        assertFalse(grafo.existeArista(0, 8));
    }

    @Test
    void sinSalidaNoSePuedeVerificar() {
        List<Plano> planos = new ArrayList<>();
        Tablero tablero = new Tablero(6, 6);
        tablero.setTerreno(new Posicion(2, 2), Terreno.PISO);
        planos.add(new Plano(tablero, new Posicion(2, 2), null));
        Mazmorra sinSalida = new Mazmorra(planos, 1);

        assertThrows(IllegalStateException.class, () -> Alcanzabilidad.salidaEsAlcanzable(sinSalida));
    }
}
