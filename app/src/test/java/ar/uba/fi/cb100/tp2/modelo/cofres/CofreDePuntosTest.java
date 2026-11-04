package ar.uba.fi.cb100.tp2.modelo.cofres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de Cofre (el esqueleto común) a través de CofreDePuntos.
 */
class CofreDePuntosTest {

    private static final int ENERGIA_INICIAL = 100;
    private static final Posicion LUGAR = new Posicion(3, 3);

    private Heroe heroe;
    private CofreDePuntos cofre;

    @BeforeEach
    void crear() {
        this.heroe = new Heroe(new Ubicacion(0, 0, LUGAR), ENERGIA_INICIAL);
        this.cofre = new CofreDePuntos(LUGAR);
    }

    // =================== CONSTRUCTOR =================== //

    @Test
    void recienCreadoEstaEnSuPosicion() {
        assertEquals(LUGAR, this.cofre.getPosicion());
    }

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new CofreDePuntos(null));
    }

    // =================== AL ABRIRSE =================== //

    @Test
    void alAbrirseSumaUnCofreAbierto() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());
        assertEquals(1, this.heroe.getCofresAbiertos());
    }

    @Test
    void dosCofresAbiertosSumanDos() {
        CofreDePuntos otro = new CofreDePuntos(new Posicion(3, 4));
        Agenda agenda = new Agenda();

        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);
        otro.alEncontrarseConHeroe(this.heroe, agenda);

        assertEquals(2, this.heroe.getCofresAbiertos());
    }

    @Test
    void alAbrirseNoCambiaLaEnergiaNiLaUbicacionDelHeroe() {
        Ubicacion antes = this.heroe.getUbicacion();

        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(ENERGIA_INICIAL, this.heroe.getEnergia());
        assertEquals(antes, this.heroe.getUbicacion());
    }

    @Test
    void alEncontrarseRechazaHeroeNuloSinContarNada() {
        assertThrows(IllegalArgumentException.class,
                () -> this.cofre.alEncontrarseConHeroe(null, new Agenda()));
    }

    // =================== ACTUAR =================== //

    @Test
    void actuarNoLoMueve() {
        Tablero tablero = new Tablero(5, 5);
        tablero.setTerreno(LUGAR, Terreno.PISO);
        tablero.setTerreno(new Posicion(3, 4), Terreno.PISO);
        Plano plano = new Plano(tablero, LUGAR, null);

        for (int paso = 0; paso < 10; paso++) {
            this.cofre.actuar(plano, this.heroe);
        }

        assertEquals(LUGAR, this.cofre.getPosicion());
        assertEquals(0, this.heroe.getCofresAbiertos());
    }
}
