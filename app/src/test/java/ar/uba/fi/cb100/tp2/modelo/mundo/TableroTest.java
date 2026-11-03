package ar.uba.fi.cb100.tp2.modelo.mundo;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}