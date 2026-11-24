
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.AgendaDTO;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba la conversión de una partida real a su DTO.
 */
class MapeadorPartidaTest {

    @Test
    void mapearConservaEstadoGeneralDeLaPartida() {

        // Crear una partida de prueba.
        Partida partida = crearPartida();

        // Crear un conversor de agenda de prueba.
        // No representa todavía la implementación definitiva.
        MapeadorPartida mapeador = new MapeadorPartida(
                new MapeadorHeroe(),
                new MapeadorMazmorra(),
                agenda -> {
                    AgendaDTO dto = new AgendaDTO();
                    dto.paso = 7;
                    dto.pendientes = new ArrayList<>();
                    return dto;
                }
        );

        // Ejecutar el mapeo.
        PartidaGuardadaDTO datos = mapeador.mapear(partida);

        // Verificar versión y pasos.
        assertEquals(1, datos.version);
        assertEquals(7, datos.pasos);

        // Verificar costos.
        assertEquals(1, datos.costos.get("PISO"));
        assertEquals(3, datos.costos.get("AGUA"));
        assertEquals(8, datos.costos.get("LAVA"));

        // Verificar héroe.
        assertEquals(75, datos.heroe.energia);
        assertEquals(100, datos.heroe.energiaMaxima);
        assertEquals(1, datos.heroe.cofresAbiertos);

        assertEquals(1, datos.heroe.ubicacion.tablero);
        assertEquals(1, datos.heroe.ubicacion.piso);
        assertEquals(2, datos.heroe.ubicacion.posicion.fila);
        assertEquals(2, datos.heroe.ubicacion.posicion.columna);

        // Verificar mazmorra.
        assertEquals(1, datos.mazmorra.pisosPorTablero);
        assertEquals(1, datos.mazmorra.planos.size());

        // Verificar agenda provisional de la prueba.
        assertNotNull(datos.agenda);
        assertEquals(7, datos.agenda.paso);
        assertTrue(datos.agenda.pendientes.isEmpty());
    }

    @Test
    void mapearRechazaPartidaNula() {

        MapeadorPartida mapeador = new MapeadorPartida(
                new MapeadorHeroe(),
                new MapeadorMazmorra(),
                agenda -> new AgendaDTO()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> mapeador.mapear(null)
        );
    }

    @Test
    void mapearRechazaAgendaNula() {

        MapeadorPartida mapeador = new MapeadorPartida(
                new MapeadorHeroe(),
                new MapeadorMazmorra(),
                agenda -> null
        );

        assertThrows(
                IllegalStateException.class,
                () -> mapeador.mapear(crearPartida())
        );
    }

    /**
     * Crea una partida sencilla sin entidades ni efectos activos.
     */
    private Partida crearPartida() {

        Tablero tablero = new Tablero(3, 3);

        Posicion centro = new Posicion(2, 2);

        tablero.setTerreno(centro, Terreno.PISO);

        Plano plano = new Plano(
                tablero,
                centro,
                new Posicion(2, 2)
        );

        Mazmorra mazmorra = new Mazmorra(
                List.of(plano),
                1
        );

        Heroe heroe = new Heroe(
                new Ubicacion(1, 1, centro),
                100
        );

        heroe.restarEnergia(25);
        heroe.sumarCofreAbierto();

        Map<Terreno, Integer> costos =
                new EnumMap<>(Terreno.class);

        costos.put(Terreno.PISO, 1);
        costos.put(Terreno.AGUA, 3);
        costos.put(Terreno.LAVA, 8);

        return new Partida(
                mazmorra,
                heroe,
                costos,
                7,
                new Agenda()
        );
    }
}
