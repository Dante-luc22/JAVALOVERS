package ar.uba.fi.cb100.tp2.modelo.monstruos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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
 * Tests de Monstruo (el esqueleto común) a través de Errante.
 */
class ErranteTest {

    private static final int DANIO = 30;
    private static final Posicion CENTRO = new Posicion(3, 3);

    // =================== AYUDAS =================== //

    /** Un plano de filas x columnas con las celdas dadas en piso y el resto pared. */
    private static Plano planoConPiso(int filas, int columnas, Posicion... pisos) {
        Tablero tablero = new Tablero(filas, columnas);
        for (Posicion piso : pisos) {
            tablero.setTerreno(piso, Terreno.PISO);
        }
        return new Plano(tablero, pisos[0], null);
    }

    /** Un plano de 7 x 7 con todo el interior en piso (borde de pared). */
    private static Plano planoAbierto() {
        Tablero tablero = new Tablero(7, 7);
        for (int fila = 2; fila <= 6; fila++) {
            for (int columna = 2; columna <= 6; columna++) {
                tablero.setTerreno(new Posicion(fila, columna), Terreno.PISO);
            }
        }
        return new Plano(tablero, new Posicion(2, 2), null);
    }

    private static Heroe heroeEn(Posicion posicion, int energia) {
        return new Heroe(new Ubicacion(0, 0, posicion), energia);
    }

    // =================== CONSTRUCTOR =================== //

    @Test
    void recienCreadoEstaEnSuPosicionInicialConSuDanio() {
        Errante errante = new Errante(CENTRO, DANIO, new Random(1));
        assertEquals(CENTRO, errante.getPosicion());
        assertEquals(CENTRO, errante.getPosicionInicial());
        assertEquals(DANIO, errante.getDanio());
    }

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new Errante(null, DANIO, new Random(1)));
    }

    @Test
    void constructorRechazaDanioCero() {
        assertThrows(IllegalArgumentException.class,
                () -> new Errante(CENTRO, 0, new Random(1)));
    }

    @Test
    void constructorRechazaDanioNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Errante(CENTRO, -5, new Random(1)));
    }

    @Test
    void constructorRechazaAzarNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Errante(CENTRO, DANIO, null));
    }

    // =================== MOVIMIENTO =================== //

    @Test
    void seMueveAUnaDeSusVecinasTransitables() {
        Plano cruz = planoConPiso(5, 5, CENTRO,
                new Posicion(2, 3), new Posicion(4, 3),
                new Posicion(3, 2), new Posicion(3, 4));
        List<Posicion> vecinas = cruz.getTablero().vecinasTransitables(CENTRO);
        Errante errante = new Errante(CENTRO, DANIO, new Random(7));

        errante.actuar(cruz, heroeEn(new Posicion(2, 3), 100));

        assertTrue(vecinas.contains(errante.getPosicion()),
                "Se movio a " + errante.getPosicion() + ", que no es vecina");
    }

    @Test
    void conUnaSolaVecinaVaAEsa() {
        Posicion unica = new Posicion(3, 4);
        Plano pasillo = planoConPiso(5, 5, CENTRO, unica);
        Errante errante = new Errante(CENTRO, DANIO, new Random(7));

        errante.actuar(pasillo, heroeEn(unica, 100));

        assertEquals(unica, errante.getPosicion());
    }

    @Test
    void encerradoSinVecinasSeQuedaDondeEsta() {
        Plano encierro = planoConPiso(5, 5, CENTRO);
        Errante errante = new Errante(CENTRO, DANIO, new Random(7));

        errante.actuar(encierro, heroeEn(CENTRO, 100));

        assertEquals(CENTRO, errante.getPosicion());
    }

    @Test
    void nuncaPisaUnaPared() {
        Plano plano = planoAbierto();
        Errante errante = new Errante(new Posicion(4, 4), DANIO, new Random(123));
        Heroe heroe = heroeEn(new Posicion(2, 2), 100);

        for (int paso = 0; paso < 200; paso++) {
            Posicion antes = errante.getPosicion();
            errante.actuar(plano, heroe);
            Posicion despues = errante.getPosicion();
            assertTrue(plano.getTablero().esTransitable(despues),
                    "Piso una pared en el paso " + paso + ": " + despues);
            int distancia = Math.abs(antes.fila() - despues.fila())
                    + Math.abs(antes.columna() - despues.columna());
            assertEquals(1, distancia, "Salto mas de una celda en el paso " + paso);
        }
    }

    @Test
    void mismaSemillaMismoRecorrido() {
        Plano plano = planoAbierto();
        Heroe heroe = heroeEn(new Posicion(2, 2), 100);
        Errante uno = new Errante(new Posicion(4, 4), DANIO, new Random(42));
        Errante otro = new Errante(new Posicion(4, 4), DANIO, new Random(42));

        for (int paso = 0; paso < 50; paso++) {
            uno.actuar(plano, heroe);
            otro.actuar(plano, heroe);
            assertEquals(uno.getPosicion(), otro.getPosicion(),
                    "Se separaron en el paso " + paso);
        }
    }

    @Test
    void actuarRechazaPlanoNulo() {
        Errante errante = new Errante(CENTRO, DANIO, new Random(1));
        assertThrows(IllegalArgumentException.class,
                () -> errante.actuar(null, heroeEn(CENTRO, 100)));
    }

    @Test
    void actuarRechazaHeroeNulo() {
        Errante errante = new Errante(CENTRO, DANIO, new Random(1));
        assertThrows(IllegalArgumentException.class,
                () -> errante.actuar(planoConPiso(5, 5, CENTRO), null));
    }

    @Test
    void actuarRechazaUnSubtipoQueDevuelvePosicionNula() {
        Monstruo roto = new Monstruo(CENTRO, DANIO) {
            @Override
            protected Posicion decidirMovimiento(Plano plano, Posicion heroe) {
                return null;
            }

            @Override
            protected void alAlcanzarAlHeroe(Heroe heroe, Agenda agenda) {
            }
        };
        assertThrows(IllegalStateException.class,
                () -> roto.actuar(planoConPiso(5, 5, CENTRO), heroeEn(CENTRO, 100)));
        assertEquals(CENTRO, roto.getPosicion());
    }

    // =================== AL ALCANZAR AL HÉROE =================== //

    @Test
    void alAlcanzarAlHeroeLeRestaElDanio() {
        Errante errante = new Errante(CENTRO, DANIO, new Random(1));
        Heroe heroe = heroeEn(CENTRO, 100);

        errante.alEncontrarseConHeroe(heroe, new Agenda());

        assertEquals(70, heroe.getEnergia());
    }

    @Test
    void alAlcanzarAlHeroeVuelveASuPosicionInicial() {
        Posicion unica = new Posicion(3, 4);
        Plano pasillo = planoConPiso(5, 5, CENTRO, unica);
        Errante errante = new Errante(CENTRO, DANIO, new Random(1));
        Heroe heroe = heroeEn(unica, 100);

        errante.actuar(pasillo, heroe);
        assertEquals(unica, errante.getPosicion());

        errante.alEncontrarseConHeroe(heroe, new Agenda());

        assertEquals(CENTRO, errante.getPosicion());
    }

    @Test
    void alAlcanzarAUnHeroeDebilLoDejaSinEnergia() {
        Errante errante = new Errante(CENTRO, DANIO, new Random(1));
        Heroe heroe = heroeEn(CENTRO, 20);

        errante.alEncontrarseConHeroe(heroe, new Agenda());

        assertEquals(0, heroe.getEnergia());
        assertTrue(heroe.estaSinEnergia());
    }

    @Test
    void alEncontrarseConHeroeRechazaHeroeNulo() {
        Errante errante = new Errante(CENTRO, DANIO, new Random(1));
        assertThrows(IllegalArgumentException.class,
                () -> errante.alEncontrarseConHeroe(null, new Agenda()));
    }
}
