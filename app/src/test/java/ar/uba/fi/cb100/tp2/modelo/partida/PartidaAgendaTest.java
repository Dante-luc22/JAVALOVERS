package ar.uba.fi.cb100.tp2.modelo.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Tests de la agenda de la Partida.
 */
class PartidaAgendaTest {

    @Test
    void laPartidaExponeSuAgenda() {
        Tablero tablero = new Tablero(5, 5);
        tablero.setTerreno(new Posicion(2, 2), Terreno.PISO);
        Mazmorra mazmorra = new Mazmorra(List.of(new Plano(tablero, new Posicion(2, 2), new Posicion(2, 2))), 1);
        Partida partida = new Partida(mazmorra, new Heroe(mazmorra.getEntrada(), 100), Map.of(Terreno.PISO, 1));

        assertNotNull(partida.getAgenda());
    }

    @Test
    void laAgendaEjecutaElEventoAlDarElPasoProgramado() {
        Tablero tablero = new Tablero(5, 5);
        tablero.setTerreno(new Posicion(2, 2), Terreno.PISO);
        tablero.setTerreno(new Posicion(3, 2), Terreno.PISO);
        Mazmorra mazmorra = new Mazmorra(List.of(new Plano(tablero, new Posicion(2, 2), new Posicion(3, 2))), 1);
        Partida partida = new Partida(mazmorra, new Heroe(mazmorra.getEntrada(), 100), Map.of(Terreno.PISO, 1));
        AtomicInteger ejecuciones = new AtomicInteger();

        partida.getAgenda().programar(1, p -> ejecuciones.incrementAndGet());
        assertEquals(0, ejecuciones.get());

        partida.esperar();

        assertEquals(1, ejecuciones.get());
    }
}
