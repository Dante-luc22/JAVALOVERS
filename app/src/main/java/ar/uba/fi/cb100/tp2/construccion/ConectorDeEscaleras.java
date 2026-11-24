package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import java.util.List;
import java.util.Set;
import java.util.Random;

/**
 * <hr><hr>
 * <h2>Conexión entre pisos de un tablero</h2>
 * <hr><hr>
 *
 * Conecta los pisos consecutivos de un tablero mediante escaleras.
 * Cada escalera ocupa la misma fila y columna en los dos pisos.
 * El ubicador utiliza el {@code Random} recibido por constructor.
 *
 * <hr>
 * <ul>
 *   <li>{@code conectarPisos} → Recorre los planos en orden de piso.
 *       Para cada par consecutivo, pide una posición al
 *       {@code UbicadorDeEscaleras} y registra ambos extremos mediante
 *       {@code Mazmorra.conectar}.</li>
 * </ul>
 * <hr>
 *
 * La celda usada en el piso compartido se reserva para la siguiente
 * búsqueda. Si un par de pisos no se puede unir, se lanza
 * {@code IllegalStateException}.
 */

public class ConectorDeEscaleras {

    private final UbicadorDeEscaleras ubicador;

    public ConectorDeEscaleras(Random azar) {
        this.ubicador = new UbicadorDeEscaleras(azar);
    }

    public void conectarPisos(Mazmorra mazmorra, int tablero, List<Plano> planos) {
        Set<Posicion> reservadas = Set.of();

        for (int piso = 1; piso < planos.size(); piso++) {
            Plano inferior = planos.get(piso - 1);
            Plano superior = planos.get(piso);

            var posicion = ubicador.prepararPosicionComun(inferior, superior, reservadas);
            if (posicion.isEmpty()) {
                throw new IllegalStateException(
                        "No se pudo conectar el piso " + piso + " con el piso " + (piso + 1)
                );
            }

            Posicion celda = posicion.get();
            mazmorra.conectar(
                    new Ubicacion(tablero, piso, celda),
                    new Ubicacion(tablero, piso + 1, celda)
            );

            reservadas = Set.of(celda);
        }
    }
}