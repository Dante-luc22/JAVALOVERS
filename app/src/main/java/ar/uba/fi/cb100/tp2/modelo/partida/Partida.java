package ar.uba.fi.cb100.tp2.modelo.partida;

import java.util.EnumMap;
import java.util.Map;

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
    private int pasos;

    /**
     * @param mazmorra todos los planos del juego
     * @param heroe    el héroe, ya puesto en la entrada
     * @param costos   cuánta energía cuesta entrar a cada terreno (sale de la configuración)
     */
    public Partida(Mazmorra mazmorra, Heroe heroe, Map<Terreno, Integer> costos) {
        this.mazmorra = mazmorra;
        this.heroe = heroe;
        this.costos = new EnumMap<>(Terreno.class);
        this.costos.putAll(costos);
        this.agenda = new Agenda();
        this.pasos = 0;
    }

    /** @return cuántos pasos se dieron; las acciones inválidas no cuentan */
    public int getPasos() {
        return this.pasos;
    }

    /** @return cuántos cofres abrió el héroe (los cuenta el propio héroe al abrirlos) */
    public int getCofresAbiertos() {
        return this.heroe.getCofresAbiertos();
    }

    /** @return el plano donde está el héroe ahora */
    public Plano getPlanoActual() {
        return this.mazmorra.getPlano(this.heroe.getUbicacion());
    }

    /**
     * @param terreno el terreno al que entra el héroe
     * @return cuánta energía cuesta entrar ahí
     * @throws IllegalArgumentException si ese terreno no tiene costo (la pared no se pisa)
     */
    public int costoDe(Terreno terreno) {
        Integer costo = this.costos.get(terreno);
        if (costo == null) {
            throw new IllegalArgumentException("El terreno " + terreno + " no tiene costo");
        }
        return costo;
    }

    /**
     * Mueve al héroe una celda. Si hay una pared (o el borde) no hace nada y la
     * acción no cuenta como paso. Si no, cobra el costo del terreno al que entra,
     * suma un paso, avisa a las entidades de esa celda que el héroe llegó y,
     * si la partida sigue, mueve a los monstruos.
     * Una partida ya terminada no acepta más movimientos.
     *
     * @param direccion hacia dónde se mueve
     * @return si la acción fue válida y cómo quedó la partida
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

        this.heroe.restarEnergia(this.costoDe(plano.getTablero().getTerreno(destino)));
        this.heroe.mover(new Ubicacion(actual.tablero(), actual.piso(), destino));
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
     * El héroe se queda donde está: cuenta como un paso, no cuesta energía y
     * los monstruos igual se mueven. Una partida ya terminada no lo acepta.
     *
     * @return si la acción fue válida y cómo quedó la partida
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

    /** @return si al héroe se le acabó la energía sin llegar a la salida */
    public boolean estaPerdida() {
        return this.estadoActual() == EstadoPartida.PERDIDA;
    }

    /** @return el puntaje de la partida hasta ahora */
    public int puntaje() {
        return calcularPuntaje(this.heroe.getCofresAbiertos(), this.heroe.getEnergia(), this.pasos);
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
     * cofres) no se vuelven a encontrar con el héroe: eso ya pasó cuando
     * él las pisó. Solo juega el plano donde está el héroe, porque los
     * monstruos nunca cambian de plano.
     */
    private void moverMonstruos(Plano plano) {
        Posicion posicionHeroe = this.heroe.getUbicacion().posicion();
        for (Entidad entidad : plano.getEntidades()) {
            Posicion antes = entidad.getPosicion();
            entidad.actuar(plano, this.heroe);
            if (!entidad.getPosicion().equals(antes) && entidad.getPosicion().equals(posicionHeroe)) {
                entidad.alEncontrarseConHeroe(this.heroe, this.agenda);
            }
        }
    }

    /**
     * Pisar la salida gana aunque ese último paso haya dejado la energía en 0:
     * por eso se mira primero la salida.
     */
    private EstadoPartida estadoActual() {
        if (this.heroe.getUbicacion().equals(this.mazmorra.getSalida())) {
            return EstadoPartida.GANADA;
        }
        if (this.heroe.getEnergia() <= 0) {
            return EstadoPartida.PERDIDA;
        }
        return EstadoPartida.EN_CURSO;
    }

    /**
     * Puntaje según la regla 9: cofres abiertos × 100 + energía restante × 2 - pasos.
     * Es estática porque no usa el estado de ninguna partida: solo hace la cuenta
     * con los números que recibe, y así se puede probar sola.
     *
     * @param cofresAbiertos  cuántos cofres se abrieron
     * @param energiaRestante la energía que le queda al héroe
     * @param pasos           cuántos pasos se dieron
     * @return el puntaje, que puede ser negativo si se dieron muchos pasos
     */
    public static int calcularPuntaje(int cofresAbiertos, int energiaRestante, int pasos) {
        return cofresAbiertos * PUNTOS_POR_COFRE + energiaRestante * PUNTOS_POR_ENERGIA - pasos;
    }

        /**
     * @return el héroe de esta partida, para que la vista lo pueda dibujar
     */
    public Heroe getHeroe() {
        return this.heroe;
    }
}
