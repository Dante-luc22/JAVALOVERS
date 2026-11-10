package ar.uba.fi.cb100.tp2.aplicacion;

import ar.uba.fi.cb100.tp2.modelo.partida.Accion;
import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.partida.ResultadoTurno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.vista.Vista;

/**
 * Corre una partida: pide la acción del jugador, se la aplica a la partida
 * y la dibuja, hasta que se gane, se pierda o el jugador salga.
 * No sabe de teclas ni de imágenes: eso lo resuelve la Vista.
 */
public class ControladorJuego {

    private final Partida partida;
    private final Vista vista;

    /**
     * @param partida la partida a jugar, ya creada
     * @param vista   dónde se muestra y de dónde se leen las acciones
     */
    public ControladorJuego(Partida partida, Vista vista) {
        this.partida = partida;
        this.vista = vista;
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
            ResultadoTurno resultado = this.ejecutar(accion);
            if (resultado.accionValida()) {
                this.vista.mostrar(this.partida);
            }
        }
        return this.partida.estadoActual();
    }

    /**
     * Aplica una acción del jugador a la partida.
     * USAR y GUARDAR todavía no hacen nada en Fácil: no cuentan como paso.
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
            case USAR, GUARDAR, SALIR -> new ResultadoTurno(false, this.partida.estadoActual());
        };
    }
}