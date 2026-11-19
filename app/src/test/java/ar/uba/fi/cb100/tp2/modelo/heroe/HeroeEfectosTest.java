package ar.uba.fi.cb100.tp2.modelo.heroe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.efectos.AguaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de los efectos activos del héroe: se aplican en cadena, en el orden
 * en que se activaron, y se borran al vencer.
 * Costos de prueba: piso 1, agua 3, lava 8.
 */
class HeroeEfectosTest {

    private static final int PISO = 1;
    private static final int AGUA = 3;
    private static final int LAVA = 8;

    /** Un efecto de prueba que suma 1 al costo de cualquier terreno (como el veneno). */
    private static class MasUno extends Efecto {
        MasUno(int duracion) {
            super(duracion);
        }

        @Override
        public int ajustarCosto(Terreno terreno, int costoActual, int costoDelPiso) {
            return costoActual + 1;
        }
    }

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

    @Test
    void pasarUnPasoSinEfectosNoFalla() {
        this.heroe.pasarUnPaso();
        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    // =================== AGREGAR Y AJUSTAR =================== //

    @Test
    void conAguaComoPisoElAguaCuestaComoPiso() {
        this.heroe.agregarEfecto(new AguaComoPiso(20));

        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
        assertEquals(LAVA, this.heroe.ajustarCosto(Terreno.LAVA, LAVA, PISO));
    }

    @Test
    void agregarEfectoRechazaNulo() {
        assertThrows(IllegalArgumentException.class, () -> this.heroe.agregarEfecto(null));
    }

    @Test
    void losEfectosSeAplicanEnCadena() {
        // Zapatos primero (agua → 1) y después +1 → 2.
        // Si cada efecto recibiera el costo base, daría 4.
        this.heroe.agregarEfecto(new AguaComoPiso(20));
        this.heroe.agregarEfecto(new MasUno(20));

        assertEquals(2, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void elOrdenDeLosEfectosImporta() {
        // +1 primero (agua → 4) y después zapatos → 1.
        this.heroe.agregarEfecto(new MasUno(20));
        this.heroe.agregarEfecto(new AguaComoPiso(20));

        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void dosEfectosIgualesSeSuman() {
        this.heroe.agregarEfecto(new MasUno(5));
        this.heroe.agregarEfecto(new MasUno(5));

        assertEquals(LAVA + 2, this.heroe.ajustarCosto(Terreno.LAVA, LAVA, PISO));
    }

    // =================== VENCIMIENTO =================== //

    @Test
    void elEfectoDuraExactamenteSusPasos() {
        this.heroe.agregarEfecto(new AguaComoPiso(3));

        for (int paso = 1; paso <= 2; paso++) {
            this.heroe.pasarUnPaso();
            assertEquals(PISO, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO),
                    "Vencio antes de tiempo, en el paso " + paso);
        }
        this.heroe.pasarUnPaso();

        assertEquals(AGUA, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    @Test
    void dosEfectosQueVencenJuntosSeBorranLosDos() {
        // Borrar dentro del for haría que se saltee el segundo
        // (o tiraría ConcurrentModificationException).
        this.heroe.agregarEfecto(new AguaComoPiso(1));
        this.heroe.agregarEfecto(new MasUno(1));

        this.heroe.pasarUnPaso();

        assertTrue(this.heroe.getEfectosActivos().isEmpty());
        assertEquals(AGUA, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void venceUnoYElOtroSigue() {
        this.heroe.agregarEfecto(new MasUno(1));
        this.heroe.agregarEfecto(new AguaComoPiso(5));

        this.heroe.pasarUnPaso();

        assertEquals(1, this.heroe.getEfectosActivos().size());
        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void getEfectosActivosDevuelveUnaCopia() {
        this.heroe.agregarEfecto(new AguaComoPiso(20));

        this.heroe.getEfectosActivos().clear();

        assertEquals(1, this.heroe.getEfectosActivos().size());
        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }
}
