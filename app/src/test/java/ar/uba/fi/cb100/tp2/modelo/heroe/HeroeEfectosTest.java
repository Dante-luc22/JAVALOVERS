package ar.uba.fi.cb100.tp2.modelo.heroe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.efectos.AguaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.efectos.Envenenado;
import ar.uba.fi.cb100.tp2.modelo.efectos.TipoDeEfecto;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de los efectos activos del héroe: se aplican en cadena, en el orden
 * en que se activaron, y se sacan por tipo con quitarEfecto (que es lo que
 * hace el evento QuitarEfecto cuando la agenda lo ejecuta).
 * Costos de prueba: piso 1, agua 3, lava 8.
 */
class HeroeEfectosTest {

    private static final int PISO = 1;
    private static final int AGUA = 3;
    private static final int LAVA = 8;

    private Heroe heroe;

    @BeforeEach
    void crear() {
        this.heroe = new Heroe(new Ubicacion(0, 0, new Posicion(2, 2)), 100);
    }

    // =================== SIN EFECTOS =================== //

    @Test
    void recienCreadoNoTieneEfectos() {
        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    @Test
    void sinEfectosElCostoNoCambia() {
        assertEquals(AGUA, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
        assertEquals(LAVA, this.heroe.ajustarCosto(Terreno.LAVA, LAVA, PISO));
    }

    // =================== AGREGAR Y AJUSTAR =================== //

    @Test
    void conAguaComoPisoElAguaCuestaComoPiso() {
        this.heroe.agregarEfecto(new AguaComoPiso());

        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
        assertEquals(LAVA, this.heroe.ajustarCosto(Terreno.LAVA, LAVA, PISO));
    }

    @Test
    void agregarEfectoRechazaNulo() {
        assertThrows(IllegalArgumentException.class, () -> this.heroe.agregarEfecto(null));
    }

    @Test
    void losEfectosSeAplicanEnCadena() {
        // Zapatos primero (agua → 1) y después veneno → 2.
        // Si cada efecto recibiera el costo base, daría 4.
        this.heroe.agregarEfecto(new AguaComoPiso());
        this.heroe.agregarEfecto(new Envenenado());

        assertEquals(2, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void elOrdenDeLosEfectosImporta() {
        // Veneno primero (agua → 4) y después zapatos → 1.
        this.heroe.agregarEfecto(new Envenenado());
        this.heroe.agregarEfecto(new AguaComoPiso());

        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void dosVenenosSeSuman() {
        this.heroe.agregarEfecto(new Envenenado());
        this.heroe.agregarEfecto(new Envenenado());

        assertEquals(LAVA + 2, this.heroe.ajustarCosto(Terreno.LAVA, LAVA, PISO));
    }

    // =================== QUITAR =================== //

    @Test
    void quitarElEfectoVuelveAlCostoNormal() {
        this.heroe.agregarEfecto(new AguaComoPiso());

        this.heroe.quitarEfecto(TipoDeEfecto.AGUA_COMO_PISO);

        assertTrue(this.heroe.getEfectosActivos().isEmpty());
        assertEquals(AGUA, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void quitarUnoNoTocaLosOtros() {
        this.heroe.agregarEfecto(new AguaComoPiso());
        this.heroe.agregarEfecto(new Envenenado());

        this.heroe.quitarEfecto(TipoDeEfecto.AGUA_COMO_PISO);

        assertEquals(1, this.heroe.getEfectosActivos().size());
        assertEquals(TipoDeEfecto.ENVENENADO, this.heroe.getEfectosActivos().get(0).getTipo());
        assertEquals(AGUA + 1, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void conDosDelMismoTipoQuitaUnoSolo() {
        // Dos zapatos usados: al vencer el primero, el otro sigue activo.
        this.heroe.agregarEfecto(new AguaComoPiso());
        this.heroe.agregarEfecto(new AguaComoPiso());

        this.heroe.quitarEfecto(TipoDeEfecto.AGUA_COMO_PISO);

        assertEquals(1, this.heroe.getEfectosActivos().size());
        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void quitaElPrimeroDeEseTipo() {
        Efecto primeros = new AguaComoPiso();
        Efecto segundos = new AguaComoPiso();
        this.heroe.agregarEfecto(primeros);
        this.heroe.agregarEfecto(segundos);

        this.heroe.quitarEfecto(TipoDeEfecto.AGUA_COMO_PISO);

        assertTrue(this.heroe.getEfectosActivos().get(0) == segundos);
    }

    @Test
    void quitaPorTipoAunqueElEfectoSeaOtroObjeto() {
        // Lo que pasa al retomar una partida guardada: el efecto del héroe es
        // un objeto nuevo, y el evento solo conoce su tipo.
        this.heroe.agregarEfecto(new AguaComoPiso());

        this.heroe.quitarEfecto(new AguaComoPiso().getTipo());

        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    @Test
    void quitarUnTipoQueNoTieneNoFallaNiTocaLosOtros() {
        this.heroe.agregarEfecto(new AguaComoPiso());

        this.heroe.quitarEfecto(TipoDeEfecto.LAVA_COMO_PISO);

        assertEquals(1, this.heroe.getEfectosActivos().size());
    }

    @Test
    void quitarSinEfectosNoFalla() {
        this.heroe.quitarEfecto(TipoDeEfecto.ENVENENADO);

        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    @Test
    void quitarEfectoRechazaNulo() {
        assertThrows(IllegalArgumentException.class, () -> this.heroe.quitarEfecto(null));
    }

    @Test
    void getEfectosActivosDevuelveUnaCopia() {
        this.heroe.agregarEfecto(new AguaComoPiso());

        this.heroe.getEfectosActivos().clear();

        assertEquals(1, this.heroe.getEfectosActivos().size());
    }
}
