
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.AgendaDTO;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Convierte una partida completa en su representación DTO.
 *
 * Delega la conversión del héroe, la mazmorra y la agenda
 * en componentes especializados.
 */
public class MapeadorPartida {

    private static final int VERSION_GUARDADO = 1;

    private final MapeadorHeroe mapeadorHeroe;
    private final MapeadorMazmorra mapeadorMazmorra;

    private final Function<Agenda, AgendaDTO> mapeadorAgenda;

    /**
     * Construye el mapeador con sus componentes especializados.
     *
     * @param mapeadorHeroe convierte el héroe
     * @param mapeadorMazmorra convierte la mazmorra
     * @param mapeadorAgenda convierte la agenda
     */
    public MapeadorPartida(
            MapeadorHeroe mapeadorHeroe,
            MapeadorMazmorra mapeadorMazmorra,
            Function<Agenda, AgendaDTO> mapeadorAgenda) {

        this.mapeadorHeroe = Objects.requireNonNull(
                mapeadorHeroe,
                "El mapeador del héroe no puede ser nulo.");
        this.mapeadorMazmorra = Objects.requireNonNull(
                mapeadorMazmorra,
                "El mapeador de la mazmorra no puede ser nulo.");

        this.mapeadorAgenda = Objects.requireNonNull(
                mapeadorAgenda,
                "El mapeador de la agenda no puede ser nulo.");
    }

    /**
     * Convierte una partida real en su DTO de persistencia.
     *
     * @param partida partida actual del juego
     * @return representación completa de la partida
     */
    public PartidaGuardadaDTO mapear(Partida partida) {

        if (partida == null) {
            throw new IllegalArgumentException(
                    "La partida no puede ser nula."
            );
        }

        PartidaGuardadaDTO datos = new PartidaGuardadaDTO();

        // Versión del formato de guardado.
        datos.version = VERSION_GUARDADO;

        // Contador de pasos.
        datos.pasos = partida.getPasos();

        // Dificultad original de la partida.
        datos.dificultad = partida.getDificultad() == null
                ? null
                : partida.getDificultad().name();

// Semilla original utilizada para generar la mazmorra.
        datos.semilla = partida.getSemilla();

        // Estado actual del generador aleatorio compartido.
        datos.estadoAzar = partida.getAzar().getEstadoInterno();

        // Costos efectivos utilizados durante esta partida.
        datos.costos = mapearCostos(partida.getCostos());

        // Estado actual del héroe.
        datos.heroe = mapeadorHeroe.mapear(partida.getHeroe());

        // Estado actual de la mazmorra.
        datos.mazmorra = mapeadorMazmorra.mapear(
                partida.getMazmorra()
        );

        // Agenda: se delega en su conversor específico.
        Agenda agenda = partida.getAgenda();

        if (agenda == null) {
            throw new IllegalStateException(
                    "La partida no tiene una agenda válida."
            );
        }

        try {
            datos.agenda = mapeadorAgenda.apply(agenda);

        } catch (RuntimeException e) {

            throw new IllegalStateException(
                    "No se pudo convertir la agenda de la partida.",
                    e
            );
        }

        if (datos.agenda == null) {
            throw new IllegalStateException(
                    "El mapeador de agenda devolvió un resultado nulo."
            );
        }

        return datos;
    }

    /**
     * Convierte los costos del enum Terreno a claves de texto.
     */
    private Map<String, Integer> mapearCostos(
            Map<Terreno, Integer> costosOriginales) {

        if (costosOriginales == null) {
            throw new IllegalStateException(
                    "Los costos de la partida no pueden ser nulos."
            );
        }

        Map<String, Integer> costos = new LinkedHashMap<>();

        for (Map.Entry<Terreno, Integer> entrada
                : costosOriginales.entrySet()) {

            Terreno terreno = entrada.getKey();
            Integer costo = entrada.getValue();

            if (terreno == null) {
                throw new IllegalStateException(
                        "La partida contiene un costo sin terreno."
                );
            }

            if (terreno == Terreno.PARED) {
                throw new IllegalStateException(
                        "La pared no puede tener costo de movimiento."
                );
            }

            if (costo == null || costo < 0) {
                throw new IllegalStateException(
                        "El costo del terreno " + terreno + " es inválido."
                );
            }

            costos.put(terreno.name(), costo);
        }

        for (Terreno terreno : new Terreno[]{
                Terreno.PISO,
                Terreno.AGUA,
                Terreno.LAVA
        }) {
            if (!costos.containsKey(terreno.name())) {
                throw new IllegalStateException(
                        "Falta el costo del terreno " + terreno + "."
                );
            }
        }
        return costos;
    }
}
