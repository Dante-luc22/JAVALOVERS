package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

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
}