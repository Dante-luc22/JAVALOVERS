package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.efectos.*;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.*;
import ar.uba.fi.cb100.tp2.modelo.objetos.*;
import ar.uba.fi.cb100.tp2.modelo.partida.AzarPersistible;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ServicioPersistenciaPartidaTest {

    @TempDir
    Path directorioTemporal;

    private final ServicioPersistenciaPartida servicio =
            new ServicioPersistenciaPartida();

    /**
     * Comprueba el guardado físico y la posterior carga
     * de una partida completa.
     */
    @Test
    void guardarYCargarConservaEstadoDeLaPartida() throws Exception {

        Partida original = crearPartida();

        Path archivo = directorioTemporal.resolve("partida.json");

        // Guardar la partida.
        servicio.guardar(original, archivo);

        assertTrue(Files.isRegularFile(archivo));

        // Cargar la partida.
        Partida recuperada = servicio.cargar(archivo);

        // Estado general.
        assertEquals(original.getPasos(), recuperada.getPasos());

        assertEquals(
                original.getAzar().getEstadoInterno(),
                recuperada.getAzar().getEstadoInterno()
        );

        assertEquals(
                original.getCostos(),
                recuperada.getCostos()
        );

        // Héroe.
        Heroe originalHeroe = original.getHeroe();
        Heroe recuperadoHeroe = recuperada.getHeroe();

        assertEquals(
                originalHeroe.getEnergia(),
                recuperadoHeroe.getEnergia()
        );

        assertEquals(
                originalHeroe.getEnergiaMaxima(),
                recuperadoHeroe.getEnergiaMaxima()
        );

        assertEquals(
                originalHeroe.getCofresAbiertos(),
                recuperadoHeroe.getCofresAbiertos()
        );

        assertEquals(
                originalHeroe.getUbicacion(),
                recuperadoHeroe.getUbicacion()
        );

        // Inventario.
        assertEquals(
                originalHeroe.getInventario().getCantidades(),
                recuperadoHeroe.getInventario().getCantidades()
        );

        // Efectos activos.
        assertEquals(
                List.of(7, 12, 5),
                recuperadoHeroe.getEfectosActivos()
                        .stream()
                        .map(Efecto::getPasosRestantes)
                        .toList()
        );

        // Agenda.
        assertEquals(
                original.getAgenda().getPasoActual(),
                recuperada.getAgenda().getPasoActual()
        );

        assertTrue(recuperada.getAgenda().getPendientes().isEmpty());

        // Mazmorra.
        assertNotNull(recuperada.getMazmorra());

        assertEquals(
                1,
                recuperada.getMazmorra().getPlanos().size()
        );
    }

    /**
     * Comprueba que el servicio rechace una partida nula.
     */
    @Test
    void guardarRechazaPartidaNula() {

        Path archivo = directorioTemporal.resolve("partida.json");

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.guardar(null, archivo)
        );
    }

    /**
     * Comprueba que el servicio rechace una ruta nula al guardar.
     */
    @Test
    void guardarRechazaRutaNula() {

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.guardar(crearPartida(), null)
        );
    }

    /**
     * Comprueba que el servicio rechace una ruta nula al cargar.
     */
    @Test
    void cargarRechazaRutaNula() {

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.cargar(null)
        );
    }

    /**
     * Comprueba que no se pueda cargar un archivo inexistente.
     */
    @Test
    void cargarRechazaArchivoInexistente() {

        Path inexistente =
                directorioTemporal.resolve("no-existe.json");

        assertThrows(
                IllegalStateException.class,
                () -> servicio.cargar(inexistente)
        );
    }

    /**
     * Construye una partida completa para las pruebas.
     */
    private Partida crearPartida() {

        // Tablero.
        Tablero tablero = new Tablero(3, 3);

        for (int fila = 1; fila <= 3; fila++) {
            for (int columna = 1; columna <= 3; columna++) {
                tablero.setTerreno(
                        new Posicion(fila, columna),
                        Terreno.PISO
                );
            }
        }

        Posicion centro = new Posicion(2, 2);

        Plano plano = new Plano(
                tablero,
                new Posicion(1, 1),
                new Posicion(3, 3)
        );

        Mazmorra mazmorra = new Mazmorra(
                List.of(plano),
                1
        );

        // Inventario.
        Inventario inventario = new Inventario();

        inventario.restaurar(new BotasIgnifugas(), 2);
        inventario.restaurar(new ZapatosDeAgua(), 3);

        // Efectos activos.
        List<Efecto> efectos = List.of(
                new AguaComoPiso(7),
                new LavaComoPiso(12),
                new Envenenado(5)
        );

        // Héroe.
        Heroe heroe = new Heroe(
                new Ubicacion(1, 1, centro),
                75,
                100,
                1,
                inventario,
                efectos
        );

        // Costos.
        Map<Terreno, Integer> costos =
                new EnumMap<>(Terreno.class);

        costos.put(Terreno.PISO, 1);
        costos.put(Terreno.AGUA, 3);
        costos.put(Terreno.LAVA, 8);

        // Agenda.
        Agenda agenda = new Agenda();
        agenda.restaurar(7, List.of());

        return new Partida(
                mazmorra,
                heroe,
                costos,
                7,
                agenda,
                new AzarPersistible(12345L)
        );
    }
}