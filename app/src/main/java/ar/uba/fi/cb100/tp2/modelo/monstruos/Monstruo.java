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
     * Constructor habitual: el monstruo comienza en su posición inicial.
     *
     * @param posicionInicial posición donde comienza el monstruo
     * @param danio daño predeterminado que realiza el monstruo
     */
    protected Monstruo(Posicion posicionInicial, int danio) {
        this(posicionInicial, posicionInicial, danio);
    }

    /**
     * Constructor de restauración: permite recuperar al monstruo
     * en una posición distinta de aquella donde comenzó.
     *
     * @param posicionInicial posición original del monstruo
     * @param posicionActual posición que tenía al guardar la partida
     * @param danio daño que realiza
     */
    protected Monstruo(
            Posicion posicionInicial,
            Posicion posicionActual,
            int danio) {

        super(posicionActual);

        if (posicionInicial == null) {
            throw new IllegalArgumentException(
                    "La posición inicial no puede ser nula."
            );
        }

        if (posicionActual == null) {
            throw new IllegalArgumentException(
                    "La posición actual no puede ser nula."
            );
        }

        if (danio <= 0) {
            throw new IllegalArgumentException(
                    "El daño debe ser mayor que cero."
            );
        }

        this.posicionInicial = posicionInicial;
        this.danio = danio;
    }

    /**
     * Método abstracto que hereda cada monstruo. Decide el movimiento
     * que realiza el monstruo basándose en el héroe.
     *
     * @param plano lugar del plano donde se va a mover el monstruo
     * @param heroe posición del héroe
     * @return posición a la que se va a mover el monstruo
     */
    protected abstract Posicion decidirMovimiento(
            Plano plano,
            Posicion heroe
    );

    /**
     * Método abstracto que hereda cada monstruo. Decide qué pasa cuando
     * un monstruo alcanza al héroe.
     *
     * @param heroe héroe que alcanza
     * @param agenda agenda para efectos que vencen más adelante
     */
    protected abstract void alAlcanzarAlHeroe(
            Heroe heroe,
            Agenda agenda
    );

    /**
     * Ataca al héroe y vuelve a su posición inicial.
     *
     * @param heroe héroe a atacar
     * @throws IllegalArgumentException si el héroe dado es nulo
     */
    protected void atacarYVolver(Heroe heroe) {
        if (heroe == null) {
            throw new IllegalArgumentException(
                    "El heroe no puede ser nulo"
            );
        }

        heroe.restarEnergia(this.danio);
        volverAlInicio();
    }

    /**
     * Lo que hace en cada paso del juego. El monstruo se mueve,
     * salvo que esté dormido.
     *
     * @param plano el plano donde está
     * @param heroe el héroe, para saber dónde está
     */
    @Override
    public final void actuar(Plano plano, Heroe heroe) {
        if (plano == null) {
            throw new IllegalArgumentException(
                    "El plano no puede ser nulo"
            );
        }

        if (heroe == null) {
            throw new IllegalArgumentException(
                    "El heroe no puede ser nulo"
            );
        }

        if (estaAdormecido()) {
            return;
        }

        Posicion posicionHeroe = heroe.getUbicacion().posicion();
        Posicion nuevaPosicion = decidirMovimiento(plano, posicionHeroe);

        if (nuevaPosicion == null) {
            throw new IllegalStateException(
                    "Se recibio una posicion nula de: "
                            + getClass().getSimpleName()
            );
        }

        setPosicion(nuevaPosicion);
    }

    /**
     * Lo que pasa cuando el héroe pisa la celda del monstruo.
     * Si el monstruo está dormido, el héroe lo despierta del todo.
     * Si el héroe tiene escudo, el monstruo no le hace nada:
     * el héroe gasta el escudo y el monstruo vuelve al inicio.
     *
     * @param heroe el héroe
     * @param agenda la agenda para efectos que vencen más adelante
     */
    @Override
    public final void alEncontrarseConHeroe(Heroe heroe, Agenda agenda) {
        if (heroe == null) {
            throw new IllegalArgumentException(
                    "El heroe recibido no puede ser nulo"
            );
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
     * Devuelve el monstruo a su posición inicial.
     */
    protected void volverAlInicio() {
        setPosicion(posicionInicial);
    }

    /**
     * Manda al monstruo de vuelta a su posición inicial porque lo repelió
     * un efecto externo. No le hace nada al héroe.
     */
    @Override
    public final void repeler() {
        volverAlInicio();
    }

    /**
     * Suma uno al contador de adormecimientos.
     */
    @Override
    public final void adormecer() {
        adormecimientos++;
    }

    /**
     * Si los adormecimientos son mayores a cero, les resta uno.
     * No baja de cero porque puede haber despertado antes.
     */
    @Override
    public final void despertar() {
        if (adormecimientos > 0) {
            adormecimientos--;
        }
    }

    /**
     * Despierta del todo a un monstruo.
     */
    private void despertarDelTodo() {
        adormecimientos = 0;
    }

    /**
     * @return true si el monstruo está adormecido
     */
    public final boolean estaAdormecido() {
        return adormecimientos > 0;
    }

    // ================ GETTERS SIMPLES ================= //

    /**
     * Devuelve la posición inicial del monstruo.
     *
     * @return posición inicial
     */
    public Posicion getPosicionInicial() {
        return posicionInicial;
    }

    /**
     * Devuelve el daño que realiza el monstruo.
     *
     * @return daño
     */
    public int getDanio() {
        return danio;
    }

    @Override
    public final char getSimbolo() {
        return 'M';
    }

    /**
     * Cada tipo de monstruo indica cuál es.
     *
     * @return el tipo de este monstruo
     */
    public abstract TipoDeMonstruo getTipoDeMonstruo();

    /**
     * @return el nombre del tipo de este monstruo, por ejemplo "CAZADOR"
     */
    @Override
    public final String getTipo() {
        return this.getTipoDeMonstruo().name();
    }
}