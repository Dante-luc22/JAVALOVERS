package ar.uba.fi.cb100.tp2.modelo.efectos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;

/**
 * Tests de Efecto (duración y vencimiento) a través de AguaComoPiso.
 * Costos de prueba: piso 1, agua 3, lava 8.
 */
class AguaComoPisoTest {

    private static final int PISO = 1;

    // =================== CONSTRUCTOR =================== //

    @Test
    void recienCreadoTieneTodaSuDuracion() {
        AguaComoPiso efecto = new AguaComoPiso(20);
        assertEquals(20, efecto.getPasosRestantes());
        assertFalse(efecto.termino());
    }

    @Test
    void constructorRechazaDuracionCero() {
        assertThrows(IllegalArgumentException.class, () -> new AguaComoPiso(0));
    }

    @Test
    void constructorRechazaDuracionNegativa() {
        assertThrows(IllegalArgumentException.class, () -> new AguaComoPiso(-3));
    }

    // =================== AJUSTAR COSTO =================== //

    @Test
    void elAguaCuestaComoElPiso() {
        assertEquals(PISO, new AguaComoPiso(20).ajustarCosto(Terreno.AGUA, 3, PISO));
    }

    @Test
    void siCambiaElCostoDelPisoElAguaLoSigue() {
        assertEquals(2, new AguaComoPiso(20).ajustarCosto(Terreno.AGUA, 3, 2));
    }

    @Test
    void noCambiaElCostoDeLaLava() {
        assertEquals(8, new AguaComoPiso(20).ajustarCosto(Terreno.LAVA, 8, PISO));
    }

    @Test
    void noCambiaElCostoDelPiso() {
        assertEquals(PISO, new AguaComoPiso(20).ajustarCosto(Terreno.PISO, PISO, PISO));
    }

    // =================== DURACIÓN =================== //

    @Test
    void cadaPasoDescuentaUno() {
        AguaComoPiso efecto = new AguaComoPiso(3);
        efecto.pasarUnPaso();
        assertEquals(2, efecto.getPasosRestantes());
    }

    @Test
    void terminaAlLlegarACero() {
        AguaComoPiso efecto = new AguaComoPiso(2);
        efecto.pasarUnPaso();
        assertFalse(efecto.termino());
        efecto.pasarUnPaso();
        assertTrue(efecto.termino());
    }

    @Test
    void nuncaBajaDeCero() {
        AguaComoPiso efecto = new AguaComoPiso(1);
        efecto.pasarUnPaso();
        efecto.pasarUnPaso();
        efecto.pasarUnPaso();
        assertEquals(0, efecto.getPasosRestantes());
        assertTrue(efecto.termino());
    }
}
