package ar.uba.fi.cb100.tp2.aplicacion;

import ar.uba.fi.cb100.tp2.modelo.partida.Accion;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.partida.ResultadoTurno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;

/**
 * Corre una partida: pide la acción del jugador, se la aplica a la partida
 * y la dibuja, hasta que se gane, se pierda o el jugador salga.
 * No sabe de teclas ni de imágenes: eso lo resuelve la Vista.
 */
public class ControladorJuego {

    private final Partida partida;

    /**
     * @param partida la partida a jugar, ya creada
     */
    public ControladorJuego(Partida partida) {
        this.partida = partida;
    }

    /**
     * Aplica una acción del jugador a la partida.
     * Usar y guardar todavía no existen en el nivel Fácil: no hacen nada
     * y no cuentan como paso.
     * @param accion lo que pidió el jugador (no SALIR: eso lo maneja el ciclo)
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
