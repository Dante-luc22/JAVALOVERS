package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.efectos.*;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.EventoProgramado;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.*;
import ar.uba.fi.cb100.tp2.modelo.objetos.*;
import ar.uba.fi.cb100.tp2.modelo.partida.AzarPersistible;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

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
     *
     * Conserva los atributos del héroe, inventario,
     * efectos activos, agenda, mazmorra y generador aleatorio.
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
        assertEquals(original.getDificultad(), recuperada.getDificultad());
        assertEquals(original.getSemilla(), recuperada.getSemilla());
        assertEquals(original.getAzar().getEstadoInterno(), recuperada.getAzar().getEstadoInterno());
        assertEquals(original.getCostos(), recuperada.getCostos());

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

        // Efectos activos: comparar sus tipos.
        assertEquals(
                tiposDeEfectos(originalHeroe),
                tiposDeEfectos(recuperadoHeroe)
        );

        // Agenda.
        assertEquals(
                original.getAgenda().getPasoActual(),
                recuperada.getAgenda().getPasoActual()
        );

        comprobarEventosPendientes(
                original.getAgenda().getPendientes(),
                recuperada.getAgenda().getPendientes()
        );

        // Mazmorra.
        assertNotNull(recuperada.getMazmorra());

        assertEquals(
                1,
                recuperada.getMazmorra().getPlanos().size()
        );
    }

    /**
     * Comprueba que los eventos pendientes sobrevivan al guardado
     * y se ejecuten en los pasos originalmente programados.
     */
    @Test
    void guardarYCargarConservaVencimientoDeLosEfectos() {

        Partida original = crearPartida();

        // Guardar y reconstruir la partida.
        servicio.guardar(
                original,
                directorioTemporal.resolve("partida-efectos.json")
        );

        Partida recuperada = servicio.cargar(
                directorioTemporal.resolve("partida-efectos.json")
        );

        Heroe heroe = recuperada.getHeroe();

        // Al cargar, los tres efectos continúan activos.
        assertEquals(
                List.of(
                        TipoDeEfecto.AGUA_COMO_PISO,
                        TipoDeEfecto.LAVA_COMO_PISO,
                        TipoDeEfecto.ENVENENADO
                ),
                tiposDeEfectos(heroe)
        );

        // Avanzar del paso 7 al paso 12.
        avanzarTurnos(recuperada, 5);

        // En el paso 12 se elimina Envenenado.
        assertEquals(
                List.of(
                        TipoDeEfecto.AGUA_COMO_PISO,
                        TipoDeEfecto.LAVA_COMO_PISO
                ),
                tiposDeEfectos(heroe)
        );

        // Avanzar del paso 12 al paso 14.
        avanzarTurnos(recuperada, 2);

        // En el paso 14 se elimina AguaComoPiso.
        assertEquals(
                List.of(TipoDeEfecto.LAVA_COMO_PISO),
                tiposDeEfectos(heroe)
        );

        // Avanzar del paso 14 al paso 19.
        avanzarTurnos(recuperada, 5);

        // En el paso 19 se elimina LavaComoPiso.
        assertTrue(heroe.getEfectosActivos().isEmpty());
    }

    /**
     * Comprueba que se rechace una partida nula.
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
     * Comprueba que se rechace una ruta nula al guardar.
     */
    @Test
    void guardarRechazaRutaNula() {

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.guardar(crearPartida(), null)
        );
    }

    /**
     * Comprueba que se rechace una ruta nula al cargar.
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
     * Avanza una cantidad determinada de turnos válidos
     * utilizando la acción de esperar del héroe.
     */
    private void avanzarTurnos(Partida partida, int cantidad) {

        for (int i = 0; i < cantidad; i++) {
            partida.esperar();
        }
    }

    /**
     * Obtiene los tipos de los efectos activos respetando su orden.
     */
    private List<TipoDeEfecto> tiposDeEfectos(Heroe heroe) {

        return heroe.getEfectosActivos()
                .stream()
                .map(Efecto::getTipo)
                .toList();
    }

    /**
     * Comprueba que se conserven el paso de vencimiento,
     * el orden y el tipo de cada evento pendiente.
     */
    private void comprobarEventosPendientes(
            List<EventoProgramado> originales,
            List<EventoProgramado> recuperados
    ) {

        assertEquals(originales.size(), recuperados.size());

        for (int i = 0; i < originales.size(); i++) {

            EventoProgramado original = originales.get(i);
            EventoProgramado recuperado = recuperados.get(i);

            assertEquals(original.paso(), recuperado.paso());
            assertEquals(original.orden(), recuperado.orden());

            assertEquals(
                    original.evento().getClass(),
                    recuperado.evento().getClass()
            );

            assertEquals(
                    ((QuitarEfecto) original.evento()).getTipo(),
                    ((QuitarEfecto) recuperado.evento()).getTipo()
            );
        }
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
                new AguaComoPiso(),
                new LavaComoPiso(),
                new Envenenado()
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

        // AguaComoPiso: vence en el paso 14.
        agenda.programar(
                7,
                new QuitarEfecto(TipoDeEfecto.AGUA_COMO_PISO)
        );

        // LavaComoPiso: vence en el paso 19.
        agenda.programar(
                12,
                new QuitarEfecto(TipoDeEfecto.LAVA_COMO_PISO)
        );

        // Envenenado: vence en el paso 12.
        agenda.programar(
                5,
                new QuitarEfecto(TipoDeEfecto.ENVENENADO)
        );

        return new Partida(
                mazmorra,
                heroe,
                costos,
                7,
                agenda,
                new AzarPersistible(12345L),
                Dificultad.MEDIO,
                12345L
        );
    }
}