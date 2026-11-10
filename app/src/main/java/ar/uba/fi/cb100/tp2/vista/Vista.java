package ar.uba.fi.cb100.tp2.vista;

import ar.uba.fi.cb100.tp2.modelo.partida.Accion;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;

/**
 * Lo que el juego necesita de una vista: mostrar cómo quedó la partida
 * y preguntar qué hace el héroe.
 * VistaBmp escribe un BMP y lee la tecla de la consola; una VistaGrafica
 * haría lo mismo con JuegoVisual. El modelo no se entera del cambio.
 */
public interface Vista {

    /**
     * Muestra la partida tal como quedó después del último paso.
     *
     * @param partida la partida a mostrar
     */
    void mostrar(Partida partida);

    /**
     * Espera a que el jugador elija qué hace el héroe.
     *
     * @return la acción elegida
     */
    Accion leerAccion();

        /**
     * Le avisa algo al jugador, por ejemplo que no se pudo mover.
     *
     * @param mensaje el texto a mostrar
     */
    void avisar(String mensaje);
}
