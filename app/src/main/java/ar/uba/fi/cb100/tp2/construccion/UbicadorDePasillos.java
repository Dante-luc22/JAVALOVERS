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

/**
 * <hr><hr>
 * <h2>Ubicación de extremos de pasillos</h2>
 * <hr><hr>
 *
 * Busca paredes en el borde de un plano que puedan servir como extremos
 * de un pasillo. Una pared es válida si su vecina hacia el interior es
 * transitable. La búsqueda excluye las esquinas y los extremos que ya
 * fueron abiertos.
 *
 * <hr>
 * <ul>
 *   <li>{@code buscarExtremo} → Recorre el lado indicado, reúne las
 *       paredes válidas y sortea una con el {@code Random} recibido
 *       por constructor.</li>
 * </ul>
 * <hr>
 *
 * Devuelve {@code Optional.empty()} si el lado no tiene candidatas.
 * La búsqueda conserva el terreno del tablero.
 */

public class UbicadorDePasillos {

    private final Random azar;

    public UbicadorDePasillos(Random azar) {
        this.azar = azar;
    }


    public Optional<Posicion> buscarExtremo(Plano plano, Direccion lado) {
        Tablero tablero = plano.getTablero();
        int filas = tablero.getFilas();
        int columnas = tablero.getColumnas();

        if (filas < 3 || columnas < 3) {
            return Optional.empty();
        }

        boolean bordeVertical = lado == Direccion.IZQUIERDA || lado == Direccion.DERECHA;
        int limite = bordeVertical ? filas : columnas;

        List<Posicion> candidatas = new ArrayList<>();
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
                candidatas.add(borde);
            }
        }

        if (candidatas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidatas.get(azar.nextInt(candidatas.size())));
    }
}