package ar.uba.fi.cb100.tp2.modelo.mundo;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Todo lo que está dentro de un plano y reacciona al héroe: monstruos y cofres.
 * Cada tipo decide qué hacer en cada paso y qué pasa cuando el héroe lo
 * encuentra, así el juego nunca necesita preguntar de qué tipo es (sin instanceof).
 */


public abstract class Entidad {

    private Posicion posicion;
    
    /**
     * @param posicion dónde está dentro del plano
     */
    protected Entidad(Posicion posicion) {
        this.posicion = posicion;
    }

    /** @return dónde está dentro del plano */
    public Posicion getPosicion() {
        return this.posicion;
    }

    /**
     * Solo la usan las subclases que se mueven (los monstruos).
     * @param posicion la nueva posición
     */
    protected void setPosicion(Posicion posicion) {
        this.posicion = posicion;
    }

    /**
     * Lo que hace en cada paso del juego: un monstruo se mueve, un cofre no hace nada.
     * @param plano el plano donde está
     * @param heroe el héroe, para saber dónde está
     */
    public abstract void actuar(Plano plano, Heroe heroe);

    /**
     * Lo que pasa cuando el héroe pisa su celda: un cofre se abre,
     * un monstruo lo alcanza.
     * @param heroe  el héroe
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    public abstract void alEncontrarseConHeroe(Heroe heroe, Agenda agenda);

    /**
     * Si la entidad tiene que sacarse del plano después de encontrarse con el
     * héroe. Por defecto no: un monstruo sigue en el plano. Un cofre abierto,
     * en cambio, desaparece (regla 4 del enunciado).
     * La Partida lo pregunta después de cada encuentro, así saca lo que
     * corresponde sin tener que preguntar de qué tipo es cada entidad.
     * @return true si hay que quitarla del plano
     */
    public boolean debeSalirDelPlano() {
        return false;
    }
}



