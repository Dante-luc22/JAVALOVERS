package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.EventoProgramado;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.AgendaDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Convierte una agenda del modelo en su representación DTO.
 *
 * Conserva el paso actual y los eventos pendientes.
 */
public class MapeadorAgenda {

    /**
     * Convierte una agenda en su representación DTO.
     *
     * Por el momento solamente admite agendas sin eventos pendientes,
     * ya que todavía no existen mapeadores para eventos concretos.
     *
     * @param agenda agenda actual de la partida
     * @return representación DTO de la agenda
     */
    public AgendaDTO mapear(Agenda agenda) {

        if (agenda == null) {
            throw new IllegalArgumentException(
                    "La agenda no puede ser nula."
            );
        }

        if (agenda.getPasoActual() < 0) {
            throw new IllegalStateException(
                    "El paso actual de la agenda es inválido."
            );
        }

        List<EventoProgramado> pendientes = agenda.getPendientes();

        if (pendientes == null) {
            throw new IllegalStateException(
                    "La lista de eventos pendientes no puede ser nula."
            );
        }

        // No descartar eventos que todavía no sabemos reconstruir.
        if (!pendientes.isEmpty()) {

            EventoProgramado primero = pendientes.get(0);

            throw new UnsupportedOperationException(
                    "Todavía no está implementado el guardado de eventos "
                            + "pendientes. Primer evento: "
                            + primero.evento().getClass().getSimpleName()
            );
        }

        AgendaDTO datos = new AgendaDTO();

        datos.paso = agenda.getPasoActual();
        datos.pendientes = new ArrayList<>();

        return datos;
    }
}