package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Conexion;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConectorDePasillosTest {

    @Test
    void abreLosDosExtremosYRegistraElPasilloEnAmbosSentidos() {
        Plano primero = crearPlano(new Posicion(3, 6));
        Plano segundo = crearPlano(new Posicion(2, 2));
        Mazmorra mazmorra = new Mazmorra(List.of(primero, segundo), 1);
        Ubicacion derecha = new Ubicacion(1, 1, new Posicion(3, 7));
        Ubicacion izquierda = new Ubicacion(2, 1, new Posicion(2, 1));

        assertTrue(conectar(mazmorra, primero, segundo, 42L));

        assertEquals(Terreno.PISO, primero.getTablero().getTerreno(derecha.posicion()));
        assertEquals(Terreno.PISO, segundo.getTablero().getTerreno(izquierda.posicion()));
        assertEquals(List.of(new Conexion(derecha, izquierda)), mazmorra.getConexiones());
        assertEquals(Optional.of(izquierda), mazmorra.destinoDesde(derecha));
        assertEquals(Optional.of(derecha), mazmorra.destinoDesde(izquierda));
    }

    @Test
    void conservaLaParedDelPrimerTableroSiFaltaElSegundoExtremo() {
        Plano primero = crearPlano(new Posicion(3, 6));
        Plano segundo = crearPlano(new Posicion(4, 4));
        Mazmorra mazmorra = new Mazmorra(List.of(primero, segundo), 1);

        assertFalse(conectar(mazmorra, primero, segundo, 42L));

        assertEquals(Terreno.PARED, primero.getTablero().getTerreno(new Posicion(3, 7)));
        assertTrue(mazmorra.getConexiones().isEmpty());
    }

    @Test
    void laMismaSemillaRepiteElPasillo() {
        assertEquals(conexionCon(42L), conexionCon(42L));
    }

    private Conexion conexionCon(long semilla) {
        Plano primero = crearPlano(new Posicion(2, 6), new Posicion(3, 6));
        Plano segundo = crearPlano(new Posicion(2, 2), new Posicion(3, 2));
        Mazmorra mazmorra = new Mazmorra(List.of(primero, segundo), 1);
        assertTrue(conectar(mazmorra, primero, segundo, semilla));
        return mazmorra.getConexiones().getFirst();
    }

    private boolean conectar(Mazmorra mazmorra, Plano primero, Plano segundo, long semilla) {
        return new ConectorDePasillos(new Random(semilla)).conectar(
                mazmorra,
                new Ubicacion(1, 1, primero.getEntrada()), Direccion.DERECHA,
                new Ubicacion(2, 1, segundo.getEntrada()), Direccion.IZQUIERDA);
    }

    private Plano crearPlano(Posicion entrada, Posicion... pisos) {
        Tablero tablero = new Tablero(5, 7);
        tablero.setTerreno(entrada, Terreno.PISO);
        for (Posicion piso : pisos) {
            tablero.setTerreno(piso, Terreno.PISO);
        }
        return new Plano(tablero, entrada, null);
    }
}
