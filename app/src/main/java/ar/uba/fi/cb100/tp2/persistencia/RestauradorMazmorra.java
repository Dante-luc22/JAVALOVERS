
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.MazmorraDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PlanoDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.TableroDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PosicionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.UbicacionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.ConexionDTO;
import ar.uba.fi.cb100.tp2.modelo.partida.AzarPersistible;

import java.util.ArrayList;
import java.util.List;

/**
 * Reconstruye una mazmorra a partir de su representación DTO.
 *
 * Reconstruye tableros, planos, entidades y conexiones.
 */
public class RestauradorMazmorra {

    private final RestauradorEntidad restauradorEntidad =
            new RestauradorEntidad();

    /**
     * Reconstruye una mazmorra sin recibir un generador aleatorio.
     *
     * Se conserva para compatibilidad con pruebas y usos que no
     * necesiten restaurar entidades que consumen valores aleatorios.
     *
     * @param datos representación de la mazmorra
     * @return mazmorra reconstruida
     */
    public Mazmorra restaurar(MazmorraDTO datos) {
        return restaurar(datos, null);
    }

    /**
     * Reconstruye una mazmorra completa, incluyendo sus entidades,
     * utilizando el generador aleatorio recibido.
     *
     * @param datos representación de la mazmorra
     * @param azar generador aleatorio compartido por la partida
     * @return mazmorra reconstruida
     */
    public Mazmorra restaurar(
            MazmorraDTO datos,
            AzarPersistible azar
    ) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos de la mazmorra no pueden ser nulos."
            );
        }

        if (datos.planos == null || datos.planos.isEmpty()) {
            throw new IllegalArgumentException(
                    "La mazmorra debe contener planos."
            );
        }

        // No permitir que se pierdan entidades silenciosamente.
        for (int i = 0; i < datos.planos.size(); i++) {

            PlanoDTO plano = datos.planos.get(i);

            if (plano == null || plano.entidades == null) {
                throw new IllegalStateException(
                        "El plano " + (i + 1)
                                + " no contiene una lista válida de entidades."
                );
            }
        }

        List<Plano> planos = new ArrayList<>();

        // Primero reconstruimos todos los planos y sus entidades.
        for (PlanoDTO planoDTO : datos.planos) {
            planos.add(restaurarPlano(planoDTO, azar));
        }

        // Después construimos la mazmorra con los planos recuperados.
        Mazmorra mazmorra = new Mazmorra(
                planos,
                datos.pisosPorTablero
        );

        // Finalmente reconstruimos las conexiones.
        if (datos.conexiones == null) {
            throw new IllegalArgumentException(
                    "La lista de conexiones no puede ser nula."
            );
        }

        for (ConexionDTO conexion : datos.conexiones) {

            Ubicacion origen = convertir(conexion.origen);
            Ubicacion destino = convertir(conexion.destino);

            mazmorra.conectar(origen, destino);
        }

        return mazmorra;
    }

    /**
     * Reconstruye un plano, su tablero y sus entidades.
     */
    private Plano restaurarPlano(
            PlanoDTO datos,
            AzarPersistible azar
    ) {

        TableroDTO tableroDTO = datos.tablero;

        Tablero tablero = new Tablero(
                tableroDTO.filas,
                tableroDTO.columnas
        );

        // Restaurar cada celda respetando el orden original.
        for (int fila = 0; fila < tableroDTO.filas; fila++) {

            for (int columna = 0;
                 columna < tableroDTO.columnas;
                 columna++) {

                String terrenoGuardado =
                        tableroDTO.celdas.get(fila).get(columna);

                Terreno terreno = Terreno.valueOf(terrenoGuardado);

                Posicion posicion = new Posicion(
                        fila + 1,
                        columna + 1
                );

                tablero.setTerreno(posicion, terreno);
            }
        }

        Posicion entrada = convertir(datos.entrada);

        Posicion salida = datos.salida == null
                ? null
                : convertir(datos.salida);

        Plano plano = new Plano(tablero, entrada, salida);

        // Restaurar las entidades respetando su orden original.
        // Se comparte el mismo generador aleatorio de la partida.
        for (var entidadDTO : datos.entidades) {

            if (entidadDTO == null) {
                throw new IllegalArgumentException(
                        "El plano contiene una entidad nula."
                );
            }

            plano.agregarEntidad(
                    restauradorEntidad.restaurar(entidadDTO, azar)
            );
        }

        return plano;
    }

    /**
     * Convierte una posición DTO en una posición del modelo.
     */
    private Posicion convertir(PosicionDTO datos) {

        return new Posicion(
                datos.fila,
                datos.columna
        );
    }

    /**
     * Convierte una ubicación DTO en una ubicación del modelo.
     */
    private Ubicacion convertir(UbicacionDTO datos) {

        return new Ubicacion(
                datos.tablero,
                datos.piso,
                convertir(datos.posicion)
        );
    }
}