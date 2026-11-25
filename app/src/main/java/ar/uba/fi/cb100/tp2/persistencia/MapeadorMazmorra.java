package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.MazmorraDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PlanoDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.TableroDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PosicionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.UbicacionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.ConexionDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Convierte una mazmorra del modelo en su representación DTO.
 *
 * Conserva el orden de los planos, sus terrenos y sus conexiones.
 * La reconstrucción de las entidades se incorpora posteriormente.
 */
public class MapeadorMazmorra {

    /**
     * Convierte una mazmorra en un DTO.
     *
     * @param mazmorra mazmorra actual del juego
     * @return representación DTO de la mazmorra
     * @throws IllegalArgumentException si la mazmorra es nula
     * @throws UnsupportedOperationException si contiene entidades
     *         cuya persistencia todavía no está implementada
     */
    public MazmorraDTO mapear(Mazmorra mazmorra) {

        if (mazmorra == null) {
            throw new IllegalArgumentException(
                    "La mazmorra no puede ser nula."
            );
        }

        MazmorraDTO datos = new MazmorraDTO();

        datos.pisosPorTablero = mazmorra.getPisosPorTablero();
        datos.planos = new ArrayList<>();
        datos.conexiones = new ArrayList<>();

        // Conservar exactamente el orden de los planos.
        for (int i = 0; i < mazmorra.getPlanos().size(); i++) {

            Plano plano = mazmorra.getPlanos().get(i);

            if (plano == null) {
                throw new IllegalStateException(
                        "La mazmorra contiene un plano nulo en la posición "
                                + i
                );
            }

            // No guardar una mazmorra parcialmente:
            // todavía falta reconstruir los estados particulares
            // de monstruos y cofres.
            if (plano.getEntidades() == null) {
                throw new IllegalStateException(
                        "El plano " + (i + 1)
                                + " tiene una lista de entidades nula."
                );
            }

            if (!plano.getEntidades().isEmpty()) {
                throw new UnsupportedOperationException(
                        "Todavía no está implementado el guardado "
                                + "de entidades del plano " + (i + 1)
                                + ". No se puede guardar la mazmorra "
                                + "sin perder su estado."
                );
            }

            datos.planos.add(mapearPlano(plano));
        }

        // Cada conexión se guarda una sola vez.
        for (var conexion : mazmorra.getConexiones()) {

            ConexionDTO conexionDTO = new ConexionDTO();

            conexionDTO.origen = mapearUbicacion(conexion.origen());
            conexionDTO.destino = mapearUbicacion(conexion.destino());

            datos.conexiones.add(conexionDTO);
        }

        return datos;
    }

    /**
     * Convierte un plano conservando su tablero,
     * entrada y salida.
     */
    private PlanoDTO mapearPlano(Plano plano) {

        PlanoDTO datos = new PlanoDTO();

        datos.tablero = mapearTablero(plano.getTablero());
        datos.entrada = mapearPosicion(plano.getEntrada());

        datos.salida = plano.getSalida() == null
                ? null
                : mapearPosicion(plano.getSalida());

        datos.entidades = new ArrayList<>();

        return datos;
    }

    /**
     * Convierte un tablero en una matriz de nombres de terrenos.
     */
    private TableroDTO mapearTablero(Tablero tablero) {

        if (tablero == null) {
            throw new IllegalStateException(
                    "Un plano contiene un tablero nulo."
            );
        }

        TableroDTO datos = new TableroDTO();

        datos.filas = tablero.getFilas();
        datos.columnas = tablero.getColumnas();
        datos.celdas = new ArrayList<>();

        for (int fila = 1; fila <= datos.filas; fila++) {

            List<String> filaDTO = new ArrayList<>();

            for (int columna = 1;
                 columna <= datos.columnas;
                 columna++) {

                Posicion posicion = new Posicion(fila, columna);

                Terreno terreno = tablero.getTerreno(posicion);

                if (terreno == null) {
                    throw new IllegalStateException(
                            "Terreno nulo en fila " + fila
                                    + ", columna " + columna + "."
                    );
                }

                filaDTO.add(terreno.name());
            }

            datos.celdas.add(filaDTO);
        }

        return datos;
    }

    /**
     * Convierte una posición del modelo en un DTO.
     */
    private PosicionDTO mapearPosicion(Posicion posicion) {

        if (posicion == null) {
            throw new IllegalArgumentException(
                    "La posición no puede ser nula."
            );
        }

        PosicionDTO datos = new PosicionDTO();

        datos.fila = posicion.fila();
        datos.columna = posicion.columna();

        return datos;
    }

    /**
     * Convierte una ubicación global en un DTO.
     */
    private UbicacionDTO mapearUbicacion(Ubicacion ubicacion) {

        if (ubicacion == null) {
            throw new IllegalArgumentException(
                    "La ubicación no puede ser nula."
            );
        }

        UbicacionDTO datos = new UbicacionDTO();

        datos.tablero = ubicacion.tablero();
        datos.piso = ubicacion.piso();
        datos.posicion = mapearPosicion(ubicacion.posicion());

        return datos;
    }
}
