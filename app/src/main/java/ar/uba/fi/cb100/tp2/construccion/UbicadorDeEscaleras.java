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
 * <h2>Ubicación de escaleras entre planos</h2>
 * <hr><hr>
 *
 * Prepara una posición interior común para una escalera entre dos planos.
 * Excluye entradas, salidas, entidades y posiciones reservadas. Entre
 * las candidatas válidas, prioriza las que no son vecinas ortogonales de
 * una posición reservada. Si todas son vecinas, sortea entre ellas.
 *
 * <hr>
 * <ul>
 *   <li>{@code buscarPosicionComun} → Sortea una posición transitable y
 *       libre en ambos planos. La variante interna recibe las reservas.</li>
 *   <li>{@code prepararPosicionComun} → Busca primero una posición común.
 *       Si no encuentra ninguna, sortea una pared que pueda abrirse y
 *       convierte únicamente la pared elegida en piso.</li>
 *   <li>{@code elegirPosicion} → Reúne las candidatas válidas y las que
 *       están separadas de las reservas. Si hay candidatas separadas,
 *       el sorteo se hace entre ellas.</li>
 *   <li>{@code esVecinaDeReservada} → Comprueba si la candidata tiene
 *       una posición reservada entre sus cuatro vecinas ortogonales.</li>
 *   <li>{@code estaDisponible} → Comprueba que la posición sea transitable
 *       y esté libre.</li>
 *   <li>{@code estaLibre} → Verifica que la posición no sea entrada ni
 *       salida y que no contenga entidades.</li>
 *   <li>{@code puedeAbrirPared} → Verifica que haya piso en un plano
 *       y pared en el otro, que ambas posiciones estén libres y que la
 *       pared tenga una vecina transitable.</li>
 * </ul>
 * <hr>
 *
 * El sorteo usa el {@code Random} recibido por constructor. Si no hay
 * una posición adecuada, se devuelve {@code Optional.empty()}.
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