package ar.uba.fi.cb100.tp2.modelo.eventos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import ar.uba.fi.cb100.tp2.modelo.partida.Partida;

/**
 * La agenda de cosas que pasan en un paso futuro (regla 7): efectos que
 * vencen, trampas que se rearman, monstruos que despiertan.
 *
 * Es una cola de prioridad (heap): siempre se necesita el próximo que vence,
 * nunca la lista entera ordenada. Si varios vencen en el mismo paso, se
 * ejecutan en el orden en que se programaron.
 */
public class Agenda {

    private final PriorityQueue<EventoProgramado> pendientes = new PriorityQueue<>();
    private int pasoActual = 0;
    private long siguienteOrden = 0;

    /**
     * Anota un evento para dentro de unos pasos.
     *
     * @param dentroDePasos 1 = al empezar el próximo paso, 2 = el siguiente, etc.
     * @param evento        qué tiene que pasar
     * @throws IllegalArgumentException si dentroDePasos es menor que 1 o el evento es nulo
     */
    public void programar(int dentroDePasos, Evento evento) {
        if (dentroDePasos < 1) {
            throw new IllegalArgumentException("Un evento se programa para 1 paso o más adelante: " + dentroDePasos);
        }
        this.pendientes.add(new EventoProgramado(this.pasoActual + dentroDePasos, this.siguienteOrden, evento));
        this.siguienteOrden++;
    }

    /**
     * Ejecuta, en orden, todos los eventos cuyo paso ya llegó. Se llama al
     * empezar cada paso de la partida.
     *
     * @param paso    el paso que empieza
     * @param partida la partida, que se le pasa a cada evento
     * @throws IllegalArgumentException si el paso es anterior al último ejecutado
     */
    public void ejecutarLosQueVencen(int paso, Partida partida) {
        if (paso < this.pasoActual) {
            throw new IllegalArgumentException("La agenda no vuelve atrás: estaba en " + this.pasoActual + " y llegó " + paso);
        }
        this.pasoActual = paso;
        while (!this.pendientes.isEmpty() && this.pendientes.peek().paso() <= paso) {
            this.pendientes.poll().evento().ejecutar(partida);
        }
    }

    /**
     * @return los eventos que faltan, en el orden en que se van a ejecutar.
     *         Es una copia: para guardar la partida.
     */
    public List<EventoProgramado> getPendientes() {
        List<EventoProgramado> enOrden = new ArrayList<>(this.pendientes);
        Collections.sort(enOrden);
        return enOrden;
    }

    /**
     * @return el último paso que se ejecutó
     */
    public int getPasoActual() {
        return this.pasoActual;
    }

    /**
     * Vuelve a armar la agenda de una partida guardada: mismo paso y mismos
     * pendientes, con su orden original para que los empates salgan igual.
     *
     * @param pasoActual el paso en que estaba
     * @param pendientes los eventos que faltaban, como los dio getPendientes()
     */
    public void restaurar(int pasoActual, List<EventoProgramado> pendientes) {
        this.pendientes.clear();
        this.pasoActual = pasoActual;
        this.siguienteOrden = 0;
        for (EventoProgramado evento : pendientes) {
            this.pendientes.add(evento);
            this.siguienteOrden = Math.max(this.siguienteOrden, evento.orden() + 1);
        }
    }
}