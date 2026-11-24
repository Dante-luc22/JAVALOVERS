
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.AgendaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.ConexionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EntidadDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EventoDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PlanoDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PosicionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.TableroDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.UbicacionDTO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lee y valida un archivo de partida guardada.
 *
 * Devuelve un DTO validado.
 * La reconstrucción de los objetos del modelo queda a cargo
 * de RestauradorPartida.
 */
public class LectorPartida {

    private static final Gson GSON = new GsonBuilder().create();

    private static final Set<String> TERRENOS_VALIDOS = Set.of(
            "PARED", "PISO", "AGUA", "LAVA"
    );

    /**
     * Lee el archivo JSON y valida su contenido.
     *
     * @param ruta ubicación del archivo
     * @return DTO de la partida guardada
     * @throws IllegalArgumentException si la ruta es nula
     * @throws IllegalStateException si el archivo no existe,
     *                               está dañado o contiene datos inválidos
     */
    public PartidaGuardadaDTO leer(Path ruta) {

        if (ruta == null) {
            throw new IllegalArgumentException(
                    "La ruta no puede ser nula."
            );
        }

        if (!Files.isRegularFile(ruta)) {
            throw new IllegalStateException(
                    "No se encontró el archivo de partida: "
                            + ruta.toAbsolutePath()
            );
        }

        PartidaGuardadaDTO partida;

        try (Reader lector = Files.newBufferedReader(
                ruta, StandardCharsets.UTF_8)) {

            partida = GSON.fromJson(
                    lector,
                    PartidaGuardadaDTO.class
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No se pudo leer la partida: " + ruta,
                    e
            );

        } catch (JsonParseException e) {

            throw new IllegalStateException(
                    "El archivo de partida no contiene un JSON válido: "
                            + e.getMessage(),
                    e
            );
        }

        validar(partida);

        return partida;
    }

    /**
     * Valida la estructura general de la partida.
     */
    private void validar(PartidaGuardadaDTO partida) {

        exigir(
                partida != null,
                "El archivo de partida está vacío."
        );

        exigir(
                partida.version != null && partida.version == 1,
                "Versión de guardado ausente o no compatible."
        );

        exigir(
                partida.pasos != null && partida.pasos >= 0,
                "La cantidad de pasos es inválida."
        );

        validarCostos(partida.costos);

        validarHeroe(partida.heroe);

        validarMazmorra(
                partida.mazmorra,
                partida.heroe.ubicacion
        );

        validarAgenda(partida.agenda);
    }

    /**
     * Verifica los costos de los terrenos utilizados por la partida.
     */
    private void validarCostos(Map<String, Integer> costos) {

        exigir(
                costos != null,
                "Faltan los costos de los terrenos."
        );

        for (String terreno : List.of("PISO", "AGUA", "LAVA")) {

            Integer costo = costos.get(terreno);

            exigir(
                    costo != null && costo >= 0,
                    "Falta un costo válido para el terreno " + terreno + "."
            );
        }
    }

    /**
     * Verifica los datos persistidos del héroe.
     */
    private void validarHeroe(
            PartidaGuardadaDTO.HeroeDTO heroe) {

        exigir(
                heroe != null,
                "Faltan los datos del héroe."
        );

        exigir(
                heroe.energia != null && heroe.energia >= 0,
                "La energía actual del héroe es inválida."
        );

        exigir(
                heroe.energiaMaxima != null && heroe.energiaMaxima > 0,
                "La energía máxima del héroe es inválida."
        );

        exigir(
                heroe.energia <= heroe.energiaMaxima,
                "La energía actual supera la energía máxima."
        );

        exigir(
                heroe.cofresAbiertos != null && heroe.cofresAbiertos >= 0,
                "La cantidad de cofres abiertos es inválida."
        );

        exigir(
                heroe.ubicacion != null,
                "Falta la ubicación del héroe."
        );

        exigir(
                heroe.inventario != null,
                "Falta la información del inventario."
        );

        Set<String> ids = new HashSet<>();

        for (PartidaGuardadaDTO.ObjetoInventarioDTO objeto
                : heroe.inventario) {

            exigir(
                    objeto != null,
                    "Hay una entrada nula en el inventario."
            );

            exigir(
                    objeto.id != null && !objeto.id.isBlank(),
                    "Hay un objeto sin identificador."
            );

            exigir(
                    objeto.cantidad != null && objeto.cantidad > 0,
                    "La cantidad del objeto " + objeto.id + " es inválida."
            );

            exigir(
                    ids.add(objeto.id),
                    "El inventario contiene dos entradas con el ID "
                            + objeto.id + "."
            );
        }

        exigir(
                heroe.efectosActivos != null,
                "Falta la información de los efectos activos."
        );

        for (PartidaGuardadaDTO.EfectoDTO efecto
                : heroe.efectosActivos) {

            exigir(
                    efecto != null,
                    "Hay un efecto nulo."
            );

            exigir(
                    efecto.tipo != null && !efecto.tipo.isBlank(),
                    "Hay un efecto sin tipo."
            );

            exigir(
                    efecto.datos != null,
                    "Faltan los datos de un efecto."
            );
        }
    }

    /**
     * Verifica los planos y las conexiones de la mazmorra.
     */
    private void validarMazmorra(
            PartidaGuardadaDTO.MazmorraDTO mazmorra,
            UbicacionDTO ubicacionHeroe) {

        exigir(
                mazmorra != null,
                "Faltan los datos de la mazmorra."
        );

        exigir(
                mazmorra.pisosPorTablero != null
                        && mazmorra.pisosPorTablero > 0,
                "La cantidad de pisos por tablero es inválida."
        );

        exigir(
                mazmorra.planos != null && !mazmorra.planos.isEmpty(),
                "La mazmorra no contiene planos."
        );

        exigir(
                mazmorra.planos.size() % mazmorra.pisosPorTablero == 0,
                "La cantidad de planos no coincide con los pisos por tablero."
        );

        // Validar todos los planos.
        for (int i = 0; i < mazmorra.planos.size(); i++) {
            validarPlano(mazmorra.planos.get(i), i);
        }

        // El héroe debe encontrarse dentro de un plano existente
        // y sobre una celda transitable.
        validarUbicacion(
                mazmorra,
                ubicacionHeroe,
                "Ubicación del héroe",
                true
        );

        // Validar las conexiones entre los planos.
        validarConexiones(mazmorra);
    }

    /**
     * Verifica un plano, sus dimensiones, terrenos y entidades.
     */
    private void validarPlano(PlanoDTO plano, int indice) {

        String contexto = "Plano " + (indice + 1);

        exigir(
                plano != null,
                contexto + ": el plano es nulo."
        );

        exigir(
                plano.tablero != null,
                contexto + ": falta el tablero."
        );

        TableroDTO tablero = plano.tablero;

        exigir(
                tablero.filas != null && tablero.filas > 0,
                contexto + ": cantidad de filas inválida."
        );

        exigir(
                tablero.columnas != null && tablero.columnas > 0,
                contexto + ": cantidad de columnas inválida."
        );

        exigir(
                tablero.celdas != null
                        && tablero.celdas.size() == tablero.filas,
                contexto + ": la cantidad de filas de celdas no coincide."
        );

        // Validar dimensiones y valores de cada celda.
        for (int f = 0; f < tablero.filas; f++) {

            List<String> fila = tablero.celdas.get(f);

            exigir(
                    fila != null && fila.size() == tablero.columnas,
                    contexto + ": cantidad de columnas incorrecta en fila "
                            + (f + 1) + "."
            );

            for (int c = 0; c < tablero.columnas; c++) {

                String terreno = fila.get(c);

                exigir(
                        terreno != null
                                && TERRENOS_VALIDOS.contains(terreno),
                        contexto + ": terreno desconocido en fila "
                                + (f + 1) + ", columna " + (c + 1) + "."
                );
            }
        }

        // Validar entrada.
        exigir(
                plano.entrada != null,
                contexto + ": falta la entrada."
        );

        validarPosicion(
                tablero,
                plano.entrada,
                contexto + ": entrada"
        );

        // La salida es opcional: algunos planos no la tienen.
        if (plano.salida != null) {

            validarPosicion(
                    tablero,
                    plano.salida,
                    contexto + ": salida"
            );
        }

        // Validar entidades.
        exigir(
                plano.entidades != null,
                contexto + ": falta la lista de entidades."
        );

        for (EntidadDTO entidad : plano.entidades) {

            exigir(
                    entidad != null,
                    contexto + ": hay una entidad nula."
            );

            exigir(
                    entidad.tipo != null && !entidad.tipo.isBlank(),
                    contexto + ": hay una entidad sin tipo."
            );

            exigir(
                    entidad.datos != null,
                    contexto + ": faltan los datos de una entidad."
            );

            validarPosicion(
                    tablero,
                    entidad.posicion,
                    contexto + ": posición de entidad " + entidad.tipo
            );
        }
    }

    /**
     * Verifica que una ubicación corresponda a un plano existente
     * y que su posición esté dentro de sus dimensiones.
     */
    private void validarUbicacion(
            PartidaGuardadaDTO.MazmorraDTO mazmorra,
            UbicacionDTO ubicacion,
            String contexto,
            boolean debeSerTransitable) {

        exigir(
                ubicacion != null,
                contexto + ": ubicación ausente."
        );

        exigir(
                ubicacion.tablero != null && ubicacion.tablero > 0,
                contexto + ": tablero inválido."
        );

        exigir(
                ubicacion.piso != null
                        && ubicacion.piso > 0
                        && ubicacion.piso <= mazmorra.pisosPorTablero,
                contexto + ": piso inválido."
        );

        long indice = ((long) ubicacion.tablero - 1)
                * mazmorra.pisosPorTablero
                + ubicacion.piso - 1;

        exigir(
                indice >= 0 && indice < mazmorra.planos.size(),
                contexto + ": el plano indicado no existe."
        );

        PlanoDTO plano = mazmorra.planos.get((int) indice);

        validarPosicion(
                plano.tablero,
                ubicacion.posicion,
                contexto
        );

        if (debeSerTransitable) {

            exigir(
                    !esPared(plano.tablero, ubicacion.posicion),
                    contexto + ": la ubicación está sobre una pared."
            );
        }
    }

    /**
     * Comprueba las conexiones y verifica que ninguna celda
     * pertenezca a más de una conexión.
     */
    private void validarConexiones(
            PartidaGuardadaDTO.MazmorraDTO mazmorra) {

        exigir(
                mazmorra.conexiones != null,
                "Falta la lista de conexiones."
        );

        Set<String> extremosUtilizados = new HashSet<>();

        for (ConexionDTO conexion : mazmorra.conexiones) {

            exigir(
                    conexion != null,
                    "Hay una conexión nula."
            );

            exigir(
                    conexion.origen != null && conexion.destino != null,
                    "Una conexión necesita origen y destino."
            );

            validarUbicacion(
                    mazmorra,
                    conexion.origen,
                    "Origen de conexión",
                    true
            );

            validarUbicacion(
                    mazmorra,
                    conexion.destino,
                    "Destino de conexión",
                    true
            );

            exigir(
                    !mismoPlano(conexion.origen, conexion.destino),
                    "Una conexión debe unir dos planos distintos."
            );

            String origen = clave(conexion.origen);
            String destino = clave(conexion.destino);

            exigir(
                    extremosUtilizados.add(origen),
                    "Una celda aparece en más de una conexión: " + origen
            );

            exigir(
                    extremosUtilizados.add(destino),
                    "Una celda aparece en más de una conexión: " + destino
            );
        }
    }

    /**
     * Valida los datos básicos de la agenda y los eventos pendientes.
     *
     * Esta representación es provisional y debe ajustarse al modelo
     * definitivo que está desarrollando el compañero.
     */
    private void validarAgenda(AgendaDTO agenda) {

        exigir(
                agenda != null,
                "Faltan los datos de la agenda."
        );

        exigir(
                agenda.paso != null && agenda.paso >= 0,
                "El paso de la agenda es inválido."
        );

        exigir(
                agenda.pendientes != null,
                "Falta la lista de eventos pendientes."
        );

        for (EventoDTO evento : agenda.pendientes) {

            exigir(
                    evento != null,
                    "Hay un evento nulo."
            );

            exigir(
                    evento.paso != null && evento.paso >= 0,
                    "Un evento tiene un paso inválido."
            );

            exigir(
                    evento.orden != null && evento.orden >= 0,
                    "Un evento tiene un orden inválido."
            );

            exigir(
                    evento.tipo != null && !evento.tipo.isBlank(),
                    "Hay un evento sin tipo."
            );

            exigir(
                    evento.datos != null,
                    "Faltan los datos de un evento."
            );
        }
    }

    /**
     * Verifica que una posición esté dentro de las dimensiones del tablero.
     */
    private void validarPosicion(
            TableroDTO tablero,
            PosicionDTO posicion,
            String contexto) {

        exigir(
                posicion != null,
                contexto + ": posición ausente."
        );

        exigir(
                posicion.fila != null
                        && posicion.fila >= 1
                        && posicion.fila <= tablero.filas,
                contexto + ": fila fuera del tablero."
        );

        exigir(
                posicion.columna != null
                        && posicion.columna >= 1
                        && posicion.columna <= tablero.columnas,
                contexto + ": columna fuera del tablero."
        );
    }

    /**
     * Indica si una posición corresponde a una pared.
     */
    private boolean esPared(
            TableroDTO tablero,
            PosicionDTO posicion) {

        return "PARED".equals(
                tablero.celdas.get(posicion.fila - 1)
                        .get(posicion.columna - 1)
        );
    }

    /**
     * Comprueba si dos ubicaciones pertenecen al mismo plano.
     */
    private boolean mismoPlano(
            UbicacionDTO a,
            UbicacionDTO b) {

        return a.tablero.equals(b.tablero)
                && a.piso.equals(b.piso);
    }

    /**
     * Construye una clave para identificar un extremo de conexión.
     */
    private String clave(UbicacionDTO ubicacion) {

        return ubicacion.tablero + ":"
                + ubicacion.piso + ":"
                + ubicacion.posicion.fila + ":"
                + ubicacion.posicion.columna;
    }

    /**
     * Interrumpe la validación cuando se encuentra un dato incorrecto.
     */
    private void exigir(boolean condicion, String mensaje) {

        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
    }
}
