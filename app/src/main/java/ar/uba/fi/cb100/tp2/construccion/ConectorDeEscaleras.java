package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import java.util.List;
import java.util.Set;
import java.util.Random;

public class ConectorDeEscaleras {

    private final UbicadorDeEscaleras ubicador;

    public ConectorDeEscaleras(Random azar) {
        this.ubicador = new UbicadorDeEscaleras(azar);
    }

    /** Conecta los planos de un tablero, ordenados desde el primer piso. */
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