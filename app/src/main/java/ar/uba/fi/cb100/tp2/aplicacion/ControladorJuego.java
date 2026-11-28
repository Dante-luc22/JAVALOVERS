package ar.uba.fi.cb100.tp2.aplicacion;

import ar.uba.fi.cb100.tp2.modelo.partida.Accion;
import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.partida.ResultadoTurno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.vista.Vista;
import ar.uba.fi.cb100.tp2.persistencia.ServicioPersistenciaPartida;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Corre una partida: pide la acción del jugador, se la aplica a la partida
 * y la dibuja, hasta que se gane, se pierda o el jugador salga.
 * No sabe de teclas ni de imágenes: eso lo resuelve la Vista.
 */
public class ControladorJuego {

    private final Partida partida;
    private final Vista vista;
    private final ServicioPersistenciaPartida servicioPersistencia;
    private final Path rutaGuardado;
    /**
     * @param partida la partida a jugar, ya creada
     * @param vista   dónde se muestra y de dónde se leen las acciones
     */
    /**
     * Constructor compatible con los usos existentes.
     */
    public ControladorJuego(Partida partida, Vista vista) {
        this(partida, vista, new ServicioPersistenciaPartida(), Path.of("partida.json"));
    }

    /**
     * Constructor que permite configurar el servicio de persistencia
     * y la ruta del archivo de guardado.
     */
    public ControladorJuego(Partida partida, Vista vista, ServicioPersistenciaPartida servicioPersistencia, Path rutaGuardado)
    {

        this.partida = Objects.requireNonNull(partida, "La partida no puede ser nula.");

        this.vista = Objects.requireNonNull(vista, "La vista no puede ser nula.");

        this.servicioPersistencia = Objects.requireNonNull(servicioPersistencia, "El servicio de persistencia no puede ser nulo.");

        this.rutaGuardado = Objects.requireNonNull(rutaGuardado, "La ruta de guardado no puede ser nula.");
    }

    /**
     * Juega la partida entera: la muestra, pide una acción, la aplica y la
     * vuelve a mostrar, hasta que se gane, se pierda o el jugador salga.
     * Una acción que no se pudo hacer (chocar una pared) no se muestra de
     * nuevo: no cambió nada.
     *
     * @return cómo terminó: GANADA, PERDIDA, o EN_CURSO si el jugador salió
     */
    public EstadoPartida jugar() {
        this.vista.mostrar(this.partida);
        while (this.partida.estadoActual() == EstadoPartida.EN_CURSO) {
            Accion accion = this.vista.leerAccion();
            if (accion == Accion.SALIR) {
                return this.partida.estadoActual();
            }
// Guardar no consume un turno.
            if (accion == Accion.GUARDAR) {
                this.guardarPartida();
                continue;
            }

            ResultadoTurno resultado = this.ejecutar(accion);
            if (resultado.accionValida()) {
                this.vista.mostrar(this.partida);
            }else {
                this.vista.avisar("No se puede: " + accion + " no hizo nada.");
            }
        }
        return this.partida.estadoActual();
    }

    /**
     * Aplica una acción del jugador a la partida.
     * USAR usa el primer objeto del inventario (todavía no hay menú para elegir);
     * con el inventario vacío no es una acción válida. GUARDAR todavía no hace
     * nada: no cuenta como paso.
     *
     * @param accion lo que eligió el jugador
     * @return si la acción fue válida y cómo quedó la partida
     */
    ResultadoTurno ejecutar(Accion accion) {
        return switch (accion) {
            case MOVER_ARRIBA -> this.partida.moverHeroe(Direccion.ARRIBA);
            case MOVER_ABAJO -> this.partida.moverHeroe(Direccion.ABAJO);
            case MOVER_IZQUIERDA -> this.partida.moverHeroe(Direccion.IZQUIERDA);
            case MOVER_DERECHA -> this.partida.moverHeroe(Direccion.DERECHA);
            case ESPERAR -> this.partida.esperar();
            case INTERACTUAR -> this.partida.interactuar();
            case USAR -> this.partida.getHeroe().getInventario().primerId()
                    .map(this.partida::usarObjeto)
                    .orElse(new ResultadoTurno(false, this.partida.estadoActual()));
            case GUARDAR, SALIR -> new ResultadoTurno(false, this.partida.estadoActual());
        };
    }
    /**
     * Guarda el estado actual de la partida.
     */
    private void guardarPartida() {

        try {
            servicioPersistencia.guardar(partida, rutaGuardado);

            vista.avisar("Partida guardada correctamente en: " + rutaGuardado.toAbsolutePath().normalize());

        } catch (RuntimeException e) {

            vista.avisar("No se pudo guardar la partida: " + e.getMessage());
        }
    }
}
