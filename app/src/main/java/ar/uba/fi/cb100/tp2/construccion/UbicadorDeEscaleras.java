package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;

import java.util.Optional;

public class UbicadorDeEscaleras {

    public Optional<Posicion> buscarPosicionComun(Plano inferior, Plano superior) {
        int filas = Math.min(inferior.getTablero().getFilas(), superior.getTablero().getFilas());
        int columnas = Math.min(inferior.getTablero().getColumnas(), superior.getTablero().getColumnas());

        for (int fila = 2; fila < filas; fila++) {
            for (int columna = 2; columna < columnas; columna++) {
                Posicion posicion = new Posicion(fila, columna);
                if (estaDisponible(inferior, posicion) && estaDisponible(superior, posicion)) {
                    return Optional.of(posicion);
                }
            }
        }

        return Optional.empty();
    }

    private boolean estaDisponible(Plano plano, Posicion posicion) {
        return plano.getTablero().esTransitable(posicion)
                && !posicion.equals(plano.getEntrada())
                && !posicion.equals(plano.getSalida())
                && plano.obtenerEntidades(posicion).isEmpty();
    }

    public Optional<Posicion> prepararPosicionComun(Plano inferior, Plano superior) {
        Optional<Posicion> posicionComun = buscarPosicionComun(inferior, superior);
        if (posicionComun.isPresent()) {
            return posicionComun;
        }

        int filas = Math.min(inferior.getTablero().getFilas(), superior.getTablero().getFilas());
        int columnas = Math.min(inferior.getTablero().getColumnas(), superior.getTablero().getColumnas());

        for (int fila = 2; fila < filas; fila++) {
            for (int columna = 2; columna < columnas; columna++) {
                Posicion posicion = new Posicion(fila, columna);
                if (abrirParedSiConecta(inferior, superior, posicion)
                        || abrirParedSiConecta(superior, inferior, posicion)) {
                    return Optional.of(posicion);
                }
            }
        }

        return Optional.empty();
    }

    private boolean abrirParedSiConecta(Plano planoConPiso, Plano planoConPared, Posicion posicion) {
        var tableroConPared = planoConPared.getTablero();

        if (planoConPiso.getTablero().getTerreno(posicion) != Terreno.PISO
                || !estaDisponible(planoConPiso, posicion)
                || tableroConPared.getTerreno(posicion) != Terreno.PARED
                || posicion.equals(planoConPared.getEntrada())
                || posicion.equals(planoConPared.getSalida())
                || !planoConPared.obtenerEntidades(posicion).isEmpty()
                || tableroConPared.vecinasTransitables(posicion).isEmpty()) {
            return false;
        }

        tableroConPared.setTerreno(posicion, Terreno.PISO);
        return true;
    }
}