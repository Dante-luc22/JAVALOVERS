package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

public abstract class Monstruo extends Entidad {

    private final Posicion posicionInicial;
    private final int danio;
    private int adormecimientos = 0;

    /**
     * Constructor de clase Monstruo, recibe su posicion y danio inicial
     *
     * @param posicionInicial posicion donde empieza el monstruo
     * @param danio           danio predeterminado que hace un monstruo
     * @throws IllegalArgumentException si la posicion inicial es nula y
     *                                  si el danio es menor o igual a 0.
     */
    protected Monstruo(Posicion posicionInicial, int danio) {
        if (posicionInicial == null) {
            throw new IllegalArgumentException(
                    "La posicion inicial no puede ser nula");
        }
        if (danio <= 0) {
            throw new IllegalArgumentException(
                    "El danio debe ser mayor que cero, se recibio: " + danio);
        }
        super(posicionInicial);
        this.posicionInicial = posicionInicial;
        this.danio = danio;
    }


    /**
     * Metodo abstracto que hereda cada monstruo, decide el movimiento
     * que realiza el monstruo basándonos en el heroe.
     *
     * @param plano lugar del plano donde se va a mover;
     * @param heroe posicion del heroe;
     * @return la posicion a la que se va a mover el monstruo. Nunca devuelve
     * null y puede devolver la misma ubicacion si el monstruo esta quieto;
     */
    protected abstract Posicion decidirMovimiento(Plano plano, Posicion heroe);


    /**
     * Metodo abstacto que hereda cada monstruo, decide que pasa cuando
     * un monstruo alcanza a al heroe.
     *
     * @param heroe  heroe que alcanza
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    protected abstract void alAlcanzarAlHeroe(Heroe heroe, Agenda agenda);


    /**
     * Metodo que le dice al monstruo que ataque y vuelva a su posicion inicial.
     *
     * @param heroe heroe a atacar
     * @throws IllegalArgumentException si el heroe dado es nulo
     */
    protected void atacarYVolver(Heroe heroe) {
        if (heroe == null) {
            throw new IllegalArgumentException("El heroe no puede ser nulo");
        }
        heroe.restarEnergia(this.danio);
        volverAlInicio();
    }


    /**
     * Lo que hace en cada paso del juego, en este caso el monstruo
     * se mueve. Si el monstruo esta dormido, no hace nada (no ataca
     * ni se mueve).
     *
     * @param plano el plano donde está
     * @param heroe el héroe, para saber dónde está
     */
    @Override
    public final void actuar(Plano plano, Heroe heroe) {
        if (plano == null) {
            throw new IllegalArgumentException(
                    "El plano no puede ser nulo");
        }
        if (heroe == null) {
            throw new IllegalArgumentException(
                    "El heroe no puede ser nulo");
        }
        if (estaAdormecido()) {
            return;
        }
        Posicion posicionHeroe = heroe.getUbicacion().posicion();
        Posicion nuevaPosicion = decidirMovimiento(plano, posicionHeroe);
        if (nuevaPosicion == null) {
            throw new IllegalStateException(
                    "Se recibio una posicion nula de: " + getClass().getSimpleName());
        }
        setPosicion(nuevaPosicion);
    }


    /**
     * Lo que pasa cuando el héroe pisa su celda, en este caso
     * el monstruo lo alcanza. Si el monstruo esta dormido,
     * al pasar por encima el heroe lo despierta del todo.
     * Si el heroe tiene escudo, el monstruo no le hace nada, el heroe
     * gasta escudo y el monstruo vuelve al inicio.
     *
     * @param heroe  el héroe
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    public final void alEncontrarseConHeroe(Heroe heroe, Agenda agenda) {
        if (heroe == null) {
            throw new IllegalArgumentException(
                    "El heroe recibido no puede ser nulo");
        }
        if (estaAdormecido()) {
            despertarDelTodo();
            return;
        }
        if (heroe.tieneEscudo()) {
            heroe.usarEscudo();
            volverAlInicio();
            return;
        }
        alAlcanzarAlHeroe(heroe, agenda);
    }

    /**
     * Devuelve el monstruo a su posicion inicial.
     */
    protected void volverAlInicio() {
        setPosicion(posicionInicial);
    }


    /**
     * Manda al monstruo de vuelta a su posicion inicial porque lo repelio
     * un efecto externo (por ejemplo, el cofre de repelente). No
     * le hace nada al heroe.
     */
    public final void repeler() {
        volverAlInicio();
    }


    /**
     * Metodo que suma 1 al contador de adormecimientos.
     */
    public final void adormecer() {
        adormecimientos++;
    }

    /**
     * Si los adormecimientos son mayores a 0, le resta 1. No
     * baja de 0 ya que puede haver despertado antes, si el heroe
     * lo piso y el evento de despertar de la agenda llega igual.
     */
    public final void despertar() {
        if (adormecimientos > 0) {
            adormecimientos--;
        }
    }

    /**
     * Despierta del Todo a un monstruo.
     */
    private void despertarDelTodo() {
        adormecimientos = 0;
    }

    /**
     * @return true si esta adormecido el monstruo
     */
    public final boolean estaAdormecido() {
        return adormecimientos > 0;
    }

    // ================ GETTERS SIMPLES ================= //

    /**
     * Devuelve la posicion inicial del monstruo;
     */
    public Posicion getPosicionInicial() {
        return posicionInicial;
    }

    /**
     * Devuelve el danio que realiza el monstruo.
     */
    public int getDanio() {
        return danio;
    }

    @Override
    public final char getSimbolo() {
        return 'M';
    }
}
