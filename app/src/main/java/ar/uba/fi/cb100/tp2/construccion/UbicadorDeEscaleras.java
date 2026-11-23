package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

/**
 * <hr><hr>
 * <h1>Ubicación de escaleras entre planos</h1>
 * <hr><hr>
 *
 * Busca una posición interior común para conectar dos planos mediante
 * una escalera. Excluye entradas, salidas, entidades y posiciones
 * reservadas. Entre las candidatas válidas, prioriza las que no son
 * vecinas ortogonales de una posición reservada. Si todas son vecinas,
 * elige entre ellas.
 *
 * El sorteo utiliza el {@code Random} recibido por constructor. Al
 * reconstruir la mazmorra con la misma semilla, se pueden reproducir
 * las posiciones de las escaleras.
 *
 * <hr>
 * <ul>
 *   <li>{@code buscarPosicionComun} → Sortea una posición transitable
 *       y libre en ambos planos, dentro de sus dimensiones compartidas.
 *       La variante interna permite excluir posiciones reservadas.</li>
 *   <li>{@code prepararPosicionComun} → Busca primero una posición común.
 *       Si no encuentra ninguna, sortea una posición donde pueda abrir
 *       una pared y convierte únicamente esa pared en piso.</li>
 *   <li>{@code elegirPosicion} → Reúne las posiciones válidas y sortea
 *       una. Si hay candidatas separadas de las reservadas, limita el
 *       sorteo a esas candidatas.</li>
 *   <li>{@code esVecinaDeReservada} → Comprueba si alguna posición
 *       reservada está arriba, abajo, a la izquierda o a la derecha
 *       de la candidata.</li>
 *   <li>{@code estaDisponible} → Comprueba que una posición sea
 *       transitable y esté libre.</li>
 *   <li>{@code estaLibre} → Verifica que la posición no sea entrada
 *       ni salida y que no contenga entidades.</li>
 *   <li>{@code puedeAbrirPared} → Comprueba que haya piso en un plano
 *       y pared en el otro, que la posición esté libre en ambos y
 *       que la pared tenga una vecina transitable.</li>
 * </ul>
 * <hr>
 *
 * Devuelve {@code Optional.empty()} si no encuentra una posición
 * adecuada. El ubicador prepara la celda y {@code ConectorDeEscaleras}
 * registra la conexión entre los pisos.
 */
public class UbicadorDeEscaleras {

    private final Random azar;

    public UbicadorDeEscaleras(Random azar) {
        this.azar = azar;
    }

    public Optional<Posicion> buscarPosicionComun(Plano inferior, Plano superior) {
        return buscarPosicionComun(inferior, superior, Set.of());
    }

    public Optional<Posicion> prepararPosicionComun(Plano inferior, Plano superior) {
        return prepararPosicionComun(inferior, superior, Set.of());
    }

    public Optional<Posicion> prepararPosicionComun(
            Plano inferior, Plano superior, Set<Posicion> reservadas) {

        Optional<Posicion> comun = buscarPosicionComun(inferior, superior, reservadas);
        if (comun.isPresent()) {
            return comun;
        }

        Optional<Posicion> apertura = elegirPosicion(inferior, superior, reservadas,
                posicion -> puedeAbrirPared(inferior, superior, posicion)
                        || puedeAbrirPared(superior, inferior, posicion));

        apertura.ifPresent(posicion -> {
            Tablero tableroConPared = inferior.getTablero().getTerreno(posicion) == Terreno.PARED
                    ? inferior.getTablero() : superior.getTablero();
            tableroConPared.setTerreno(posicion, Terreno.PISO);
        });
        return apertura;
    }

    private Optional<Posicion> buscarPosicionComun(
            Plano inferior, Plano superior, Set<Posicion> reservadas) {

        return elegirPosicion(inferior, superior, reservadas,
                posicion -> estaDisponible(inferior, posicion)
                        && estaDisponible(superior, posicion));
    }

    private Optional<Posicion> elegirPosicion(
            Plano inferior, Plano superior, Set<Posicion> reservadas, Predicate<Posicion> esApta) {

        int filas = Math.min(inferior.getTablero().getFilas(), superior.getTablero().getFilas());
        int columnas = Math.min(inferior.getTablero().getColumnas(), superior.getTablero().getColumnas());
        List<Posicion> candidatas = new ArrayList<>();
        List<Posicion> separadas = new ArrayList<>();

        for (int fila = 2; fila < filas; fila++) {
            for (int columna = 2; columna < columnas; columna++) {
                Posicion posicion = new Posicion(fila, columna);
                if (reservadas.contains(posicion) || !esApta.test(posicion)) {
                    continue;
                }

                candidatas.add(posicion);
                if (!esVecinaDeReservada(posicion, reservadas)) {
                    separadas.add(posicion);
                }
            }
        }

        List<Posicion> opciones = separadas.isEmpty() ? candidatas : separadas;
        if (opciones.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(opciones.get(azar.nextInt(opciones.size())));
    }

    private boolean esVecinaDeReservada(Posicion posicion, Set<Posicion> reservadas) {
        for (Direccion direccion : Direccion.values()) {
            if (reservadas.contains(posicion.mover(direccion))) {
                return true;
            }
        }
        return false;
    }

    private boolean estaDisponible(Plano plano, Posicion posicion) {
        return plano.getTablero().esTransitable(posicion) && estaLibre(plano, posicion);
    }

    private boolean estaLibre(Plano plano, Posicion posicion) {
        return !posicion.equals(plano.getEntrada())
                && !posicion.equals(plano.getSalida())
                && plano.obtenerEntidades(posicion).isEmpty();
    }

    private boolean puedeAbrirPared(Plano planoConPiso, Plano planoConPared, Posicion posicion) {
        Tablero tableroConPared = planoConPared.getTablero();

        return planoConPiso.getTablero().getTerreno(posicion) == Terreno.PISO
                && estaLibre(planoConPiso, posicion)
                && tableroConPared.getTerreno(posicion) == Terreno.PARED
                && estaLibre(planoConPared, posicion)
                && !tableroConPared.vecinasTransitables(posicion).isEmpty();
    }
}