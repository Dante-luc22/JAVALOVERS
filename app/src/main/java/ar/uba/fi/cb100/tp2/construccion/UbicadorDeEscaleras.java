package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.Optional;
import java.util.Set;

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