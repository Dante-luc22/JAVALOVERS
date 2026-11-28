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

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Convierte una agenda en su representación DTO.
 *
 * Conserva el paso actual y todos los eventos pendientes,
 * incluyendo su momento de ejecución y orden original.
 */
public class MapeadorAgenda {

    /**
     * Convierte una agenda en su representación DTO.
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

        AgendaDTO datos = new AgendaDTO();

        datos.paso = agenda.getPasoActual();
        datos.pendientes = new ArrayList<>();

        for (EventoProgramado pendiente : pendientes) {

            if (pendiente == null) {
                throw new IllegalStateException(
                        "La agenda contiene un evento programado nulo."
                );
            }

            datos.pendientes.add(mapearEvento(pendiente));
        }

        return datos;
    }

    /**
     * Convierte un evento programado en su representación DTO.
     *
     * @param programado evento programado
     * @return representación DTO del evento
     */
    private EventoDTO mapearEvento(EventoProgramado programado) {

        if (programado == null) {
            throw new IllegalStateException(
                    "El evento programado no puede ser nulo."
            );
        }

        Evento evento = programado.evento();

        if (evento == null) {
            throw new IllegalStateException(
                    "El evento no puede ser nulo."
            );
        }

        EventoDTO datos = new EventoDTO();

        // Conservar exactamente el vencimiento y el orden.
        datos.paso = programado.paso();
        datos.orden = programado.orden();

        // Inicializar los datos específicos.
        datos.datos = new JsonObject();
        if (evento instanceof AdormecerMonstruos adormecer) {
            datos.tipo = "ADORMECER_MONSTRUOS";
            guardarPlano(datos, adormecer.tablero(), adormecer.piso());

        } else if (evento instanceof DespertarMonstruos despertar) {
            datos.tipo = "DESPERTAR_MONSTRUOS";
            guardarPlano(datos, despertar.tablero(), despertar.piso());

        } else if (evento instanceof RepelerMonstruos repeler) {
            datos.tipo = "REPELER_MONSTRUOS";
            guardarPlano(datos, repeler.tablero(), repeler.piso());

        } else if (evento.getClass() == RomperPared.class) {
            // ... el resto queda igual else if (evento.getClass() == RomperPared.class) {

            datos.tipo = "ROMPER_PARED";

        } else if (evento.getClass() == QuitarEfecto.class) {

            QuitarEfecto quitarEfecto = (QuitarEfecto) evento;

            TipoDeEfecto tipoEfecto = quitarEfecto.getTipo();

            if (tipoEfecto == null) {
                throw new IllegalStateException(
                        "El evento QuitarEfecto no tiene un tipo válido."
                );
            }

            datos.tipo = "QUITAR_EFECTO";

            datos.datos.addProperty(
                    "tipoEfecto",
                    tipoEfecto.name()
            );

        } else {

            throw new UnsupportedOperationException(
                    "Todavía no está implementado el guardado del evento: "
                            + evento.getClass().getSimpleName()
            );
        }

        return datos;
    }

        private static void guardarPlano(EventoDTO datos, int tablero, int piso) {
        datos.datos.addProperty("tablero", tablero);
        datos.datos.addProperty("piso", piso);
    }
}