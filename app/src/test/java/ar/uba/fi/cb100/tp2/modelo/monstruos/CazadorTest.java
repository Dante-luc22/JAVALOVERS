package ar.uba.fi.cb100.tp2.modelo.monstruos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Random;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de Cazador: avanza una celda por el camino más corto (BFS) hacia el héroe.
 *
 * Plano de prueba (filas 2 a 4, columnas 2 a 5; el resto es pared):
 * <pre>
 *   . . . .        4 5 4 3
 *   . # # .   →    3 # # 2     (distancias al héroe H)
 *   . . H .        2 1 0 1
 * </pre>
 */
class CazadorTest {

    private static final int DANIO = 30;
    private static final Posicion HEROE = new Posicion(4, 4);

    // =================== AYUDAS =================== //

    /** El plano del dibujo de arriba. */
    private static Plano planoConPared() {
        Tablero tablero = new Tablero(5, 6);
        for (int fila = 2; fila <= 4; fila++) {
            for (int columna = 2; columna <= 5; columna++) {
                tablero.setTerreno(new Posicion(fila, columna), Terreno.PISO);
            }
        }
        tablero.setTerreno(new Posicion(3, 3), Terreno.PARED);
        tablero.setTerreno(new Posicion(3, 4), Terreno.PARED);
        return new Plano(tablero, new Posicion(2, 2), null);
    }

    /** Un plano con las celdas dadas en piso y el resto pared. */
    private static Plano planoConPiso(Posicion... pisos) {
        Tablero tablero = new Tablero(7, 7);
        for (Posicion piso : pisos) {
            tablero.setTerreno(piso, Terreno.PISO);
        }
        return new Plano(tablero, pisos[0], null);
    }

    private static Heroe heroeEn(Posicion posicion) {
        return new Heroe(new Ubicacion(0, 0, posicion), 100);
    }

    // =================== CONSTRUCTOR =================== //

    @Test
    void recienCreadoEstaEnSuPosicionInicialConSuDanio() {
        Cazador cazador = new Cazador(new Posicion(2, 2), DANIO);
        assertEquals(new Posicion(2, 2), cazador.getPosicion());
        assertEquals(new Posicion(2, 2), cazador.getPosicionInicial());
        assertEquals(DANIO, cazador.getDanio());
    }

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class, () -> new Cazador(null, DANIO));
    }

    @Test
    void constructorRechazaDanioNoPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new Cazador(new Posicion(2, 2), 0));
    }

    // =================== CALCULAR DISTANCIAS (BFS) =================== //

    @Test
    void elDestinoEstaADistanciaCero() {
        Cazador cazador = new Cazador(new Posicion(2, 2), DANIO);
        HashMap<Posicion, Integer> distancias = cazador.calcularDistancias(planoConPared(), HEROE);
        assertEquals(0, distancias.get(HEROE));
    }

    @Test
    void calculaLasDistanciasDelDibujo() {
        Cazador cazador = new Cazador(new Posicion(2, 2), DANIO);
        HashMap<Posicion, Integer> distancias = cazador.calcularDistancias(planoConPared(), HEROE);

        int[][] esperadas = {
                {4, 5, 4, 3},
                {3, -1, -1, 2},
                {2, 1, 0, 1},
        };
        for (int fila = 0; fila < 3; fila++) {
            for (int columna = 0; columna < 4; columna++) {
                Posicion celda = new Posicion(fila + 2, columna + 2);
                if (esperadas[fila][columna] == -1) {
                    assertFalse(distancias.containsKey(celda), "Una pared tiene distancia: " + celda);
                } else {
                    assertEquals(esperadas[fila][columna], distancias.get(celda), "Mal la celda " + celda);
                }
            }
        }
        assertEquals(10, distancias.size());
    }

    @Test
    void lasCeldasInalcanzablesNoTienenDistancia() {
        Posicion encerrada = new Posicion(2, 5);
        Plano plano = planoConPiso(new Posicion(2, 2), new Posicion(2, 3), encerrada);
        Cazador cazador = new Cazador(new Posicion(2, 2), DANIO);

        HashMap<Posicion, Integer> distancias = cazador.calcularDistancias(plano, new Posicion(2, 2));

        assertEquals(1, distancias.get(new Posicion(2, 3)));
        assertFalse(distancias.containsKey(encerrada));
    }

    // =================== DECIDIR MOVIMIENTO =================== //

    @Test
    void rodeaLaParedEnVezDeIrDerecho() {
        // Desde (2,4) el héroe está justo abajo, pero abajo hay pared:
        // tiene que ir a la derecha, por (2,5), que está a distancia 3.
        Cazador cazador = new Cazador(new Posicion(2, 4), DANIO);

        cazador.actuar(planoConPared(), heroeEn(HEROE));

        assertEquals(new Posicion(2, 5), cazador.getPosicion());
    }

    @Test
    void cadaPasoLoAcercaUnaCeldaHastaAlcanzarlo() {
        Plano plano = planoConPared();
        Cazador cazador = new Cazador(new Posicion(2, 2), DANIO);
        Heroe heroe = heroeEn(HEROE);

        for (int esperada = 3; esperada >= 0; esperada--) {
            cazador.actuar(plano, heroe);
            int distancia = cazador.calcularDistancias(plano, HEROE).get(cazador.getPosicion());
            assertEquals(esperada, distancia, "No se acerco en el paso " + (4 - esperada));
        }
        assertEquals(HEROE, cazador.getPosicion());
    }

    @Test
    void conElHeroeAlLadoPisaSuCelda() {
        Cazador cazador = new Cazador(new Posicion(4, 5), DANIO);

        cazador.actuar(planoConPared(), heroeEn(HEROE));

        assertEquals(HEROE, cazador.getPosicion());
    }

    @Test
    void siYaEstaEnLaCeldaDelHeroeSeQuedaAhi() {
        Cazador cazador = new Cazador(HEROE, DANIO);

        cazador.actuar(planoConPared(), heroeEn(HEROE));

        assertEquals(HEROE, cazador.getPosicion());
    }

    @Test
    void siElHeroeEsInalcanzableSeQuedaQuieto() {
        Posicion inicio = new Posicion(2, 2);
        Posicion heroeEncerrado = new Posicion(2, 5);
        Plano plano = planoConPiso(inicio, new Posicion(2, 3), heroeEncerrado);
        Cazador cazador = new Cazador(inicio, DANIO);

        cazador.actuar(plano, heroeEn(heroeEncerrado));

        assertEquals(inicio, cazador.getPosicion());
    }

    @Test
    void encerradoSinVecinasSeQuedaQuieto() {
        Posicion solo = new Posicion(3, 3);
        Cazador cazador = new Cazador(solo, DANIO);

        cazador.actuar(planoConPiso(solo, new Posicion(5, 5)), heroeEn(new Posicion(5, 5)));

        assertEquals(solo, cazador.getPosicion());
    }

    @Test
    void nuncaPisaUnaPared() {
        Plano plano = planoConPared();
        Cazador cazador = new Cazador(new Posicion(2, 3), DANIO);
        Heroe heroe = heroeEn(HEROE);

        for (int paso = 0; paso < 10; paso++) {
            cazador.actuar(plano, heroe);
            assertTrue(plano.getTablero().esTransitable(cazador.getPosicion()),
                    "Piso una pared en el paso " + paso);
        }
    }

    @Test
    void siElHeroeSeMueveLoPersigueASuNuevaPosicion() {
        // Si las distancias quedaran guardadas de un paso al otro, el cazador
        // seguiría yendo adonde estaba el héroe antes.
        Plano plano = planoConPared();
        Cazador cazador = new Cazador(new Posicion(2, 4), DANIO);
        Heroe heroe = heroeEn(new Posicion(2, 2));

        cazador.actuar(plano, heroe);
        assertEquals(new Posicion(2, 3), cazador.getPosicion());

        heroe.mover(new Ubicacion(0, 0, new Posicion(4, 5)));
        cazador.actuar(plano, heroe);
        assertEquals(new Posicion(2, 4), cazador.getPosicion());
    }

    @Test
    void conEmpateSiempreEligeLoMismo() {
        // Desde (2,3) las dos vecinas, (2,2) y (2,4), están a distancia 4.
        Cazador uno = new Cazador(new Posicion(2, 3), DANIO);
        Cazador otro = new Cazador(new Posicion(2, 3), DANIO);

        uno.actuar(planoConPared(), heroeEn(HEROE));
        otro.actuar(planoConPared(), heroeEn(HEROE));

        assertEquals(uno.getPosicion(), otro.getPosicion());
        assertTrue(uno.getPosicion().equals(new Posicion(2, 2))
                || uno.getPosicion().equals(new Posicion(2, 4)));
    }

    // =================== AL ALCANZAR AL HÉROE =================== //

    @Test
    void alAlcanzarAlHeroeLeRestaElDanioYVuelveAlInicio() {
        Cazador cazador = new Cazador(new Posicion(4, 5), DANIO);
        Heroe heroe = heroeEn(HEROE);
        cazador.actuar(planoConPared(), heroe);

        cazador.alEncontrarseConHeroe(heroe, new Agenda());

        assertEquals(70, heroe.getEnergia());
        assertEquals(new Posicion(4, 5), cazador.getPosicion());
    }

    @Test
    void sePuedeRegistrarEnElCatalogo() {
        CatalogoDeMonstruos catalogo = new CatalogoDeMonstruos();
        catalogo.registrar("CAZADOR", posicion -> new Cazador(posicion, DANIO));

        Monstruo creado = catalogo.crearAlAzar(new Posicion(2, 2), new Random(1));

        assertTrue(creado instanceof Cazador);
        assertEquals(new Posicion(2, 2), creado.getPosicion());
    }
}
