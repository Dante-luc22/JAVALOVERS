package ar.uba.fi.cb100.tp2.modelo.cofres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de CofreDeTrampaDePinchos: quita energía, sin bajar de 0.
 */
class CofreDeTrampaDePinchosTest {

    private static final Posicion LUGAR = new Posicion(3, 3);
    private static final int DANIO = 15;

    private Heroe heroe;
    private CofreDeTrampaDePinchos trampa;

    @BeforeEach
    void crear() {
        this.heroe = new Heroe(new Ubicacion(0, 0, LUGAR), 100);
        this.trampa = new CofreDeTrampaDePinchos(LUGAR, DANIO);
    }

    // =================== CONSTRUCTOR =================== //

    @Test
    void recienCreadoEstaEnSuPosicionYCerrado() {
        assertEquals(LUGAR, this.trampa.getPosicion());
        assertFalse(this.trampa.debeSalirDelPlano());
    }

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new CofreDeTrampaDePinchos(null, DANIO));
    }

    @Test
    void constructorRechazaDanioCero() {
        assertThrows(IllegalArgumentException.class,
                () -> new CofreDeTrampaDePinchos(LUGAR, 0));
    }

    @Test
    void constructorRechazaDanioNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> new CofreDeTrampaDePinchos(LUGAR, -10));
    }

    // =================== AL ABRIRSE =================== //

    @Test
    void alAbrirseLeQuitaElDanio() {
        this.trampa.alEncontrarseConHeroe(this.heroe, new Agenda());
        assertEquals(85, this.heroe.getEnergia());
    }

    @Test
    void conPocaEnergiaLoDejaEnCeroYPierde() {
        this.heroe.restarEnergia(90);

        this.trampa.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(0, this.heroe.getEnergia());
        assertTrue(this.heroe.estaSinEnergia());
    }

    @Test
    void aunqueSeaTrampaSumaElCofreAbierto() {
        this.trampa.alEncontrarseConHeroe(this.heroe, new Agenda());
        assertEquals(1, this.heroe.getCofresAbiertos());
    }

    @Test
    void alAbrirseQuedaParaSacarDelPlano() {
        this.trampa.alEncontrarseConHeroe(this.heroe, new Agenda());
        assertTrue(this.trampa.debeSalirDelPlano());
    }

    @Test
    void noCambiaLaUbicacionNiLaEnergiaMaximaDelHeroe() {
        Ubicacion antes = this.heroe.getUbicacion();

        this.trampa.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(antes, this.heroe.getUbicacion());
        assertEquals(100, this.heroe.getEnergiaMaxima());
    }

    @Test
    void alEncontrarseRechazaHeroeNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> this.trampa.alEncontrarseConHeroe(null, new Agenda()));
    }

    @Test
    void sePuedeRegistrarEnElCatalogoConSuDanio() {
        CatalogoDeCofres catalogo = new CatalogoDeCofres();
        catalogo.registrar("TRAMPA_DE_PINCHOS", posicion -> new CofreDeTrampaDePinchos(posicion, DANIO));

        Cofre creado = catalogo.crearAlAzar(LUGAR, new Random(1));
        creado.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertTrue(creado instanceof CofreDeTrampaDePinchos);
        assertEquals(85, this.heroe.getEnergia());
    }
}
