package ar.uba.fi.cb100.tp2.modelo.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PartidaTest {

    @Test
    void unaPartidaNuevaEmpiezaSinPasosNiCofres() {
        Partida partida = new Partida();

        assertEquals(0, partida.getPasos());
        assertEquals(0, partida.getCofresAbiertos());
    }

    @Test
    void registrarPasosYCofresSumaALosContadores() {
        Partida partida = new Partida();

        partida.registrarPaso();
        partida.registrarPaso();
        partida.registrarCofreAbierto();

        assertEquals(2, partida.getPasos());
        assertEquals(1, partida.getCofresAbiertos());
    }

    @Test
    void elPuntajeSumaCofresYEnergiaYRestaLosPasos() {
        // 3 cofres abiertos, 50 de energía y 20 pasos: 300 + 100 - 20
        assertEquals(380, Partida.calcularPuntaje(3, 50, 20));
    }

    @Test
    void elPuntajeDeUnaPartidaSinPasosNiCofresEsLaEnergiaPorDos() {
        // Sin cofres ni pasos y con la energía inicial: 0 + 200 - 0
        assertEquals(200, Partida.calcularPuntaje(0, 100, 0));
    }

    @Test
    void elPuntajePuedeSerNegativoSiSeDieronMuchosPasos() {
        // Sin cofres ni energía y con 10 pasos: 0 + 0 - 10
        assertEquals(-10, Partida.calcularPuntaje(0, 0, 10));
    }
}
