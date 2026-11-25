package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.Evento;
import ar.uba.fi.cb100.tp2.modelo.eventos.EventoProgramado;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.AgendaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EventoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Reconstruye una agenda a partir de su representación DTO.
 *
 * Conserva el paso actual, los eventos pendientes y su orden.
 */
public class RestauradorAgenda {

    private final Function<EventoDTO, Evento> fabricaEventos;

    /**
     * Constructor predeterminado.
     *
     * Permite restaurar agendas sin eventos pendientes.
     * Los eventos concretos se incorporarán progresivamente.
     */
    public RestauradorAgenda() {

        this(datos -> {
            throw new UnsupportedOperationException(
                    "Todavía no está implementada la restauración "
                            + "del evento: " + datos.tipo
            );
        });
    }

    /**
     * Constructor que permite inyectar una fábrica de eventos.
     *
     * @param fabricaEventos reconstruye eventos desde sus DTO
     */
    public RestauradorAgenda(
            Function<EventoDTO, Evento> fabricaEventos
    ) {

        this.fabricaEventos = Objects.requireNonNull(
                fabricaEventos,
                "La fábrica de eventos no puede ser nula."
        );
    }

    /**
     * Reconstruye una agenda completa.
     *
     * @param datos representación DTO de la agenda
     * @return agenda reconstruida
     */
    public Agenda restaurar(AgendaDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos de la agenda no pueden ser nulos."
            );
        }

        if (datos.paso == null || datos.paso < 0) {
            throw new IllegalArgumentException(
                    "El paso actual de la agenda es inválido."
            );
        }

        if (datos.pendientes == null) {
            throw new IllegalArgumentException(
                    "La lista de eventos pendientes no puede ser nula."
            );
        }

        List<EventoProgramado> eventosRestaurados =
                new ArrayList<>();

        for (EventoDTO eventoDTO : datos.pendientes) {

            if (eventoDTO == null) {
                throw new IllegalArgumentException(
                        "La agenda contiene un evento nulo."
                );
            }

            if (eventoDTO.paso == null || eventoDTO.paso < 0) {
                throw new IllegalArgumentException(
                        "El paso de un evento pendiente es inválido."
                );
            }

            if (eventoDTO.orden == null || eventoDTO.orden < 0) {
                throw new IllegalArgumentException(
                        "El orden de un evento pendiente es inválido."
                );
            }

            if (eventoDTO.tipo == null || eventoDTO.tipo.isBlank()) {
                throw new IllegalArgumentException(
                        "El tipo de un evento pendiente es inválido."
                );
            }

            Evento evento;

            try {
                evento = fabricaEventos.apply(eventoDTO);

            } catch (UnsupportedOperationException e) {
                throw e;

            } catch (RuntimeException e) {
                throw new IllegalStateException(
                        "No se pudo reconstruir el evento: "
                                + eventoDTO.tipo,
                        e
                );
            }

            if (evento == null) {
                throw new IllegalStateException(
                        "La fábrica devolvió un evento nulo: "
                                + eventoDTO.tipo
                );
            }

            eventosRestaurados.add(
                    new EventoProgramado(
                            eventoDTO.paso,
                            eventoDTO.orden,
                            evento
                    )
            );
        }

        Agenda agenda = new Agenda();

        agenda.restaurar(
                datos.paso,
                eventosRestaurados
        );

        return agenda;
    }
}