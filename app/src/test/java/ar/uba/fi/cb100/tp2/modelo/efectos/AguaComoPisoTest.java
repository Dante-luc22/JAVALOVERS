package ar.uba.fi.cb100.tp2.modelo.efectos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;

/**
 * Tests de cómo cada efecto ajusta el costo de un terreno. La duración ya no
 * es parte del efecto: la maneja la agenda (ver QuitarEfectoTest).
 * Costos de prueba: piso 1, agua 3, lava 8.
 */
class AguaComoPisoTest {

    private static final int PISO = 1;
    private static final int AGUA = 3;
    private static final int LAVA = 8;

    // =================== AGUA COMO PISO =================== //

    @Test
    void elAguaCuestaComoElPiso() {
        assertEquals(PISO, new AguaComoPiso().ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void siCambiaElCostoDelPisoElAguaLoSigue() {
        assertEquals(2, new AguaComoPiso().ajustarCosto(Terreno.AGUA, AGUA, 2));
    }

    @Test
    void aguaComoPisoNoCambiaLaLavaNiElPiso() {
        assertEquals(LAVA, new AguaComoPiso().ajustarCosto(Terreno.LAVA, LAVA, PISO));
        assertEquals(PISO, new AguaComoPiso().ajustarCosto(Terreno.PISO, PISO, PISO));
    }

    // =================== LAVA COMO PISO =================== //

    @Test
    void laLavaCuestaComoElPiso() {
        assertEquals(PISO, new LavaComoPiso().ajustarCosto(Terreno.LAVA, LAVA, PISO));
    }

    @Test
    void lavaComoPisoNoCambiaElAguaNiElPiso() {
        assertEquals(AGUA, new LavaComoPiso().ajustarCosto(Terreno.AGUA, AGUA, PISO));
        assertEquals(PISO, new LavaComoPiso().ajustarCosto(Terreno.PISO, PISO, PISO));
    }

    // =================== ENVENENADO =================== //

    @Test
    void envenenadoSumaUnoEnCualquierTerreno() {
        Envenenado veneno = new Envenenado();
        assertEquals(PISO + 1, veneno.ajustarCosto(Terreno.PISO, PISO, PISO));
        assertEquals(AGUA + 1, veneno.ajustarCosto(Terreno.AGUA, AGUA, PISO));
        assertEquals(LAVA + 1, veneno.ajustarCosto(Terreno.LAVA, LAVA, PISO));
    }
}
