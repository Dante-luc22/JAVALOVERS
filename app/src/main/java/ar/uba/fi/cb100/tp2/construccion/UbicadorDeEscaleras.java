package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.Optional;
import java.util.Set;

/**
 * <hr><hr>
 * <h1>Ubicación de escaleras entre planos</h1>
 * <hr><hr>
 *
 * Busca una posición interior común para conectar dos planos mediante
 * escaleras, evitando entradas, salidas, entidades y posiciones reservadas.
 * Si no encuentra una disponible, intenta habilitarla abriendo una pared
 * junto a una celda transitable.
 *
 * <hr>
 * <ul>
 *   <li>{@code buscarPosicionComun} → Busca la primera posición transitable
 *       y libre en ambos planos, dentro de sus dimensiones compartidas.
 *       La variante interna permite excluir posiciones reservadas.</li>
 *   <li>{@code prepararPosicionComun} → Busca una posición común y,
 *       si no existe, intenta convertir una pared de uno de los planos
 *       en piso. Permite indicar posiciones reservadas.</li>
 *   <li>{@code estaDisponible} → Comprueba que una posición sea
 *       transitable y esté libre.</li>
 *   <li>{@code estaLibre} → Verifica que la posición no sea entrada
 *       ni salida y que no contenga entidades.</li>
 *   <li>{@code abrirParedSiConecta} → Convierte una pared en piso
 *       si en el otro plano hay piso en esa posición, ambos lugares
 *       están libres y la pared tiene algún vecino transitable.</li>
 * </ul>
 * <hr>
 * Devuelve {@code Optional.empty()} si no encuentra o no puede habilitar
 * una posición adecuada. Esta clase prepara la ubicación; no coloca
 * las escaleras.
 */



public class UbicadorDeEscaleras {

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

        int filas = Math.min(inferior.getTablero().getFilas(), superior.getTablero().getFilas());
        int columnas = Math.min(inferior.getTablero().getColumnas(), superior.getTablero().getColumnas());

        for (int fila = 2; fila < filas; fila++) {
            for (int columna = 2; columna < columnas; columna++) {
                Posicion posicion = new Posicion(fila, columna);
                if (reservadas.contains(posicion)) {
                    continue;
                }
                if (abrirParedSiConecta(inferior, superior, posicion)
                        || abrirParedSiConecta(superior, inferior, posicion)) {
                    return Optional.of(posicion);
                }
            }
        }

        return Optional.empty();
    }

    private Optional<Posicion> buscarPosicionComun(
            Plano inferior, Plano superior, Set<Posicion> reservadas) {

        int filas = Math.min(inferior.getTablero().getFilas(), superior.getTablero().getFilas());
        int columnas = Math.min(inferior.getTablero().getColumnas(), superior.getTablero().getColumnas());

        for (int fila = 2; fila < filas; fila++) {
            for (int columna = 2; columna < columnas; columna++) {
                Posicion posicion = new Posicion(fila, columna);
                if (!reservadas.contains(posicion)
                        && estaDisponible(inferior, posicion)
                        && estaDisponible(superior, posicion)) {
                    return Optional.of(posicion);
                }
            }
        }

        return Optional.empty();
    }

    private boolean estaDisponible(Plano plano, Posicion posicion) {
        return plano.getTablero().esTransitable(posicion) && estaLibre(plano, posicion);
    }

    private boolean estaLibre(Plano plano, Posicion posicion) {
        return !posicion.equals(plano.getEntrada())
                && !posicion.equals(plano.getSalida())
                && plano.obtenerEntidades(posicion).isEmpty();
    }

    private boolean abrirParedSiConecta(Plano planoConPiso, Plano planoConPared, Posicion posicion) {
        Tablero tableroConPared = planoConPared.getTablero();

        if (planoConPiso.getTablero().getTerreno(posicion) != Terreno.PISO
                || !estaLibre(planoConPiso, posicion)
                || tableroConPared.getTerreno(posicion) != Terreno.PARED
                || !estaLibre(planoConPared, posicion)
                || tableroConPared.vecinasTransitables(posicion).isEmpty()) {
            return false;
        }

        tableroConPared.setTerreno(posicion, Terreno.PISO);
        return true;
    }
}