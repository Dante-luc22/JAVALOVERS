
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

import java.util.ArrayList;
import java.util.List;

/**
 * Reconstruye una mazmorra a partir de su representación DTO.
 *
 * Reconstruye tableros, planos y conexiones.
 * Las entidades se incorporarán mediante un restaurador específico.
 */
public class RestauradorMazmorra {

    /**
     * Reconstruye una mazmorra completa en cuanto a su estructura:
     * planos, tableros y conexiones.
     *
     * Por ahora no reconstruye las entidades dinámicas.
     *
     * @param datos representación de la mazmorra
     * @return mazmorra reconstruida
     */
    public Mazmorra restaurar(MazmorraDTO datos) {

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

            if (!plano.entidades.isEmpty()) {
                throw new UnsupportedOperationException(
                        "Todavía no está implementada la reconstrucción "
                                + "de entidades del plano " + (i + 1)
                                + ". No se puede cargar la mazmorra "
                                + "sin perder su estado."
                );
            }
        }

        List<Plano> planos = new ArrayList<>();

        // Primero reconstruimos todos los planos.
        for (PlanoDTO planoDTO : datos.planos) {
            planos.add(restaurarPlano(planoDTO));
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
     * Reconstruye un plano y su tablero.
     */
    private Plano restaurarPlano(PlanoDTO datos) {

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

        return new Plano(tablero, entrada, salida);
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
