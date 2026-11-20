package ar.uba.fi.cb100.tp2.modelo.objetos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;

/**
 * Clase abstracta objeto. Algo que el heroe guarda en el inventario y usa
 * cuando quiere con la tecla u, y que cada subtipo define su efecto en
 * usar.
 */
public abstract class Objeto {

    private final String id;

    /**
     * Constructor para crear un objeto con su id
     *
     * @param id (por ejemplo, ZAPATOS_DE_AGUA)
     * @throws IllegalArgumentException si el id es nulo o esta vacio.
     */
    protected Objeto(String id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo.");
        }
        if (id.isBlank()) {
            throw new IllegalArgumentException("El id no puede estar vacio.");
        }
        this.id = id;
    }

    /**
     * Aplica el efecto del objeto al heroe, y la agenda es para los efectos
     * que vencen mas adelante.
     *
     * @param heroe  heroe que usa el objeto.
     * @param agenda la agenda, para efectos que vencen más adelante.
     */
    public abstract void usar(Heroe heroe, Agenda agenda);


    /**
     * @return el id del objeto.
     */
    public String getId() {
        return id;
    }
}
