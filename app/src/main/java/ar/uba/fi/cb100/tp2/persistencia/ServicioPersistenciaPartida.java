package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.partida.Partida;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Coordina el guardado y la carga de partidas completas.
 *
 * Delega el mapeo, la escritura, la lectura y la restauración
 * en sus respectivos componentes especializados.
 */
public class ServicioPersistenciaPartida {

    private final MapeadorPartida mapeador;
    private final EscritorPartida escritor;
    private final LectorPartida lector;
    private final RestauradorPartida restaurador;

    /**
     * Construye el servicio con los componentes reales del proyecto.
     */
    public ServicioPersistenciaPartida() {

        this(
                new MapeadorPartida(
                        new MapeadorHeroe(),
                        new MapeadorMazmorra(),
                        new MapeadorAgenda()::mapear
                ),
                new EscritorPartida(),
                new LectorPartida(),
                new RestauradorPartida(
                        new RestauradorMazmorra(),
                        new RestauradorHeroe(),
                        new RestauradorAgenda()::restaurar
                )
        );
    }

    /**
     * Constructor que permite inyectar los componentes.
     */
    public ServicioPersistenciaPartida(
            MapeadorPartida mapeador,
            EscritorPartida escritor,
            LectorPartida lector,
            RestauradorPartida restaurador
    ) {

        this.mapeador = Objects.requireNonNull(
                mapeador,
                "El mapeador no puede ser nulo."
        );

        this.escritor = Objects.requireNonNull(
                escritor,
                "El escritor no puede ser nulo."
        );

        this.lector = Objects.requireNonNull(
                lector,
                "El lector no puede ser nulo."
        );

        this.restaurador = Objects.requireNonNull(
                restaurador,
                "El restaurador no puede ser nulo."
        );
    }

    /**
     * Guarda una partida en formato JSON.
     *
     * @param partida partida actual
     * @param destino ubicación del archivo JSON
     */
    public void guardar(Partida partida, Path destino) {

        if (partida == null) {
            throw new IllegalArgumentException(
                    "La partida no puede ser nula."
            );
        }

        if (destino == null) {
            throw new IllegalArgumentException(
                    "La ruta de guardado no puede ser nula."
            );
        }

        var datos = mapeador.mapear(partida);

        escritor.guardar(datos, destino);
    }

    /**
     * Lee un archivo JSON y reconstruye la partida.
     *
     * @param origen ubicación del archivo JSON
     * @return partida reconstruida
     */
    public Partida cargar(Path origen) {

        if (origen == null) {
            throw new IllegalArgumentException(
                    "La ruta de carga no puede ser nula."
            );
        }

        var datos = lector.leer(origen);

        return restaurador.restaurar(datos);
    }
}