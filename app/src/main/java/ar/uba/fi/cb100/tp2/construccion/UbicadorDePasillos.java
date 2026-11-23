package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.Optional;

/** Busca celdas del borde que puedan servir como extremos de un pasillo. */
public class UbicadorDePasillos {

    /**
     * Devuelve la primera pared del lado elegido que tiene una vecina interior
     * transitable. La búsqueda excluye las esquinas y conserva el tablero.
     */
    public Optional<Posicion> buscarExtremo(Plano plano, Direccion lado) {
        Tablero tablero = plano.getTablero();
        int filas = tablero.getFilas();
        int columnas = tablero.getColumnas();

        if (filas < 3 || columnas < 3) {
            return Optional.empty();
        }

        boolean bordeVertical = lado == Direccion.IZQUIERDA || lado == Direccion.DERECHA;
        int limite = bordeVertical ? filas : columnas;

        for (int indice = 2; indice < limite; indice++) {
            Posicion borde = switch (lado) {
                case ARRIBA -> new Posicion(1, indice);
                case ABAJO -> new Posicion(filas, indice);
                case IZQUIERDA -> new Posicion(indice, 1);
                case DERECHA -> new Posicion(indice, columnas);
            };

            Posicion interior = new Posicion(
                    borde.fila() - lado.getDeltaFila(),
                    borde.columna() - lado.getDeltaColumna()
            );

            if (tablero.getTerreno(borde) == Terreno.PARED
                    && tablero.esTransitable(interior)) {
                return Optional.of(borde);
            }
        }

        return Optional.empty();
    }
}