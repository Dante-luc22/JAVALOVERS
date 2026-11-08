package ar.uba.fi.cb100.tp2.modelo.monstruos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Tests de Fantasma: va en línea recta hacia el héroe atravesando paredes,
 * pero avanza solo cada 2 pasos. En el primer paso descansa.
 */
class FantasmaTest {

    private static final int DANIO = 30;

    // =================== AYUDAS =================== //

    /** Un tablero de 7 x 7 todo de pared, salvo las celdas dadas. */
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

    /** Hace actuar al fantasma una vez y devuelve dónde quedó. */
    private static Posicion paso(Fantasma fantasma, Plano plano, Heroe heroe) {
        fantasma.actuar(plano, heroe);
        return fantasma.getPosicion();
    }

    // =================== CONSTRUCTOR =================== //

    @Test
    void recienCreadoEstaEnSuPosicionInicialConSuDanio() {
        Fantasma fantasma = new Fantasma(new Posicion(2, 2), DANIO);
        assertEquals(new Posicion(2, 2), fantasma.getPosicion());
        assertEquals(DANIO, fantasma.getDanio());
    }

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class, () -> new Fantasma(null, DANIO));
    }

    @Test
    void constructorRechazaDanioNoPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new Fantasma(new Posicion(2, 2), 0));
    }

    // =================== CADA 2 PASOS =================== //

    @Test
    void enElPrimerPasoDescansa() {
        Plano plano = planoConPiso(new Posicion(4, 6));
        Fantasma fantasma = new Fantasma(new Posicion(4, 2), DANIO);

        assertEquals(new Posicion(4, 2), paso(fantasma, plano, heroeEn(new Posicion(4, 6))));
    }

    @Test
    void enElSegundoPasoAvanza() {
        Plano plano = planoConPiso(new Posicion(4, 6));
        Fantasma fantasma = new Fantasma(new Posicion(4, 2), DANIO);
        Heroe heroe = heroeEn(new Posicion(4, 6));

        paso(fantasma, plano, heroe);

        assertEquals(new Posicion(4, 3), paso(fantasma, plano, heroe));
    }

    @Test
    void alternaDescansoYAvance() {
        Plano plano = planoConPiso(new Posicion(4, 7));
        Fantasma fantasma = new Fantasma(new Posicion(4, 1), DANIO);
        Heroe heroe = heroeEn(new Posicion(4, 7));

        Posicion[] esperadas = {
                new Posicion(4, 1), new Posicion(4, 2),
                new Posicion(4, 2), new Posicion(4, 3),
                new Posicion(4, 3), new Posicion(4, 4),
        };
        for (int i = 0; i < esperadas.length; i++) {
            assertEquals(esperadas[i], paso(fantasma, plano, heroe), "Mal en el paso " + (i + 1));
        }
    }

    // =================== ATRAVIESA PAREDES =================== //

    @Test
    void atraviesaUnaParedEnVezDeRodearla() {
        // Todo el tablero es pared salvo la celda del héroe:
        // un cazador no podría moverse, el fantasma va derecho.
        Posicion heroe = new Posicion(4, 6);
        Plano plano = planoConPiso(heroe);
        Fantasma fantasma = new Fantasma(new Posicion(4, 2), DANIO);
        Heroe elHeroe = heroeEn(heroe);

        for (int i = 0; i < 8; i++) {
            paso(fantasma, plano, elHeroe);
        }

        assertEquals(heroe, fantasma.getPosicion());
    }

    @Test
    void vaEnDiagonalAcercandoseEnFilaYColumna() {
        // Héroe abajo a la izquierda: en 4 avances queda a distancia 0
        // (2 filas y 2 columnas). Si la distancia estuviera mal calculada,
        // el fantasma creería que ya llegó y se quedaría quieto.
        Posicion heroe = new Posicion(4, 2);
        Plano plano = planoConPiso(heroe);
        Fantasma fantasma = new Fantasma(new Posicion(2, 4), DANIO);
        Heroe elHeroe = heroeEn(heroe);

        for (int i = 0; i < 8; i++) {
            paso(fantasma, plano, elHeroe);
        }

        assertEquals(heroe, fantasma.getPosicion());
    }

    @Test
    void cadaAvanceEsDeUnaSolaCelda() {
        Plano plano = planoConPiso(new Posicion(6, 6));
        Fantasma fantasma = new Fantasma(new Posicion(1, 1), DANIO);
        Heroe heroe = heroeEn(new Posicion(6, 6));

        for (int i = 0; i < 20; i++) {
            Posicion antes = fantasma.getPosicion();
            Posicion despues = paso(fantasma, plano, heroe);
            int salto = Math.abs(antes.fila() - despues.fila())
                    + Math.abs(antes.columna() - despues.columna());
            assertTrue(salto <= 1, "Salto " + salto + " celdas en el paso " + (i + 1));
        }
    }

    @Test
    void nuncaSaleDelTablero() {
        Plano plano = planoConPiso(new Posicion(1, 1));
        Fantasma fantasma = new Fantasma(new Posicion(1, 7), DANIO);
        Heroe heroe = heroeEn(new Posicion(1, 1));

        for (int i = 0; i < 30; i++) {
            Posicion despues = paso(fantasma, plano, heroe);
            assertTrue(plano.getTablero().estaDentro(despues), "Salio del tablero: " + despues);
        }
    }

    @Test
    void siYaEstaEnLaCeldaDelHeroeSeQuedaAhi() {
        Posicion heroe = new Posicion(3, 3);
        Plano plano = planoConPiso(heroe);
        Fantasma fantasma = new Fantasma(heroe, DANIO);
        Heroe elHeroe = heroeEn(heroe);

        paso(fantasma, plano, elHeroe);
        assertEquals(heroe, paso(fantasma, plano, elHeroe));
    }

    // =================== AL ALCANZAR AL HÉROE =================== //

    @Test
    void alAlcanzarAlHeroeLeRestaElDanioYVuelveAlInicio() {
        Posicion heroe = new Posicion(4, 3);
        Plano plano = planoConPiso(heroe);
        Fantasma fantasma = new Fantasma(new Posicion(4, 2), DANIO);
        Heroe elHeroe = heroeEn(heroe);
        paso(fantasma, plano, elHeroe);
        paso(fantasma, plano, elHeroe);
        assertEquals(heroe, fantasma.getPosicion());

        fantasma.alEncontrarseConHeroe(elHeroe, new Agenda());

        assertEquals(70, elHeroe.getEnergia());
        assertEquals(new Posicion(4, 2), fantasma.getPosicion());
    }

    @Test
    void sePuedeRegistrarEnElCatalogo() {
        CatalogoDeMonstruos catalogo = new CatalogoDeMonstruos();
        catalogo.registrar("FANTASMA", posicion -> new Fantasma(posicion, DANIO));

        Monstruo creado = catalogo.crearAlAzar(new Posicion(2, 2), new Random(1));

        assertTrue(creado instanceof Fantasma);
    }
}
