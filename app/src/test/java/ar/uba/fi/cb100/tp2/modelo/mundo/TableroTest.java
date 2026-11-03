package ar.uba.fi.cb100.tp2.modelo.mundo;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Tests de Tablero: la grilla, sus límites y las vecinas transitables.
 */
class TableroTest {

    @Test
    void unTableroNuevoEsTodoPared() {
        Tablero tablero = new Tablero(3, 4);

        assertEquals(Terreno.PARED, tablero.getTerreno(new Posicion(1, 1)));
        assertEquals(Terreno.PARED, tablero.getTerreno(new Posicion(3, 4)));
    }

    @Test
    void setTerrenoCambiaLaCeldaYGetTerrenoLaDevuelve() {
        Tablero tablero = new Tablero(3, 3);
        Posicion centro = new Posicion(2, 2);

        tablero.setTerreno(centro, Terreno.AGUA);

        assertEquals(Terreno.AGUA, tablero.getTerreno(centro));
    }

        @Test
    void estaDentroAceptaLosBordesYRechazaLoDeAfuera() {
        Tablero tablero = new Tablero(3, 4);

        assertTrue(tablero.estaDentro(new Posicion(1, 1)));
        assertTrue(tablero.estaDentro(new Posicion(3, 4)));

        assertFalse(tablero.estaDentro(new Posicion(0, 1)));
        assertFalse(tablero.estaDentro(new Posicion(4, 1)));
        assertFalse(tablero.estaDentro(new Posicion(1, 0)));
        assertFalse(tablero.estaDentro(new Posicion(1, 5)));
    }

    @Test
    void esTransitableEsFalsoParaParedYParaAfueraDelTablero() {
        Tablero tablero = new Tablero(3, 3);
        tablero.setTerreno(new Posicion(2, 2), Terreno.LAVA);

        assertTrue(tablero.esTransitable(new Posicion(2, 2)));
        assertFalse(tablero.esTransitable(new Posicion(1, 1)));
        assertFalse(tablero.esTransitable(new Posicion(0, 2)));
    }

        @Test
    void vecinasTransitablesDevuelveSoloLasQueNoSonPared() {
        Tablero tablero = new Tablero(4, 4);
        tablero.setTerreno(new Posicion(2, 2), Terreno.PISO);
        tablero.setTerreno(new Posicion(3, 2), Terreno.AGUA);
        tablero.setTerreno(new Posicion(2, 3), Terreno.PISO);

        List<Posicion> vecinas = tablero.vecinasTransitables(new Posicion(2, 2));

        assertEquals(List.of(new Posicion(3, 2), new Posicion(2, 3)), vecinas);
    }

    @Test
    void vecinasTransitablesEnLaEsquinaNoSeSaleDelTablero() {
        Tablero tablero = new Tablero(2, 2);
        tablero.setTerreno(new Posicion(1, 1), Terreno.PISO);
        tablero.setTerreno(new Posicion(1, 2), Terreno.PISO);
        tablero.setTerreno(new Posicion(2, 1), Terreno.PISO);
        tablero.setTerreno(new Posicion(2, 2), Terreno.PISO);

        List<Posicion> vecinas = tablero.vecinasTransitables(new Posicion(1, 1));

        assertEquals(List.of(new Posicion(2, 1), new Posicion(1, 2)), vecinas);
    }

    @Test
    void unaCeldaRodeadaDeParedNoTieneVecinasTransitables() {
        Tablero tablero = new Tablero(3, 3);
        tablero.setTerreno(new Posicion(2, 2), Terreno.PISO);

        assertTrue(tablero.vecinasTransitables(new Posicion(2, 2)).isEmpty());
    }
}