
package ar.uba.fi.cb100.tp2.persistencia;

// Imports del modelo. Ajustar los paquetes según la ubicación
// real de las clases en el proyecto.
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.objetos.Inventario;
import ar.uba.fi.cb100.tp2.modelo.objetos.Objeto;
import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.HeroeDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.ObjetoInventarioDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EfectoDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PosicionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.UbicacionDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Reconstruye un héroe a partir de los datos de una partida guardada.
 *
 * La reconstrucción de objetos y efectos concretos se delega
 * en fábricas que se incorporarán progresivamente.
 */
public class RestauradorHeroe {

    private final Function<String, Objeto> fabricaObjetos;

    private final Function<EfectoDTO, Efecto> fabricaEfectos;

    /**
     * Constructor provisional.
     *
     * Permite restaurar héroes sin objetos ni efectos activos.
     * Si encuentra alguno, informa que todavía falta implementar
     * su reconstrucción.
     */
    public RestauradorHeroe() {

        this(
                id -> {
                    throw new UnsupportedOperationException(
                            "Todavía no se puede reconstruir el objeto: " + id
                    );
                },
                datos -> {
                    throw new UnsupportedOperationException(
                            "Todavía no se puede reconstruir el efecto: "
                                    + datos.tipo
                    );
                }
        );
    }

    /**
     * Constructor que permite incorporar fábricas de objetos y efectos.
     *
     * @param fabricaObjetos reconstruye objetos a partir de su ID
     * @param fabricaEfectos reconstruye efectos a partir de sus datos
     */
    public RestauradorHeroe(
            Function<String, Objeto> fabricaObjetos,
            Function<EfectoDTO, Efecto> fabricaEfectos) {

        this.fabricaObjetos = Objects.requireNonNull(
                fabricaObjetos,
                "La fábrica de objetos no puede ser nula."
        );

        this.fabricaEfectos = Objects.requireNonNull(
                fabricaEfectos,
                "La fábrica de efectos no puede ser nula."
        );
    }

    /**
     * Reconstruye un héroe conservando sus atributos originales.
     *
     * @param datos información guardada del héroe
     * @return héroe reconstruido
     */
    public Heroe restaurar(HeroeDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del héroe no pueden ser nulos."
            );
        }

        // Recuperar la ubicación.
        Ubicacion ubicacion = convertir(datos.ubicacion);

        // Recuperar el inventario.
        Inventario inventario = restaurarInventario(datos.inventario);

        // Recuperar los efectos activos.
        List<Efecto> efectos = restaurarEfectos(datos.efectosActivos);

        // Construir el héroe con todos sus valores originales.
        return new Heroe(
                ubicacion,
                datos.energia,
                datos.energiaMaxima,
                datos.cofresAbiertos,
                inventario,
                efectos
        );
    }

    /**
     * Reconstruye el inventario respetando el orden original.
     */
    private Inventario restaurarInventario(
            List<ObjetoInventarioDTO> datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del inventario no pueden ser nulos."
            );
        }

        Inventario inventario = new Inventario();

        for (ObjetoInventarioDTO entrada : datos) {

            if (entrada == null) {
                throw new IllegalStateException(
                        "El inventario contiene una entrada nula."
                );
            }

            Objeto objeto;

            try {
                objeto = fabricaObjetos.apply(entrada.id);

            } catch (RuntimeException e) {

                throw new IllegalStateException(
                        "No se pudo reconstruir el objeto: " + entrada.id,
                        e
                );
            }

            if (objeto == null) {
                throw new IllegalStateException(
                        "La fábrica devolvió un objeto nulo para: "
                                + entrada.id
                );
            }

            if (!objeto.getId().equals(entrada.id)) {
                throw new IllegalStateException(
                        "El ID reconstruido no coincide con el guardado: "
                                + entrada.id
                );
            }

            inventario.restaurar(objeto, entrada.cantidad);
        }

        return inventario;
    }

    /**
     * Reconstruye los efectos activos del héroe.
     */
    private List<Efecto> restaurarEfectos(
            List<EfectoDTO> datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos de los efectos no pueden ser nulos."
            );
        }

        List<Efecto> efectos = new ArrayList<>();

        for (EfectoDTO efectoDTO : datos) {

            if (efectoDTO == null) {
                throw new IllegalStateException(
                        "La lista contiene un efecto nulo."
                );
            }

            Efecto efecto;

            try {
                efecto = fabricaEfectos.apply(efectoDTO);

            } catch (RuntimeException e) {

                throw new IllegalStateException(
                        "No se pudo reconstruir el efecto: "
                                + efectoDTO.tipo,
                        e
                );
            }

            if (efecto == null) {
                throw new IllegalStateException(
                        "La fábrica devolvió un efecto nulo: "
                                + efectoDTO.tipo
                );
            }

            efectos.add(efecto);
        }

        return efectos;
    }

    /**
     * Convierte una ubicación DTO en una ubicación del modelo.
     */
    private Ubicacion convertir(UbicacionDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "La ubicación no puede ser nula."
            );
        }

        return new Ubicacion(
                datos.tablero,
                datos.piso,
                convertir(datos.posicion)
        );
    }

    /**
     * Convierte una posición DTO en una posición del modelo.
     */
    private Posicion convertir(PosicionDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "La posición no puede ser nula."
            );
        }

        return new Posicion(
                datos.fila,
                datos.columna
        );
    }
}
