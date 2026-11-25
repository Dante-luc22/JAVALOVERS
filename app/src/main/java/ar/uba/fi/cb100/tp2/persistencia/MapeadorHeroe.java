package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.HeroeDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.ObjetoInventarioDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PosicionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.UbicacionDTO;

import java.util.Map;

/**
 * Convierte el estado actual de un héroe en su representación DTO.
 *
 * Conserva ubicación, energía, cofres abiertos e inventario.
 * Los efectos activos se incorporarán cuando se conozcan sus
 * implementaciones concretas.
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

        // No descartar silenciosamente los efectos activos.
        // Su representación se incorporará al estudiar Efecto.
        if (heroe.getEfectosActivos() == null) {
            throw new IllegalStateException(
                    "La lista de efectos activos no puede ser nula."
            );
        }

        if (!heroe.getEfectosActivos().isEmpty()) {
            throw new UnsupportedOperationException(
                    "Todavía no está implementado el guardado "
                            + "de los efectos activos del héroe."
            );
        }

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
