
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.partida.AzarPersistible;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.AgendaDTO;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Coordina la reconstrucción de una partida completa.
 *
 * Recibe los datos previamente leídos y validados por LectorPartida.
 * Delega la reconstrucción de sus componentes en restauradores
 * especializados.
 */
public class RestauradorPartida {

    private final RestauradorMazmorra restauradorMazmorra;

    private final RestauradorHeroe restauradorHeroe;

    private final Function<AgendaDTO, Agenda> restauradorAgenda;

    /**
     * Constructor con restauradores especializados.
     *
     * @param restauradorMazmorra restaurador de la mazmorra
     * @param restauradorHeroe restaurador del héroe
     * @param restauradorAgenda restaurador de la agenda
     */
    public RestauradorPartida(
            RestauradorMazmorra restauradorMazmorra,
            RestauradorHeroe restauradorHeroe,
            Function<AgendaDTO, Agenda> restauradorAgenda) {

        this.restauradorMazmorra = Objects.requireNonNull(
                restauradorMazmorra,
                "El restaurador de mazmorra no puede ser nulo."
        );

        this.restauradorHeroe = Objects.requireNonNull(
                restauradorHeroe,
                "El restaurador del héroe no puede ser nulo."
        );

        this.restauradorAgenda = Objects.requireNonNull(
                restauradorAgenda,
                "El restaurador de agenda no puede ser nulo."
        );
    }

    /**
     * Reconstruye una partida a partir de su DTO.
     *
     * @param datos información de la partida guardada
     * @return partida reconstruida
     */
    public Partida restaurar(PartidaGuardadaDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos de la partida no pueden ser nulos.");
        }

        if (datos.pasos == null || datos.pasos < 0) {
            throw new IllegalArgumentException("La cantidad de pasos guardados es inválida.");
        }
        // Recuperar la dificultad original.
        if (datos.dificultad == null || datos.dificultad.isBlank()) {
            throw new IllegalArgumentException("La dificultad guardada es inválida.");
        }

        Dificultad dificultad;

        try {
            dificultad = Dificultad.valueOf(datos.dificultad);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("La dificultad guardada no existe: " + datos.dificultad, e);
        }

        // Recuperar la semilla original.
        if (datos.semilla == null) {
            throw new IllegalArgumentException("La semilla original no puede ser nula.");
        }

        // 1. Recuperar el generador aleatorio en el estado guardado.
        if (datos.estadoAzar == null || datos.estadoAzar < 0 || datos.estadoAzar > ((1L << 48) - 1)) {
            throw new IllegalArgumentException("El estado del generador aleatorio es inválido.");
        }

        AzarPersistible azar =
                AzarPersistible.desdeEstado(datos.estadoAzar);

        // 2. Reconstruir la mazmorra.
        Mazmorra mazmorra = restauradorMazmorra.restaurar(datos.mazmorra,azar);

        // 3. Reconstruir al héroe.
        Heroe heroe = restauradorHeroe.restaurar(datos.heroe);

        // 4. Reconstruir los costos efectivos de la partida.
        Map<Terreno, Integer> costos = restaurarCostos(datos.costos);

        // 5. Reconstruir la agenda mediante su restaurador específico.
        Agenda agenda;

        try {
            agenda = restauradorAgenda.apply(datos.agenda);

        } catch (RuntimeException e) {

            throw new IllegalStateException(
                    "No se pudo reconstruir la agenda de la partida.",
                    e
            );
        }

        if (agenda == null) {
            throw new IllegalStateException(
                    "El restaurador devolvió una agenda nula."
            );
        }

        // 6. Crear la partida conservando todos sus valores originales,
        // incluido el estado del generador aleatorio.
        return new Partida(
                mazmorra,
                heroe,
                costos,
                datos.pasos,
                agenda,
                azar,
                dificultad,
                datos.semilla
        );
    }

    /**
     * Convierte las claves de texto del JSON a valores del enum Terreno.
     */
    private Map<Terreno, Integer> restaurarCostos(
            Map<String, Integer> datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los costos guardados no pueden ser nulos."
            );
        }

        Map<Terreno, Integer> costos =
                new EnumMap<>(Terreno.class);

        for (Map.Entry<String, Integer> entrada : datos.entrySet()) {

            if (entrada.getKey() == null) {
                throw new IllegalStateException(
                        "Hay un terreno sin identificador."
                );
            }

            if (entrada.getValue() == null || entrada.getValue() < 0) {
                throw new IllegalStateException(
                        "El costo de " + entrada.getKey() + " es inválido."
                );
            }

            Terreno terreno;

            try {
                terreno = Terreno.valueOf(entrada.getKey());

            } catch (IllegalArgumentException e) {

                throw new IllegalStateException(
                        "Terreno desconocido en los costos: "
                                + entrada.getKey(),
                        e
                );
            }

            if (terreno == Terreno.PARED) {
                throw new IllegalStateException(
                        "La pared no puede tener costo de movimiento."
                );
            }

            costos.put(terreno, entrada.getValue());
        }

        for (Terreno terreno : new Terreno[]{
                Terreno.PISO,
                Terreno.AGUA,
                Terreno.LAVA
        }) {
            if (!costos.containsKey(terreno)) {
                throw new IllegalStateException(
                        "Falta el costo del terreno " + terreno + "."
                );
            }
        }

        return costos;
    }
}
