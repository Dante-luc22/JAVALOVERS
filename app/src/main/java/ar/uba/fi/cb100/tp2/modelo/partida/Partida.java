package ar.uba.fi.cb100.tp2.modelo.partida;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;

import ar.uba.fi.cb100.tp2.modelo.efectos.QuitarEfecto;
import ar.uba.fi.cb100.tp2.modelo.efectos.TipoDeEfecto;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.EventoProgramado;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

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
    private Dificultad dificultad;
    private Long semilla;

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

    /**
     * Constructor para crear o restaurar partidas conservando
     * su dificultad y semilla originales.
     */
    public Partida(
            Mazmorra mazmorra,
            Heroe heroe,
            Map<Terreno, Integer> costos,
            int pasos,
            Agenda agenda,
            AzarPersistible azar,
            Dificultad dificultad,
            long semilla) {

        this(mazmorra, heroe, costos, pasos, agenda, azar);

        if (dificultad == null) {
            throw new IllegalArgumentException("La dificultad no puede ser nula.");
        }

        this.dificultad = dificultad;
        this.semilla = semilla;
    }

    /**
     * Constructor para una partida nueva con dificultad y semilla.
     */
    public Partida(
            Mazmorra mazmorra,
            Heroe heroe,
            Map<Terreno, Integer> costos,
            AzarPersistible azar,
            Dificultad dificultad,
            long semilla) {

        this(
                mazmorra,
                heroe,
                costos,
                0,
                new Agenda(),
                azar,
                dificultad,
                semilla
        );
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
    public Dificultad getDificultad() {
        return this.dificultad;
    }

    public Long getSemilla() {
        return this.semilla;
    }


    /**
     * Cuántos pasos faltan para que se le pase el veneno al héroe: recién
     * envenenado por un cofre son 10, y baja de a uno por paso. Se lee de la
     * agenda, donde está anotado cuándo se le quita el efecto. Si se envenenó
     * más de una vez, cuenta hasta el último que se le pasa.
     *
     * @return los pasos que faltan, o 0 si el héroe no está envenenado
     */
    public int pasosParaQueSePaseElVeneno() {
        int pasos = 0;
        for (EventoProgramado programado : this.agenda.getPendientes()) {
            if (programado.evento() instanceof QuitarEfecto quitar
                    && quitar.getTipo() == TipoDeEfecto.ENVENENADO) {
                pasos = Math.max(pasos, programado.paso() - this.agenda.getPasoActual());
            }
        }
        return pasos;
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
     * Mueve al héroe una celda. Si hay una pared (o el borde) no hace nada y la
     * acción no cuenta como paso. Si no, empieza el paso (suma un paso y corre
     * la agenda), cobra el costo del terreno al que entra y avisa a los
     * monstruos de esa celda que el héroe llegó. Los cofres y las escaleras no
     * se activan solos: para eso está {@link #interactuar()}. Si la partida
     * sigue, mueve a los monstruos del plano donde quedó el héroe.
     * Una partida ya terminada no acepta más movimientos.
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

        // Primero empieza el paso: un efecto que vence en este paso ya no
        // cuenta para este movimiento, y los eventos que despiertan
        // monstruos hacen que ya jueguen en este turno.
        this.empezarPaso();

        Terreno terreno = plano.getTablero().getTerreno(destino);
        int costo = this.heroe.ajustarCosto(terreno, this.costoDe(terreno), this.costoDe(Terreno.PISO));
        if (costo > this.costoDe(Terreno.PISO)) {
            // agua, lava o un efecto que encarece el paso: es un daño y se muestra
            this.heroe.restarEnergia(costo);
        } else {
            // caminar por un piso común no se muestra
            this.heroe.restarEnergiaEnSilencio(costo);
        }
        Ubicacion ubicacionNueva = new Ubicacion(actual.tablero(), actual.piso(), destino);
        this.heroe.mover(ubicacionNueva);
        this.encontrarEntidades(plano, destino);
        return this.cerrarTurno();
    }

    /**
     * El jugador interactúa con la celda donde está parado (tecla E). Si hay
     * un cofre cerrado, lo abre. Si no hay cofre pero la celda es una escalera
     * o un pasillo, lleva al héroe al otro extremo, donde se encuentra con los
     * monstruos que haya.
     *
     * Cuenta como un paso y no cuesta energía, como usar un objeto. Si en la
     * celda no hay nada con qué interactuar, la acción no es válida y no
     * cuenta como paso. Una partida ya terminada no la acepta.
     *
     * @return si la acción fue válida y cómo quedó la partida
     */
    public ResultadoTurno interactuar() {
        if (this.estadoActual() != EstadoPartida.EN_CURSO) {
            return new ResultadoTurno(false, this.estadoActual());
        }
        Ubicacion actual = this.heroe.getUbicacion();
        Plano plano = this.getPlanoActual();
        boolean hayCofre = this.hayAlgoParaInteractuar(plano, actual.posicion());
        Optional<Ubicacion> destino = this.mazmorra.destinoDesde(actual);
        if (!hayCofre && destino.isEmpty()) {
            return new ResultadoTurno(false, this.estadoActual());
        }

        this.empezarPaso();
        if (hayCofre) {
            this.activarAlInteractuar(plano, actual.posicion());
        } else {
            this.heroe.mover(destino.get());
            this.encontrarEntidades(this.getPlanoActual(), destino.get().posicion());
        }
        return this.cerrarTurno();
    }

    /**
     * @return si el héroe tiene algo con qué interactuar donde está: un cofre
     *         cerrado o una escalera o pasillo
     */
    public boolean puedeInteractuar() {
        return this.interaccionDisponible() != Interaccion.NINGUNA;
    }

    /**
     * Qué pasaría si el jugador interactúa ahora, para que la vista se lo
     * avise. Si hay un cofre, se abre el cofre; si no, se usa la escalera o
     * el pasillo. Una partida terminada no admite ninguna.
     *
     * @return la interacción disponible donde está parado el héroe
     */
    public Interaccion interaccionDisponible() {
        if (this.estadoActual() != EstadoPartida.EN_CURSO) {
            return Interaccion.NINGUNA;
        }
        Ubicacion actual = this.heroe.getUbicacion();
        if (this.hayAlgoParaInteractuar(this.getPlanoActual(), actual.posicion())) {
            return Interaccion.ABRIR_COFRE;
        }
        Optional<Ubicacion> destino = this.mazmorra.destinoDesde(actual);
        if (destino.isEmpty()) {
            return Interaccion.NINGUNA;
        }
        return destino.get().tablero() == actual.tablero()
                ? Interaccion.USAR_ESCALERA
                : Interaccion.USAR_PASILLO;
    }

    /**
     * El héroe se queda donde está: cuenta como un paso, no cuesta energía y
     * los monstruos igual se mueven. Una partida ya terminada no lo acepta.
     *
     * @return resultado del turno
     */
    public ResultadoTurno esperar() {

        if (this.estadoActual() != EstadoPartida.EN_CURSO) {
            return new ResultadoTurno(false, this.estadoActual());
        }
        this.empezarPaso();
        this.heroe.sumarEnergia(1);
        return this.cerrarTurno();
    }

    /**
     * Usa un objeto del inventario como la acción del paso: cuenta como un paso,
     * no cuesta energía y los monstruos igual se mueven. Si el héroe no tiene
     * ese objeto la acción no es válida y no cuenta como paso.
     *
     * @param id el id del objeto a usar (por ejemplo ZAPATOS_DE_AGUA)
     * @return si la acción fue válida y cómo quedó la partida
     */
    public ResultadoTurno usarObjeto(String id) {
        if (this.estadoActual() != EstadoPartida.EN_CURSO
                || id == null || id.isBlank()
                || !this.heroe.getInventario().contiene(id)) {
            return new ResultadoTurno(false, this.estadoActual());
        }

        this.empezarPaso();
        this.heroe.getInventario().usar(id, this.heroe, this.agenda);
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
     * Arranca un paso válido: lo cuenta y ejecuta los eventos de la agenda
     * cuyo momento llegó. Se llama después de validar la acción y antes de
     * hacerla.
     */
    private void empezarPaso() {
        this.pasos++;
        this.agenda.ejecutarLosQueVencen(this.pasos, this);
    }

    /**
     * Avisa a las entidades de una celda que el héroe llegó. Solo a las que se
     * activan al pisarlas (los monstruos): los cofres esperan a que el jugador
     * interactúe.
     */
    private void encontrarEntidades(Plano plano, Posicion posicion) {
        for (Entidad entidad : plano.obtenerEntidades(posicion)) {
            if (!entidad.seActivaAlInteractuar()) {
                this.activar(plano, entidad);
            }
        }
    }

    /**
     * Activa las entidades de la celda que esperaban una interacción (los
     * cofres).
     */
    private void activarAlInteractuar(Plano plano, Posicion posicion) {
        for (Entidad entidad : plano.obtenerEntidades(posicion)) {
            if (entidad.seActivaAlInteractuar()) {
                this.activar(plano, entidad);
            }
        }
    }

    /**
     * @return si en la celda hay alguna entidad que espera una interacción
     */
    private boolean hayAlgoParaInteractuar(Plano plano, Posicion posicion) {
        for (Entidad entidad : plano.obtenerEntidades(posicion)) {
            if (entidad.seActivaAlInteractuar()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Le avisa a una entidad que se encontró con el héroe y la saca del plano
     * si ya cumplió su papel (un cofre abierto).
     */
    private void activar(Plano plano, Entidad entidad) {
        entidad.alEncontrarseConHeroe(this.heroe, this.agenda);
        if (entidad.debeSalirDelPlano()) {
            plano.quitarEntidad(entidad);
        }
    }

    /**
     * Cierra el paso del héroe: si la partida sigue, mueven los monstruos.
     * Si el héroe ganó o perdió en este paso, los monstruos no juegan.
     */
    private ResultadoTurno cerrarTurno() {

        if (this.estadoActual() == EstadoPartida.EN_CURSO) {
            this.moverMonstruos(this.getPlanoActual());
        }

        return new ResultadoTurno(true, this.estadoActual());
    }

    /**
     * Le da su turno a cada entidad del plano. Una entidad que se movió y
     * quedó en la celda del héroe lo alcanzó. Las que no se movieron (los
     * cofres) no se encuentran con el héroe: un cofre se abre solo cuando el
     * jugador interactúa. Solo juega el plano donde está el héroe, porque los
     * monstruos nunca cambian de plano.
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
