package ar.uba.fi.cb100.tp2.modelo.partida;

import java.util.EnumMap;
import java.util.Map;
import java.util.Collections;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Una partida en curso: junta la mazmorra, el héroe y la cuenta de pasos, y
 * aplica las reglas del juego (movimiento, costo del terreno, turno de los
 * monstruos, ganar, perder y puntaje). Los cofres abiertos los cuenta el héroe.
 */
public class Partida {

    private static final int PUNTOS_POR_COFRE = 100;
    private static final int PUNTOS_POR_ENERGIA = 2;

    private final Mazmorra mazmorra;
    private final Heroe heroe;
    private final Map<Terreno, Integer> costos;
    private final Agenda agenda;
    private final AzarPersistible azar;

    private int pasos;

    /**
     * Constructor habitual de una partida.
     */
    public Partida(
            Mazmorra mazmorra,
            Heroe heroe,
            Map<Terreno, Integer> costos) {

        this(
                mazmorra,
                heroe,
                costos,
                0,
                new Agenda(),
                new AzarPersistible(0L)
        );
    }

    /**
     * Constructor para crear una partida con su generador aleatorio compartido.
     *
     * @param mazmorra mazmorra de la partida
     * @param heroe héroe de la partida
     * @param costos costos de movimiento
     * @param azar generador aleatorio compartido
     */
    public Partida(
            Mazmorra mazmorra,
            Heroe heroe,
            Map<Terreno, Integer> costos,
            AzarPersistible azar) {

        this(
                mazmorra,
                heroe,
                costos,
                0,
                new Agenda(),
                azar
        );
    }

    /**
     * Constructor de compatibilidad para restaurar partidas
     * que todavía no reciben explícitamente el generador.
     */
    public Partida(
            Mazmorra mazmorra,
            Heroe heroe,
            Map<Terreno, Integer> costos,
            int pasos,
            Agenda agenda) {

        this(
                mazmorra,
                heroe,
                costos,
                pasos,
                agenda,
                new AzarPersistible(0L)
        );
    }

    /**
     * Constructor principal para crear o reconstruir una partida completa.
     *
     * @param mazmorra mazmorra de la partida
     * @param heroe héroe de la partida
     * @param costos costos de movimiento
     * @param pasos cantidad de pasos realizados
     * @param agenda agenda de eventos
     * @param azar generador aleatorio compartido
     */
    public Partida(
            Mazmorra mazmorra,
            Heroe heroe,
            Map<Terreno, Integer> costos,
            int pasos,
            Agenda agenda,
            AzarPersistible azar) {

        if (mazmorra == null || heroe == null || costos == null
                || agenda == null || azar == null) {
            throw new IllegalArgumentException(
                    "Los componentes de la partida no pueden ser nulos."
            );
        }

        if (pasos < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de pasos no puede ser negativa."
            );
        }

        this.mazmorra = mazmorra;
        this.heroe = heroe;

        this.costos = new EnumMap<>(Terreno.class);
        this.costos.putAll(costos);

        this.pasos = pasos;
        this.agenda = agenda;
        this.azar = azar;
    }

    /** @return cuántos pasos se dieron; las acciones inválidas no cuentan */
    public int getPasos() {
        return this.pasos;
    }

    /** @return cuántos cofres abrió el héroe */
    public int getCofresAbiertos() {
        return this.heroe.getCofresAbiertos();
    }

    /** @return el plano donde está el héroe ahora */
    public Plano getPlanoActual() {
        return this.mazmorra.getPlano(this.heroe.getUbicacion());
    }

    /**
     * Devuelve una copia de los costos efectivos de la partida.
     *
     * @return costos asociados a cada terreno
     */
    public Map<Terreno, Integer> getCostos() {
        return Collections.unmodifiableMap(
                new EnumMap<>(this.costos)
        );
    }

    /**
     * Devuelve la agenda de eventos de la partida.
     *
     * @return agenda de la partida
     */
    public Agenda getAgenda() {
        return this.agenda;
    }

    /**
     * Devuelve el generador aleatorio compartido por la partida.
     *
     * Se devuelve siempre la misma instancia para conservar
     * una única secuencia de números aleatorios.
     *
     * @return generador aleatorio de la partida
     */
    public AzarPersistible getAzar() {
        return this.azar;
    }

    /**
     * @param terreno el terreno al que entra el héroe
     * @return cuánta energía cuesta entrar ahí
     * @throws IllegalArgumentException si ese terreno no tiene costo
     */
    public int costoDe(Terreno terreno) {
        Integer costo = this.costos.get(terreno);

        if (costo == null) {
            throw new IllegalArgumentException(
                    "El terreno " + terreno + " no tiene costo"
            );
        }

        return costo;
    }

    /**
     * Mueve al héroe una celda. Si hay una pared o el borde, no hace nada
     * y la acción no cuenta como paso.
     *
     * @param direccion dirección del movimiento
     * @return resultado del turno
     */
    public ResultadoTurno moverHeroe(Direccion direccion) {

        if (this.estadoActual() != EstadoPartida.EN_CURSO) {
            return new ResultadoTurno(false, this.estadoActual());
        }

        Ubicacion actual = this.heroe.getUbicacion();
        Plano plano = this.getPlanoActual();
        Posicion destino = actual.posicion().mover(direccion);

        if (!plano.getTablero().esTransitable(destino)) {
            return new ResultadoTurno(false, this.estadoActual());
        }

        this.heroe.restarEnergia(
                this.costoDe(plano.getTablero().getTerreno(destino))
        );

        this.heroe.mover(
                new Ubicacion(actual.tablero(), actual.piso(), destino)
        );

        this.pasos++;

        for (Entidad entidad : plano.obtenerEntidades(destino)) {
            entidad.alEncontrarseConHeroe(this.heroe, this.agenda);

            if (entidad.debeSalirDelPlano()) {
                plano.quitarEntidad(entidad);
            }
        }

        return this.cerrarTurno();
    }

    /**
     * El héroe se queda donde está: cuenta como un paso,
     * no cuesta energía y los monstruos igual se mueven.
     *
     * @return resultado del turno
     */
    public ResultadoTurno esperar() {

        if (this.estadoActual() != EstadoPartida.EN_CURSO) {
            return new ResultadoTurno(false, this.estadoActual());
        }

        this.pasos++;

        return this.cerrarTurno();
    }

    /** @return si el héroe pisó la salida */
    public boolean estaGanada() {
        return this.estadoActual() == EstadoPartida.GANADA;
    }

    /** @return si al héroe se le acabó la energía */
    public boolean estaPerdida() {
        return this.estadoActual() == EstadoPartida.PERDIDA;
    }

    /** @return el puntaje de la partida hasta ahora */
    public int puntaje() {
        return calcularPuntaje(
                this.heroe.getCofresAbiertos(),
                this.heroe.getEnergia(),
                this.pasos
        );
    }

    /**
     * Cierra el paso del héroe: si la partida sigue,
     * mueven los monstruos.
     */
    private ResultadoTurno cerrarTurno() {

        if (this.estadoActual() == EstadoPartida.EN_CURSO) {
            this.moverMonstruos(this.getPlanoActual());
        }

        return new ResultadoTurno(true, this.estadoActual());
    }

    /**
     * Le da su turno a cada entidad del plano.
     *
     * @param plano plano donde se encuentra el héroe
     */
    private void moverMonstruos(Plano plano) {

        Posicion posicionHeroe =
                this.heroe.getUbicacion().posicion();

        for (Entidad entidad : plano.getEntidades()) {

            Posicion antes = entidad.getPosicion();

            entidad.actuar(plano, this.heroe);

            if (!entidad.getPosicion().equals(antes)
                    && entidad.getPosicion().equals(posicionHeroe)) {

                entidad.alEncontrarseConHeroe(
                        this.heroe,
                        this.agenda
                );
            }
        }
    }

    /**
     * Determina el estado actual de la partida.
     *
     * @return estado actual
     */
    public EstadoPartida estadoActual() {

        if (this.heroe.getUbicacion().equals(this.mazmorra.getSalida())) {
            return EstadoPartida.GANADA;
        }

        if (this.heroe.getEnergia() <= 0) {
            return EstadoPartida.PERDIDA;
        }

        return EstadoPartida.EN_CURSO;
    }

    /**
     * Calcula el puntaje según la regla 9.
     *
     * @param cofresAbiertos cantidad de cofres abiertos
     * @param energiaRestante energía restante
     * @param pasos cantidad de pasos realizados
     * @return puntaje obtenido
     */
    public static int calcularPuntaje(
            int cofresAbiertos,
            int energiaRestante,
            int pasos) {

        return cofresAbiertos * PUNTOS_POR_COFRE
                + energiaRestante * PUNTOS_POR_ENERGIA
                - pasos;
    }

    /** @return el héroe de esta partida */
    public Heroe getHeroe() {
        return this.heroe;
    }

    /** @return la mazmorra de esta partida */
    public Mazmorra getMazmorra() {
        return this.mazmorra;
    }
}