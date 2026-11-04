package ar.uba.fi.cb100.tp2.modelo.valores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests de Posicion: mover en cada dirección y equals/hashCode
 * (los pide el enunciado porque Posicion se usa como clave de hash).
 */
class PosicionTest {

    @Test
    void moverDevuelveLaVecinaEnCadaDireccion() {
        Posicion centro = new Posicion(3, 3);

        assertEquals(new Posicion(2, 3), centro.mover(Direccion.ARRIBA));
        assertEquals(new Posicion(4, 3), centro.mover(Direccion.ABAJO));
        assertEquals(new Posicion(3, 2), centro.mover(Direccion.IZQUIERDA));
        assertEquals(new Posicion(3, 4), centro.mover(Direccion.DERECHA));
    }

    @Test
    void moverNoCambiaLaPosicionOriginal() {
        Posicion original = new Posicion(3, 3);

        original.mover(Direccion.ARRIBA);

        assertEquals(new Posicion(3, 3), original);
    }

    @Test
    void dosPosicionesConLaMismaFilaYColumnaSonIgualesYTienenElMismoHash() {
        Posicion una = new Posicion(2, 5);
        Posicion otra = new Posicion(2, 5);

        assertEquals(una, otra);
        assertEquals(una.hashCode(), otra.hashCode());
        assertNotEquals(una, new Posicion(5, 2));
    }

    @Test
    void unHashSetNoGuardaDosVecesLaMismaPosicion() {
        Set<Posicion> visitadas = new HashSet<>();

        visitadas.add(new Posicion(1, 1));
        visitadas.add(new Posicion(1, 1));

        assertEquals(1, visitadas.size());
        assertTrue(visitadas.contains(new Posicion(1, 1)));
    }
}
