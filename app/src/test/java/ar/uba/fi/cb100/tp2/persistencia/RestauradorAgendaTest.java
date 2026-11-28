package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.eventos.AdormecerMonstruos;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.DespertarMonstruos;
import ar.uba.fi.cb100.tp2.modelo.eventos.RepelerMonstruos;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.AgendaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EventoDTO;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestauradorAgendaTest {

    private final RestauradorAgenda restaurador =
            new RestauradorAgenda();

    /**
     * Comprueba que se conserve el paso actual
     * de una agenda sin eventos pendientes.
     */
    @Test
    void restaurarConservaPasoActual() {

        AgendaDTO datos = crearAgendaDTO(7);

        Agenda agenda = restaurador.restaurar(datos);

        assertEquals(7, agenda.getPasoActual());
        assertTrue(agenda.getPendientes().isEmpty());
    }

    /**
     * Comprueba que se reconstruya una agenda inicial.
     */
    @Test
    void restaurarConservaAgendaInicial() {

        AgendaDTO datos = crearAgendaDTO(0);

        Agenda agenda = restaurador.restaurar(datos);

        assertEquals(0, agenda.getPasoActual());
        assertTrue(agenda.getPendientes().isEmpty());
    }

    /**
     * Comprueba que no se puedan reconstruir eventos
     * pendientes sin una fábrica que los soporte.
     */
    @Test
    void restaurarRechazaEventosNoSoportados() {

        AgendaDTO datos = crearAgendaDTO(7);

        datos.pendientes.add(
                crearEventoDTO(10, 0, "EVENTO_DESCONOCIDO")
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> restaurador.restaurar(datos)
        );
    }

    /**
     * Comprueba que se conserve el orden de ejecución
     * de los eventos que vencen en el mismo paso.
     */
    @Test
    void restaurarConservaOrdenDeEventosProgramados() {

        List<String> ejecutados = new ArrayList<>();

        AgendaDTO datos = crearAgendaDTO(7);

        // Se agregan en orden inverso deliberadamente.
        datos.pendientes.add(
                crearEventoDTO(10, 8, "SEGUNDO")
        );

        datos.pendientes.add(
                crearEventoDTO(10, 2, "PRIMERO")
        );

        RestauradorAgenda restauradorConFabrica =
                new RestauradorAgenda(
                        eventoDTO ->
                                partida -> ejecutados.add(eventoDTO.tipo)
                );

        Agenda agenda = restauradorConFabrica.restaurar(datos);

        assertEquals(7, agenda.getPasoActual());

        // Ejecutar los eventos que vencen en el paso 10.
        agenda.ejecutarLosQueVencen(10, null);

        assertEquals(
                List.of("PRIMERO", "SEGUNDO"),
                ejecutados
        );
    }

    /**
     * Comprueba que el siguiente orden se reconstruya correctamente.
     */
    @Test
    void restaurarConservaSiguienteOrdenDisponible() {

        AgendaDTO datos = crearAgendaDTO(7);

        datos.pendientes.add(
                crearEventoDTO(10, 8, "EVENTO_EXISTENTE")
        );

        RestauradorAgenda restauradorConFabrica =
                new RestauradorAgenda(
                        eventoDTO -> partida -> {
                        }
                );

        Agenda agenda = restauradorConFabrica.restaurar(datos);

        // Nuevo evento para el mismo paso de vencimiento.
        agenda.programar(
                3,
                partida -> {
                }
        );

        assertEquals(2, agenda.getPendientes().size());

        assertEquals(
                8L,
                agenda.getPendientes().get(0).orden()
        );

        assertEquals(
                9L,
                agenda.getPendientes().get(1).orden()
        );
    }

    /**
     * Comprueba que se rechace un paso actual inválido.
     */
    @Test
    void restaurarRechazaPasoActualInvalido() {

        AgendaDTO datos = crearAgendaDTO(-1);

        assertThrows(
                IllegalArgumentException.class,
                () -> restaurador.restaurar(datos)
        );
    }

    /**
     * Crea una agenda DTO válida.
     */
    private AgendaDTO crearAgendaDTO(int paso) {

        AgendaDTO datos = new AgendaDTO();

        datos.paso = paso;
        datos.pendientes = new ArrayList<>();

        return datos;
    }

    /**
     * Crea un evento DTO de prueba.
     */
    private EventoDTO crearEventoDTO(
            int paso,
            long orden,
            String tipo
    ) {

        EventoDTO datos = new EventoDTO();

        datos.paso = paso;
        datos.orden = orden;
        datos.tipo = tipo;

        return datos;
    }

        @Test
    void losEventosDeMonstruosConservanElPlanoAlGuardarYCargar() {
        Agenda agenda = new Agenda();
        agenda.programar(1, new AdormecerMonstruos(2, 3));
        agenda.programar(6, new DespertarMonstruos(2, 3));
        agenda.programar(1, new RepelerMonstruos(1, 2));

        Agenda cargada = new RestauradorAgenda().restaurar(new MapeadorAgenda().mapear(agenda));

        assertEquals(agenda.getPendientes(), cargada.getPendientes());
    }
}