package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import java.util.Random;

/**
 * <hr><hr>
 * <h2>Conexión por pasillo entre dos tableros</h2>
 * <hr><hr>
 *
 * Une un plano de cada tablero mediante dos extremos situados en los
 * bordes indicados. Utiliza {@code UbicadorDePasillos} para sortear ambas
 * posiciones con el {@code Random} recibido por constructor.
 *
 * <hr>
 * <ul>
 *   <li>{@code conectar} → Obtiene los planos a partir del tablero y
 *       el piso de cada referencia. Busca los dos extremos, convierte
 *       las paredes elegidas en piso y registra el paso en ambos
 *       sentidos mediante {@code Mazmorra.conectar}.</li>
 * </ul>
 * <hr>
 *
 * Si falta un extremo, devuelve {@code false} y conserva ambos tableros.
 * Si las referencias pertenecen al mismo tablero, lanza
 * {@code IllegalArgumentException}.
 */

public class ConectorDePasillos {

    private final UbicadorDePasillos ubicador;

    public ConectorDePasillos(Random azar) {
        this.ubicador = new UbicadorDePasillos(azar);
    }


    public boolean conectar(
            Mazmorra mazmorra,
            Ubicacion referenciaOrigen, Direccion ladoOrigen,
            Ubicacion referenciaDestino, Direccion ladoDestino) {

        if (referenciaOrigen.tablero() == referenciaDestino.tablero()) {
            throw new IllegalArgumentException("Un pasillo une tableros distintos");
        }

        Plano planoOrigen = mazmorra.getPlano(referenciaOrigen);
        Plano planoDestino = mazmorra.getPlano(referenciaDestino);

        var extremoOrigen = ubicador.buscarExtremo(planoOrigen, ladoOrigen);
        var extremoDestino = ubicador.buscarExtremo(planoDestino, ladoDestino);

        if (extremoOrigen.isEmpty() || extremoDestino.isEmpty()) {
            return false;
        }

        Ubicacion origen = new Ubicacion(
                referenciaOrigen.tablero(), referenciaOrigen.piso(), extremoOrigen.get());
        Ubicacion destino = new Ubicacion(
                referenciaDestino.tablero(), referenciaDestino.piso(), extremoDestino.get());

        planoOrigen.getTablero().setTerreno(origen.posicion(), Terreno.PISO);
        planoDestino.getTablero().setTerreno(destino.posicion(), Terreno.PISO);

        mazmorra.conectar(origen, destino);
        return true;
    }
}