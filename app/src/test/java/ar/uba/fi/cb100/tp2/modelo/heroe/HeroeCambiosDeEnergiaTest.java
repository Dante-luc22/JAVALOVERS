package ar.uba.fi.cb100.tp2.modelo.heroe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de los cambios de energía que el héroe deja anotados para que la vista
 * los muestre: negativos para el daño y positivos para la energía recuperada.
 */
class HeroeCambiosDeEnergiaTest {

    private static final int ENERGIA_INICIAL = 100;

    private Heroe heroe;

    @BeforeEach
    void crearHeroe() {
        this.heroe = new Heroe(new Ubicacion(1, 1, new Posicion(2, 2)), ENERGIA_INICIAL);
    }

    @Test
    void unHeroeNuevoNoTieneCambiosPendientes() {
        assertTrue(this.heroe.retirarCambiosDeEnergia().isEmpty());
    }

    @Test
    void restarEnergiaDejaElDanioAnotadoEnNegativo() {
        this.heroe.restarEnergia(30);

        assertEquals(List.of(-30), this.heroe.retirarCambiosDeEnergia());
    }

    @Test
    void sumarEnergiaDejaLoRecuperadoAnotadoEnPositivo() {
        this.heroe.restarEnergiaEnSilencio(10);
        this.heroe.sumarEnergia(1);

        assertEquals(List.of(1), this.heroe.retirarCambiosDeEnergia());
    }

    @Test
    void losCambiosSalenEnElOrdenEnQuePasaron() {
        this.heroe.restarEnergia(3);
        this.heroe.restarEnergia(30);
        this.heroe.sumarEnergia(5);

        assertEquals(List.of(-3, -30, 5), this.heroe.retirarCambiosDeEnergia());
    }

    @Test
    void cadaCambioSeRetiraUnaSolaVez() {
        this.heroe.restarEnergia(30);
        this.heroe.retirarCambiosDeEnergia();

        assertTrue(this.heroe.retirarCambiosDeEnergia().isEmpty());
    }

    @Test
    void restarEnSilencioBajaLaEnergiaPeroNoDejaNadaAnotado() {
        this.heroe.restarEnergiaEnSilencio(1);

        assertEquals(ENERGIA_INICIAL - 1, this.heroe.getEnergia());
        assertTrue(this.heroe.retirarCambiosDeEnergia().isEmpty());
    }

    @Test
    void conLaEnergiaLlenaSumarNoAnotaNada() {
        this.heroe.sumarEnergia(1);

        assertEquals(ENERGIA_INICIAL, this.heroe.getEnergia());
        assertTrue(this.heroe.retirarCambiosDeEnergia().isEmpty());
    }

    @Test
    void siSeCortaPorLaEnergiaMaximaSeAnotaSoloLoQueDeVerdadSubio() {
        this.heroe.restarEnergiaEnSilencio(4);
        this.heroe.sumarEnergia(20);

        assertEquals(List.of(4), this.heroe.retirarCambiosDeEnergia());
    }

    @Test
    void elDanioSeAnotaCompletoAunqueLaEnergiaNoAlcance() {
        this.heroe.restarEnergiaEnSilencio(ENERGIA_INICIAL - 5);
        this.heroe.restarEnergia(30);

        assertEquals(0, this.heroe.getEnergia());
        assertEquals(List.of(-30), this.heroe.retirarCambiosDeEnergia());
    }

    @Test
    void restarCeroNoAnotaNada() {
        this.heroe.restarEnergia(0);

        assertTrue(this.heroe.retirarCambiosDeEnergia().isEmpty());
    }

    @Test
    void unaCantidadNegativaSigueSiendoInvalidaYNoDejaNada() {
        assertThrows(IllegalArgumentException.class, () -> this.heroe.restarEnergia(-1));
        assertThrows(IllegalArgumentException.class, () -> this.heroe.restarEnergiaEnSilencio(-1));
        assertThrows(IllegalArgumentException.class, () -> this.heroe.sumarEnergia(-1));

        assertTrue(this.heroe.retirarCambiosDeEnergia().isEmpty());
    }

    @Test
    void laListaEntregadaEsUnaCopia() {
        this.heroe.restarEnergia(30);
        List<Integer> cambios = this.heroe.retirarCambiosDeEnergia();
        cambios.add(999);

        assertTrue(this.heroe.retirarCambiosDeEnergia().isEmpty());
    }
}
