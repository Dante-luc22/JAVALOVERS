package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;
import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.efectos.TipoDeEfecto;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.HeroeDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.ObjetoInventarioDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PosicionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.UbicacionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EfectoDTO;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Convierte el estado actual de un héroe en su representación DTO.
 * Conserva ubicación, energía, cofres abiertos, inventario y efectos activos.
 * La duración de los efectos se conserva mediante los eventos de la agenda.
 */
public class MapeadorHeroe {

    /**
     * Convierte un héroe del modelo en su representación DTO.
     *
     * @param heroe héroe actual de la partida
     * @return representación DTO del héroe
     */
    public HeroeDTO mapear(Heroe heroe) {

        if (heroe == null) {
            throw new IllegalArgumentException(
                    "El héroe no puede ser nulo."
            );
        }

        HeroeDTO datos = new HeroeDTO();

        // Conservar ubicación actual.
        datos.ubicacion = mapearUbicacion(heroe.getUbicacion());

        // Conservar atributos energéticos.
        datos.energia = heroe.getEnergia();
        datos.energiaMaxima = heroe.getEnergiaMaxima();

        // Conservar cantidad de cofres abiertos.
        datos.cofresAbiertos = heroe.getCofresAbiertos();

        // Conservar inventario y orden de inserción.
        if (heroe.getInventario() == null) {
            throw new IllegalStateException(
                    "El héroe no tiene inventario."
            );
        }

        Map<String, Integer> cantidades =
                heroe.getInventario().getCantidades();

        for (Map.Entry<String, Integer> entrada
                : cantidades.entrySet()) {

            if (entrada.getKey() == null
                    || entrada.getValue() == null
                    || entrada.getValue() <= 0) {

                throw new IllegalStateException(
                        "El inventario contiene una entrada inválida."
                );
            }

            ObjetoInventarioDTO objeto =
                    new ObjetoInventarioDTO();

            objeto.id = entrada.getKey();
            objeto.cantidad = entrada.getValue();

            datos.inventario.add(objeto);
        }

        // Conservar los efectos activos.
        List<Efecto> efectosActivos = heroe.getEfectosActivos();

        if (efectosActivos == null) {
            throw new IllegalStateException(
                    "La lista de efectos activos no puede ser nula."
            );
        }

        for (Efecto efecto : efectosActivos) {
            datos.efectosActivos.add(mapearEfecto(efecto));
        }

        return datos;
    }

    /**
     * Convierte un efecto activo en su representación DTO.
     * Conserva su tipo. La duración se representa mediante
     * los eventos pendientes de la agenda.
     *
     * @param efecto efecto activo del héroe
     * @return representación DTO del efecto
     */
    private EfectoDTO mapearEfecto(Efecto efecto) {

        if (efecto == null) {
            throw new IllegalStateException(
                    "No se puede guardar un efecto nulo."
            );
        }

        TipoDeEfecto tipo = efecto.getTipo();

        if (tipo == null) {
            throw new IllegalStateException(
                    "El efecto no tiene un tipo válido."
            );
        }

        if (tipo != TipoDeEfecto.AGUA_COMO_PISO
                && tipo != TipoDeEfecto.LAVA_COMO_PISO
                && tipo != TipoDeEfecto.ENVENENADO) {

            throw new UnsupportedOperationException(
                    "Todavía no está implementado el guardado del efecto: "
                            + tipo
            );
        }

        EfectoDTO datos = new EfectoDTO();

        // Guardar el nombre del enum, no el nombre de la clase.
        datos.tipo = tipo.name();

        // El efecto no tiene datos adicionales propios.
        datos.datos = new JsonObject();

        return datos;
    }

    /**
     * Convierte una ubicación del modelo en un DTO.
     */
    private UbicacionDTO mapearUbicacion(Ubicacion ubicacion) {

        if (ubicacion == null) {
            throw new IllegalStateException(
                    "El héroe no tiene una ubicación válida."
            );
        }

        UbicacionDTO datos = new UbicacionDTO();

        datos.tablero = ubicacion.tablero();
        datos.piso = ubicacion.piso();
        datos.posicion = mapearPosicion(ubicacion.posicion());

        return datos;
    }

    /**
     * Convierte una posición del modelo en un DTO.
     */
    private PosicionDTO mapearPosicion(Posicion posicion) {

        if (posicion == null) {
            throw new IllegalStateException(
                    "La posición del héroe no puede ser nula."
            );
        }

        PosicionDTO datos = new PosicionDTO();

        datos.fila = posicion.fila();
        datos.columna = posicion.columna();

        return datos;
    }
}