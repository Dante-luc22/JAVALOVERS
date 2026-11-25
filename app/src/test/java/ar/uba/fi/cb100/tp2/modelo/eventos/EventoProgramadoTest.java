package ar.uba.fi.cb100.tp2.modelo.eventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * El compareTo de EventoProgramado decide el orden de la agenda: el enunciado
 * pide que los tipos que entran en un heap tengan su compareTo testeado.
 */
class EventoProgramadoTest {

    private static final Evento NADA = partida -> { };

    @Test
    void vaPrimeroElQueVenceEnUnPasoAnterior() {
        EventoProgramado antes = new EventoProgramado(3, 9, NADA);
        EventoProgramado despues = new EventoProgramado(5, 1, NADA);

        assertTrue(antes.compareTo(despues) < 0);
        assertTrue(despues.compareTo(antes) > 0);
    }

    @Test
    void enElMismoPasoVaPrimeroElQueSeProgramoAntes() {
        EventoProgramado primero = new EventoProgramado(4, 1, NADA);
        EventoProgramado segundo = new EventoProgramado(4, 2, NADA);

        assertTrue(primero.compareTo(segundo) < 0);
        assertTrue(segundo.compareTo(primero) > 0);
    }

    @Test
    void mismoPasoYMismoOrdenDaCero() {
        assertEquals(0, new EventoProgramado(4, 1, NADA).compareTo(new EventoProgramado(4, 1, NADA)));
    }

    @Test
    void conPasosMuyGrandesNoSeDaVuelta() {
        EventoProgramado chico = new EventoProgramado(0, 0, NADA);
        EventoProgramado enorme = new EventoProgramado(Integer.MAX_VALUE, 0, NADA);

        assertTrue(chico.compareTo(enorme) < 0);
    }

    @Test
    void equalsYHashCodePorValor() {
        assertEquals(new EventoProgramado(2, 7, NADA), new EventoProgramado(2, 7, NADA));
        assertEquals(new EventoProgramado(2, 7, NADA).hashCode(), new EventoProgramado(2, 7, NADA).hashCode());
        assertNotEquals(new EventoProgramado(2, 7, NADA), new EventoProgramado(2, 8, NADA));
    }

    @Test
    void rechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new EventoProgramado(-1, 0, NADA));
        assertThrows(IllegalArgumentException.class, () -> new EventoProgramado(0, -1, NADA));
        assertThrows(IllegalArgumentException.class, () -> new EventoProgramado(0, 0, null));
    }
}
