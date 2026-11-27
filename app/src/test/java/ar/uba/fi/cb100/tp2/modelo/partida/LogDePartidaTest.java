package ar.uba.fi.cb100.tp2.modelo.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class LogDePartidaTest {

    @Test
    void recienCreadoEstaVacio() {
        LogDePartida log = new LogDePartida();

        assertEquals(0, log.cantidad());
        assertTrue(log.getEntradas().isEmpty());
    }

    @Test
    void guardaLasEntradasEnElOrdenEnQuePasaron() {
        LogDePartida log = new LogDePartida();
        log.anotar(1, "a");
        log.anotar(1, "b");
        log.anotar(2, "c");

        assertEquals(List.of(new EntradaDelLog(1, "a"), new EntradaDelLog(1, "b"),
                new EntradaDelLog(2, "c")), log.getEntradas());
    }

    @Test
    void entradasDelPasoDevuelveSoloLasDeEsePasoEnOrden() {
        LogDePartida log = new LogDePartida();
        log.anotar(1, "a");
        log.anotar(2, "b");
        log.anotar(2, "c");
        log.anotar(3, "d");

        assertEquals(List.of(new EntradaDelLog(2, "b"), new EntradaDelLog(2, "c")),
                log.entradasDelPaso(2));
    }

    @Test
    void unPasoSinNadaDevuelveVacio() {
        LogDePartida log = new LogDePartida();
        log.anotar(1, "a");

        assertTrue(log.entradasDelPaso(5).isEmpty());
    }

    @Test
    void getEntradasEsUnaCopia() {
        LogDePartida log = new LogDePartida();
        log.anotar(1, "a");

        log.getEntradas().clear();

        assertEquals(1, log.cantidad());
    }

    @Test
    void rechazaTextoVacioOPasoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new EntradaDelLog(1, " "));
        assertThrows(IllegalArgumentException.class, () -> new EntradaDelLog(-1, "a"));
    }
}
