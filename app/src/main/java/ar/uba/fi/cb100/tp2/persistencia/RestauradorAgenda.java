package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.Evento;
import ar.uba.fi.cb100.tp2.modelo.eventos.EventoProgramado;

import ar.uba.fi.cb100.tp2.modelo.eventos.AdormecerMonstruos;
import ar.uba.fi.cb100.tp2.modelo.eventos.DespertarMonstruos;
import ar.uba.fi.cb100.tp2.modelo.eventos.RepelerMonstruos;
import ar.uba.fi.cb100.tp2.modelo.eventos.RomperPared;
import ar.uba.fi.cb100.tp2.modelo.efectos.QuitarEfecto;

import ar.uba.fi.cb100.tp2.modelo.efectos.TipoDeEfecto;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.AgendaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EventoDTO;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Reconstruye una agenda a partir de los datos de una partida guardada.
 *
 * Conserva el paso actual, los eventos pendientes,
 * sus momentos de ejecución y su orden original.
 */
public class RestauradorAgenda {

    private final Function<EventoDTO, Evento> fabricaEventos;

    /**
     * Constructor predeterminado.
     *
     * Permite reconstruir los eventos actualmente soportados.
     */
    public RestauradorAgenda() {

        this(RestauradorAgenda::crearEvento);
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
     * Construye un evento concreto a partir de su representación DTO.
     *
     * @param datos representación DTO del evento
     * @return evento reconstruido
     */
    private static Evento crearEvento(EventoDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del evento no pueden ser nulos."
            );
        }

        if (datos.tipo == null || datos.tipo.isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de evento no puede ser nulo ni vacío."
            );
        }

        if (datos.datos == null) {
            throw new IllegalArgumentException(
                    "Los datos específicos del evento no pueden ser nulos."
            );
        }

        switch (datos.tipo) {

            case "ADORMECER_MONSTRUOS":
                return new AdormecerMonstruos();

            case "DESPERTAR_MONSTRUOS":
                return new DespertarMonstruos();

            case "REPELER_MONSTRUOS":
                return new RepelerMonstruos();

            case "ROMPER_PARED":
                return new RomperPared();

            case "QUITAR_EFECTO":
                return restaurarQuitarEfecto(datos);

            default:
                throw new UnsupportedOperationException(
                        "Todavía no está implementada la restauración "
                                + "del evento: " + datos.tipo
                );
        }
    }

    /**
     * Reconstruye un evento QuitarEfecto.
     *
     * Recupera el tipo de efecto que debe eliminarse.
     *
     * @param datos representación DTO del evento
     * @return evento reconstruido
     */
    private static Evento restaurarQuitarEfecto(EventoDTO datos) {

        JsonElement tipoGuardado =
                datos.datos.get("tipoEfecto");

        if (tipoGuardado == null
                || !tipoGuardado.isJsonPrimitive()
                || !tipoGuardado.getAsJsonPrimitive().isString()) {

            throw new IllegalArgumentException(
                    "El evento QuitarEfecto no contiene un tipo de efecto válido."
            );
        }

        String nombreTipo = tipoGuardado.getAsString();

        if (nombreTipo.isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de efecto no puede estar vacío."
            );
        }

        TipoDeEfecto tipoEfecto;

        try {

            tipoEfecto = TipoDeEfecto.valueOf(nombreTipo);

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "No existe un tipo de efecto registrado con el nombre: "
                            + nombreTipo,
                    e
            );
        }

        return new QuitarEfecto(tipoEfecto);
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

        // Recuperar el paso actual y los eventos pendientes.
        // Agenda.restaurar también calcula el siguiente orden disponible.
        Agenda agenda = new Agenda();

        agenda.restaurar(
                datos.paso,
                eventosRestaurados
        );

        return agenda;
    }
}