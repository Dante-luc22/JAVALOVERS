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
 * Tests de Dormilon: duerme mientras el héroe está a más de "radio" pasos
 * caminando; cuando está más cerca, caza como un Cazador.
 *
 * Plano de prueba (el mismo de CazadorTest), con las distancias al héroe H:
 * <pre>
 *   4 5 4 3
 *   3 # # 2
 *   2 1 H 1
 * </pre>
 */
class DormilonTest {

    private static final int DANIO = 30;
    private static final int RADIO = 3;
    private static final Posicion HEROE = new Posicion(4, 4);

    // =================== AYUDAS =================== //

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

    private static Heroe heroeEn(Posicion posicion) {
        return new Heroe(new Ubicacion(0, 0, posicion), 100);
    }

    // =================== CONSTRUCTOR =================== //

    @Test
    void recienCreadoEstaEnSuPosicionInicialConSuDanio() {
        Dormilon dormilon = new Dormilon(new Posicion(2, 2), DANIO, RADIO);
        assertEquals(new Posicion(2, 2), dormilon.getPosicion());
        assertEquals(DANIO, dormilon.getDanio());
    }

    @Test
    void esUnCazador() {
        assertTrue(new Dormilon(new Posicion(2, 2), DANIO, RADIO) instanceof Cazador);
    }

    @Test
    void constructorRechazaRadioCero() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dormilon(new Posicion(2, 2), DANIO, 0));
    }

    @Test
    void constructorRechazaRadioNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dormilon(new Posicion(2, 2), DANIO, -1));
    }

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dormilon(null, DANIO, RADIO));
    }

    @Test
    void constructorRechazaDanioNoPositivo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dormilon(new Posicion(2, 2), 0, RADIO));
    }

    // =================== DUERME =================== //

    @Test
    void conElHeroeMasLejosQueElRadioSigueDurmiendo() {
        // (2,2) está a 4 pasos del héroe; el radio es 3.
        Dormilon dormilon = new Dormilon(new Posicion(2, 2), DANIO, RADIO);

        dormilon.actuar(planoConPared(), heroeEn(HEROE));

        assertEquals(new Posicion(2, 2), dormilon.getPosicion());
    }

    @Test
    void cuentaLaDistanciaCaminandoNoEnLineaRecta() {
        // (2,4) está a 2 celdas del héroe en línea recta, pero la pared
        // obliga a dar la vuelta: caminando son 4. Sigue durmiendo.
        Dormilon dormilon = new Dormilon(new Posicion(2, 4), DANIO, RADIO);

        dormilon.actuar(planoConPared(), heroeEn(HEROE));

        assertEquals(new Posicion(2, 4), dormilon.getPosicion());
    }

    @Test
    void conElHeroeInalcanzableSigueDurmiendo() {
        Tablero tablero = new Tablero(5, 5);
        tablero.setTerreno(new Posicion(2, 2), Terreno.PISO);
        tablero.setTerreno(new Posicion(2, 3), Terreno.PISO);
        tablero.setTerreno(new Posicion(4, 4), Terreno.PISO);
        Plano plano = new Plano(tablero, new Posicion(2, 2), null);
        Dormilon dormilon = new Dormilon(new Posicion(2, 2), DANIO, RADIO);

        dormilon.actuar(plano, heroeEn(new Posicion(4, 4)));

        assertEquals(new Posicion(2, 2), dormilon.getPosicion());
    }

    @Test
    void muchosPasosDormidoNoSeMueveNunca() {
        Plano plano = planoConPared();
        Dormilon dormilon = new Dormilon(new Posicion(2, 3), DANIO, RADIO);
        Heroe heroe = heroeEn(HEROE);

        for (int paso = 0; paso < 10; paso++) {
            dormilon.actuar(plano, heroe);
            assertEquals(new Posicion(2, 3), dormilon.getPosicion(), "Se movio en el paso " + paso);
        }
    }

    // =================== DESPIERTA Y CAZA =================== //

    @Test
    void justoEnElRadioDespiertaYAvanza() {
        // (3,2) está a 3 pasos: no es más que el radio, así que caza.
        Dormilon dormilon = new Dormilon(new Posicion(3, 2), DANIO, RADIO);

        dormilon.actuar(planoConPared(), heroeEn(HEROE));

        assertEquals(new Posicion(4, 2), dormilon.getPosicion());
    }

    @Test
    void conUnRadioMasGrandeDespiertaDesdeMasLejos() {
        Dormilon dormilon = new Dormilon(new Posicion(2, 2), DANIO, 5);

        dormilon.actuar(planoConPared(), heroeEn(HEROE));

        assertEquals(new Posicion(3, 2), dormilon.getPosicion());
    }

    @Test
    void despiertaCuandoElHeroeSeAcerca() {
        Plano plano = planoConPared();
        Dormilon dormilon = new Dormilon(new Posicion(2, 2), DANIO, RADIO);
        Heroe heroe = heroeEn(HEROE);

        dormilon.actuar(plano, heroe);
        assertEquals(new Posicion(2, 2), dormilon.getPosicion());

        heroe.mover(new Ubicacion(0, 0, new Posicion(4, 3)));
        dormilon.actuar(plano, heroe);
        assertEquals(new Posicion(3, 2), dormilon.getPosicion());
    }

    @Test
    void despiertoLlegaHastaElHeroe() {
        Plano plano = planoConPared();
        Dormilon dormilon = new Dormilon(new Posicion(3, 2), DANIO, RADIO);
        Heroe heroe = heroeEn(HEROE);

        for (int paso = 0; paso < 3; paso++) {
            dormilon.actuar(plano, heroe);
        }

        assertEquals(HEROE, dormilon.getPosicion());
    }

    @Test
    void siElHeroeSeAlejaVuelveADormirse() {
        Plano plano = planoConPared();
        Dormilon dormilon = new Dormilon(new Posicion(3, 2), DANIO, RADIO);
        Heroe heroe = heroeEn(HEROE);

        dormilon.actuar(plano, heroe);
        assertEquals(new Posicion(4, 2), dormilon.getPosicion());

        heroe.mover(new Ubicacion(0, 0, new Posicion(2, 5)));
        dormilon.actuar(plano, heroe);
        assertEquals(new Posicion(4, 2), dormilon.getPosicion());
    }

    // =================== AL ALCANZAR AL HÉROE =================== //

    @Test
    void alAlcanzarAlHeroeLeRestaElDanioYVuelveAlInicio() {
        Dormilon dormilon = new Dormilon(new Posicion(4, 5), DANIO, RADIO);
        Heroe heroe = heroeEn(HEROE);
        dormilon.actuar(planoConPared(), heroe);
        assertEquals(HEROE, dormilon.getPosicion());

        dormilon.alEncontrarseConHeroe(heroe, new Agenda());

        assertEquals(70, heroe.getEnergia());
        assertEquals(new Posicion(4, 5), dormilon.getPosicion());
    }

    @Test
    void sePuedeRegistrarEnElCatalogoConSuRadio() {
        CatalogoDeMonstruos catalogo = new CatalogoDeMonstruos();
        catalogo.registrar("DORMILON", posicion -> new Dormilon(posicion, DANIO, RADIO));

        Monstruo creado = catalogo.crearAlAzar(new Posicion(2, 2), new Random(1));

        assertTrue(creado instanceof Dormilon);
        assertEquals(new Posicion(2, 2), creado.getPosicion());
    }
}
