package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.AgendaDTO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapeadorAgendaTest {

    private final MapeadorAgenda mapeador =
            new MapeadorAgenda();

    /**
     * Comprueba que se conserve el paso actual
     * de una agenda sin eventos pendientes.
     */
    @Test
    void mapearConservaPasoActual() {

        Agenda agenda = new Agenda();

        agenda.restaurar(7, java.util.List.of());

        AgendaDTO datos = mapeador.mapear(agenda);

        assertEquals(7, datos.paso);
        assertNotNull(datos.pendientes);
        assertTrue(datos.pendientes.isEmpty());
    }

    /**
     * Comprueba que una agenda recién creada
     * conserve su estado inicial.
     */
    @Test
    void mapearConservaAgendaInicial() {

        Agenda agenda = new Agenda();

        AgendaDTO datos = mapeador.mapear(agenda);

        assertEquals(0, datos.paso);
        assertNotNull(datos.pendientes);
        assertTrue(datos.pendientes.isEmpty());
    }

    /**
     * Comprueba que no se descarten eventos pendientes
     * cuya persistencia todavía no está implementada.
     */
    @Test
    void mapearRechazaEventosPendientesNoSoportados() {

        Agenda agenda = new Agenda();

        agenda.programar(
                3,
                partida -> {
                    // Evento de prueba.
                }
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> mapeador.mapear(agenda)
        );
    }

    /**
     * Comprueba que no se pueda mapear una agenda nula.
     */
    @Test
    void mapearRechazaAgendaNula() {

        assertThrows(
                IllegalArgumentException.class,
                () -> mapeador.mapear(null)
        );
    }
}