package ar.uba.fi.cb100.tp2.modelo.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

class CeldasVisitadasTest {

    private static Ubicacion en(int tablero, int piso, int fila, int columna) {
        return new Ubicacion(tablero, piso, new Posicion(fila, columna));
    }

    @Test
    void recienCreadaNoTieneNada() {
        CeldasVisitadas visitadas = new CeldasVisitadas();

        assertEquals(0, visitadas.total());
        assertEquals(0, visitadas.cantidadEn(1, 1));
        assertFalse(visitadas.fueVisitada(en(1, 1, 2, 2)));
    }

    @Test
    void laPrimeraVisitaDevuelveTrueYLaSegundaFalse() {
        CeldasVisitadas visitadas = new CeldasVisitadas();

        assertTrue(visitadas.visitar(en(1, 1, 2, 2)));
        assertFalse(visitadas.visitar(en(1, 1, 2, 2)));
        assertEquals(1, visitadas.cantidadEn(1, 1));
    }

    @Test
    void laMismaPosicionEnOtroPlanoCuentaAparte() {
        CeldasVisitadas visitadas = new CeldasVisitadas();
        visitadas.visitar(en(1, 1, 2, 2));
        visitadas.visitar(en(1, 2, 2, 2));
        visitadas.visitar(en(2, 1, 2, 2));

        assertEquals(1, visitadas.cantidadEn(1, 1));
        assertEquals(1, visitadas.cantidadEn(1, 2));
        assertEquals(1, visitadas.cantidadEn(2, 1));
        assertEquals(3, visitadas.total());
        assertFalse(visitadas.fueVisitada(en(2, 2, 2, 2)));
    }

    @Test
    void dosClavesConLosMismosDatosSonLaMismaClave() {
        Map<ClaveDePlano, String> tabla = new HashMap<>();
        tabla.put(new ClaveDePlano(1, 2), "a");

        assertEquals(new ClaveDePlano(1, 2), new ClaveDePlano(1, 2));
        assertEquals("a", tabla.get(new ClaveDePlano(1, 2)));
    }

    @Test
    void laClaveRechazaTableroOPisoMenorAUno() {
        assertThrows(IllegalArgumentException.class, () -> new ClaveDePlano(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new ClaveDePlano(1, 0));
    }
}
