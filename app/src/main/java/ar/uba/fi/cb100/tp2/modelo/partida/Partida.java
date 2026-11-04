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
 * Una partida en curso: junta la mazmorra, el héroe y la cuenta de pasos y
 * cofres, y aplica las reglas del juego (movimiento, costo del terreno,
 * ganar, perder y puntaje).
 */
public class Partida {

    private static final int PUNTOS_POR_COFRE = 100;
    private static final int PUNTOS_POR_ENERGIA = 2;

    private final Mazmorra mazmorra;
    private final Heroe heroe;
    private final Map<Terreno, Integer> costos;
    private final Agenda agenda;
    private int pasos;
    private int cofresAbiertos;

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
        this.cofresAbiertos = 0;
    }

    /** @return cuántos pasos se dieron; las acciones inválidas no cuentan */
    public int getPasos() {
        return this.pasos;
    }

    /** @return cuántos cofres abrió el héroe */
    public int getCofresAbiertos() {
        return this.cofresAbiertos;
    }

    /** Suma un paso. Se llama una vez por cada acción válida del héroe. */
    public void registrarPaso() {
        this.pasos++;
    }

    /** Suma un cofre abierto. */
    public void registrarCofreAbierto() {
        this.cofresAbiertos++;
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
     * suma un paso y avisa a las entidades de esa celda que el héroe llegó.
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
        this.registrarPaso();

        for (Entidad entidad : plano.obtenerEntidades(destino)) {
            entidad.alEncontrarseConHeroe(this.heroe, this.agenda);
        }
        return new ResultadoTurno(true, this.estadoActual());
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
        return calcularPuntaje(this.cofresAbiertos, this.heroe.getEnergia(), this.pasos);
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
}
