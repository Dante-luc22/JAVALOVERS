package ar.uba.fi.cb100.tp2.modelo.eventos;

/**
 * Un evento con el paso en que vence. El orden desempata los que vencen en el
 * mismo paso: se ejecutan en el orden en que se programaron.
 *
 * @param paso   el paso al empezar el cual se ejecuta
 * @param orden  número creciente que asigna la Agenda al programarlo
 * @param evento qué pasa
 */
public record EventoProgramado(int paso, long orden, Evento evento)
        implements Comparable<EventoProgramado> {

    /**
     * @throws IllegalArgumentException si el paso o el orden son negativos o el evento es nulo
     */
    public EventoProgramado {
        if (paso < 0 || orden < 0) {
            throw new IllegalArgumentException("Paso y orden no pueden ser negativos");
        }
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo");
        }
    }

    /**
     * Primero el que vence antes; si vencen en el mismo paso, el que se programó antes.
     */
    @Override
    public int compareTo(EventoProgramado otro) {
        int porPaso = Integer.compare(this.paso, otro.paso);
        if (porPaso != 0) {
            return porPaso;
        }
        return Long.compare(this.orden, otro.orden);
    }
}