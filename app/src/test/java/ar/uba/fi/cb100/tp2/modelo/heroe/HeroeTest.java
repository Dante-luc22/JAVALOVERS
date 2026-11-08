package ar.uba.fi.cb100.tp2.modelo.heroe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de Heroe: creación, energía, cofres, movimiento y todas las
 * excepciones que lanza.
 */
class HeroeTest {

    private static final int ENERGIA_INICIAL = 100;

    private Ubicacion inicio;
    private Heroe heroe;

    @BeforeEach
    void crearHeroe() {
        this.inicio = new Ubicacion(0, 0, new Posicion(2, 2));
        this.heroe = new Heroe(this.inicio, ENERGIA_INICIAL);
    }

    // =================== CREACIÓN =================== //

    @Test
    void recienCreadoTieneEnergiaInicialUbicacionYCeroCofres() {
        assertEquals(ENERGIA_INICIAL, this.heroe.getEnergia());
        assertEquals(this.inicio, this.heroe.getUbicacion());
        assertEquals(0, this.heroe.getCofresAbiertos());
        assertFalse(this.heroe.estaSinEnergia());
    }

    @Test
    void aceptaEnergiaInicialMayorA100PorqueLaDecideLaConfiguracion() {
        Heroe conMucha = new Heroe(this.inicio, 150);
        assertEquals(150, conMucha.getEnergia());
    }

    @Test
    void constructorRechazaUbicacionNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new Heroe(null, ENERGIA_INICIAL));
    }

    @Test
    void constructorRechazaEnergiaInicialCero() {
        assertThrows(IllegalArgumentException.class,
                () -> new Heroe(this.inicio, 0));
    }

    @Test
    void constructorRechazaEnergiaInicialNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> new Heroe(this.inicio, -10));
    }

    // =================== ENERGÍA =================== //

    @Test
    void restarEnergiaDescuentaLaCantidad() {
        this.heroe.restarEnergia(30);
        assertEquals(70, this.heroe.getEnergia());
        assertFalse(this.heroe.estaSinEnergia());
    }

    @Test
    void restarVariasVecesAcumula() {
        this.heroe.restarEnergia(1);
        this.heroe.restarEnergia(3);
        this.heroe.restarEnergia(8);
        assertEquals(88, this.heroe.getEnergia());
    }

    @Test
    void restarExactamenteLaEnergiaQueTieneLaDejaEnCero() {
        this.heroe.restarEnergia(ENERGIA_INICIAL);
        assertEquals(0, this.heroe.getEnergia());
        assertTrue(this.heroe.estaSinEnergia());
    }

    @Test
    void restarMasDeLoQueTieneLaDejaEnCeroSinExcepcion() {
        Heroe debil = new Heroe(this.inicio, 20);
        debil.restarEnergia(30);
        assertEquals(0, debil.getEnergia());
        assertTrue(debil.estaSinEnergia());
    }

    @Test
    void restarEnergiaSinEnergiaSigueEnCero() {
        this.heroe.restarEnergia(ENERGIA_INICIAL);
        this.heroe.restarEnergia(8);
        assertEquals(0, this.heroe.getEnergia());
    }

    @Test
    void restarEnergiaRechazaCantidadNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> this.heroe.restarEnergia(-5));
        assertEquals(ENERGIA_INICIAL, this.heroe.getEnergia());
    }

    @Test
    void restarCeroNoCambiaLaEnergia() {
        this.heroe.restarEnergia(0);
        assertEquals(ENERGIA_INICIAL, this.heroe.getEnergia());
    }

    // =================== SUMAR ENERGÍA =================== //

    @Test
    void laEnergiaMaximaEsLaEnergiaInicial() {
        assertEquals(ENERGIA_INICIAL, this.heroe.getEnergiaMaxima());
        assertEquals(150, new Heroe(this.inicio, 150).getEnergiaMaxima());
    }

    @Test
    void sumarEnergiaDevuelveLaCantidad() {
        this.heroe.restarEnergia(40);
        this.heroe.sumarEnergia(25);
        assertEquals(85, this.heroe.getEnergia());
    }

    @Test
    void sumarEnergiaNoPasaDelMaximo() {
        this.heroe.restarEnergia(10);
        this.heroe.sumarEnergia(50);
        assertEquals(ENERGIA_INICIAL, this.heroe.getEnergia());
    }

    @Test
    void sumarConEnergiaLlenaNoCambiaNada() {
        this.heroe.sumarEnergia(30);
        assertEquals(ENERGIA_INICIAL, this.heroe.getEnergia());
    }

    @Test
    void conMaximoMayorA100SumaSinTopeEn100() {
        Heroe fuerte = new Heroe(this.inicio, 150);
        fuerte.restarEnergia(90);
        fuerte.sumarEnergia(50);
        assertEquals(110, fuerte.getEnergia());
    }

    @Test
    void conMaximoMenorA100NoSePasaDeSuMaximo() {
        Heroe debil = new Heroe(this.inicio, 80);
        debil.restarEnergia(10);
        debil.sumarEnergia(20);
        assertEquals(80, debil.getEnergia());
    }

    @Test
    void sumarEnergiaRechazaCantidadNegativa() {
        this.heroe.restarEnergia(40);
        assertThrows(IllegalArgumentException.class,
                () -> this.heroe.sumarEnergia(-5));
        assertEquals(60, this.heroe.getEnergia());
    }

    @Test
    void sumarCeroNoCambiaLaEnergia() {
        this.heroe.restarEnergia(40);
        this.heroe.sumarEnergia(0);
        assertEquals(60, this.heroe.getEnergia());
    }

    @Test
    void unHeroeSinEnergiaPuedeRecuperarla() {
        this.heroe.restarEnergia(ENERGIA_INICIAL);
        this.heroe.sumarEnergia(10);
        assertEquals(10, this.heroe.getEnergia());
        assertFalse(this.heroe.estaSinEnergia());
    }

    // =================== COFRES =================== //

    @Test
    void sumarCofreAbiertoIncrementaDeAUno() {
        this.heroe.sumarCofreAbierto();
        this.heroe.sumarCofreAbierto();
        assertEquals(2, this.heroe.getCofresAbiertos());
    }

    // =================== MOVIMIENTO =================== //

    @Test
    void moverCambiaLaUbicacion() {
        Ubicacion destino = new Ubicacion(0, 0, new Posicion(2, 3));
        this.heroe.mover(destino);
        assertEquals(destino, this.heroe.getUbicacion());
    }

    @Test
    void moverNoGastaEnergia() {
        this.heroe.mover(new Ubicacion(0, 0, new Posicion(2, 3)));
        assertEquals(ENERGIA_INICIAL, this.heroe.getEnergia());
    }

    @Test
    void moverRechazaDestinoNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> this.heroe.mover(null));
        assertEquals(this.inicio, this.heroe.getUbicacion());
    }
}
