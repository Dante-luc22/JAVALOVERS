package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import java.util.Random;

public class ConectorDePasillos {

    private final UbicadorDePasillos ubicador;

    public ConectorDePasillos(Random azar) {
        this.ubicador = new UbicadorDePasillos(azar);
    }

    /**
     * Usa el tablero y el piso de cada ubicación para identificar los planos.
     * Devuelve false si no hay un extremo disponible en alguno de los bordes.
     */
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