package ar.uba.fi.cb100.tp2.modelo.eventos;

import ar.uba.fi.cb100.tp2.modelo.partida.Partida;

/**
 * La agenda de cosas que pasan en un paso futuro (regla 7 del enunciado):
 * efectos que vencen, trampas que se rearman, monstruos que despiertan.
 * En el nivel Fácil está vacía: existe porque las firmas de Monstruo y
 * Cofre la reciben. En el Hito 3 se le agrega la cola de prioridad.
 */
public class Agenda {

    /**
     * Ejecuta, en orden, los eventos cuyo momento llegó. La Partida la llama al
     * empezar cada paso válido, antes de hacer la acción del héroe.
     * Por ahora no hace nada: la cola de prioridad es del Hito 3 (Owen).
     *
     * @param paso    el número del paso que empieza
     * @param partida la partida sobre la que actúan los eventos
     */
    public void ejecutarLosQueVencen(int paso, Partida partida) {
    }
}
