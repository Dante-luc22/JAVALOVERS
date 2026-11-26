package ar.uba.fi.cb100.tp2.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.partida.Accion;
import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.vista.Vista;
import ar.uba.fi.cb100.tp2.persistencia.ServicioPersistenciaPartida;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests del ciclo del juego, con una vista falsa que devuelve acciones
 * preparadas y cuenta cuántas veces se mostró la partida.
 */
public class ControladorJuegoTest {

    /** Vista de mentira: no dibuja ni lee el teclado. */
    private static class VistaFalsa implements Vista {

        private final Deque<Accion> acciones;
        private int vecesMostrada = 0;
        private int avisos = 0;
        private String ultimoAviso;

        VistaFalsa(Accion... acciones) {
            this.acciones = new ArrayDeque<>(List.of(acciones));}

        @Override
        public void mostrar(Partida partida) {this.vecesMostrada++;}

        @Override
        public Accion leerAccion() {return this.acciones.removeFirst();}

        @Override
        public void avisar(String mensaje) {this.avisos++;this.ultimoAviso = mensaje;}
    }

    private Partida partida;

    @org.junit.jupiter.api.io.TempDir
    private java.nio.file.Path directorioTemporal;

    /**
     * Un pasillo de piso en la fila 2: entrada en (2, 2) y salida en (2, 4).
     * Sin monstruos ni cofres.
     */
    @BeforeEach
    void armarPartida() {

        Tablero tablero = new Tablero(5, 6);

        for (int columna = 2; columna <= 4; columna++) {
            tablero.setTerreno(
                    new Posicion(2, columna),
                    Terreno.PISO
            );
        }

        Plano plano = new Plano(tablero, new Posicion(2, 2), new Posicion(2, 4));

        Mazmorra mazmorra = new Mazmorra(List.of(plano), 1);

        Heroe heroe = new Heroe(mazmorra.getEntrada(), 100);

        this.partida = new Partida(mazmorra, heroe, Map.of(
                Terreno.PISO, 1,
                Terreno.AGUA, 3,
                Terreno.LAVA, 10)
        );
    }

    @Test
    void llegarALaSalidaGanaLaPartida() {

        VistaFalsa vista = new VistaFalsa(Accion.MOVER_DERECHA, Accion.MOVER_DERECHA);

        EstadoPartida fin = new ControladorJuego(this.partida, vista).jugar();

        assertEquals(EstadoPartida.GANADA, fin);
        assertEquals(3, vista.vecesMostrada);
    }

    @Test
    void salirTerminaSinJugarYQuedaEnCurso() {

        VistaFalsa vista = new VistaFalsa(Accion.SALIR);

        EstadoPartida fin = new ControladorJuego(this.partida, vista).jugar();

        assertEquals(EstadoPartida.EN_CURSO, fin);
        assertEquals(1, vista.vecesMostrada);
        assertEquals(0, this.partida.getPasos());
    }

    @Test
    void chocarUnaParedNoSeMuestraNiCuentaComoPaso() {

        VistaFalsa vista = new VistaFalsa(Accion.MOVER_ARRIBA, Accion.SALIR);

        new ControladorJuego(this.partida, vista).jugar();

        assertEquals(1, vista.vecesMostrada);
        assertEquals(0, this.partida.getPasos());
        assertEquals(1, vista.avisos);
    }

    @Test
    void esperarCuentaComoPasoYSeMuestra() {

        VistaFalsa vista = new VistaFalsa(Accion.ESPERAR, Accion.SALIR);

        new ControladorJuego(this.partida, vista).jugar();

        assertEquals(2, vista.vecesMostrada);
        assertEquals(1, this.partida.getPasos());
    }
    /**
     * Comprueba que guardar cree el archivo JSON sin consumir un turno
     * y que el jugador pueda continuar con la partida.
     */
    @Test
    void guardarNoConsumeTurnoYPermiteContinuar() throws Exception {

        VistaFalsa vista = new VistaFalsa(Accion.GUARDAR, Accion.SALIR);

        ServicioPersistenciaPartida servicio = new ServicioPersistenciaPartida();

        java.nio.file.Path destino = directorioTemporal.resolve("partida.json");

        ControladorJuego controlador = new ControladorJuego(this.partida, vista, servicio, destino);

        EstadoPartida fin = controlador.jugar();

        // El juego continúa hasta que el jugador decide salir.
        assertEquals(EstadoPartida.EN_CURSO, fin);

        // El archivo JSON fue creado.
        assertTrue(java.nio.file.Files.isRegularFile(destino));

        // Guardar no consume un turno.
        assertEquals(0, this.partida.getPasos());

        // La ubicación y la energía no cambian.
        assertEquals(new Posicion(2, 2), this.partida.getHeroe().getUbicacion().posicion());

        assertEquals(100, this.partida.getHeroe().getEnergia());

        // No se vuelve a dibujar la partida después de guardar.
        assertEquals(1, vista.vecesMostrada);

        // Se informa que el guardado fue exitoso.
        assertEquals(1, vista.avisos);

        assertTrue(vista.ultimoAviso.contains("Partida guardada correctamente"));

        // Comprobar que el archivo realmente se puede cargar.
        Partida recuperada = servicio.cargar(destino);

        assertEquals(0, recuperada.getPasos());

        assertEquals(this.partida.getHeroe().getUbicacion(), recuperada.getHeroe().getUbicacion());

        assertEquals(this.partida.getHeroe().getEnergia(), recuperada.getHeroe().getEnergia());
    }

    /**
     * Comprueba que, si el guardado falla, se informe al jugador
     * y la partida pueda continuar sin consumir un turno.
     */
    @Test
    void falloAlGuardarInformaYPermiteContinuar() {

        // La agenda contiene un evento que todavía no sabemos persistir.
        this.partida.getAgenda().programar(1, partida -> {
                    // Evento de prueba.
                }
        );

        VistaFalsa vista = new VistaFalsa(Accion.GUARDAR, Accion.SALIR);

        ServicioPersistenciaPartida servicio = new ServicioPersistenciaPartida();

        java.nio.file.Path destino = directorioTemporal.resolve("partida-fallida.json");

        ControladorJuego controlador = new ControladorJuego(this.partida, vista, servicio, destino);

        EstadoPartida fin = controlador.jugar();

        // El jugador puede continuar y salir normalmente.
        assertEquals(EstadoPartida.EN_CURSO, fin);

        // El archivo no debe haberse creado.
        assertFalse(java.nio.file.Files.exists(destino));

        // El intento fallido no consume turnos.
        assertEquals(0, this.partida.getPasos());

        // Se informa el problema.
        assertEquals(1, vista.avisos);

        assertTrue(vista.ultimoAviso.contains("No se pudo guardar la partida"));

        // El evento pendiente sigue existiendo.
        assertEquals(1, this.partida.getAgenda().getPendientes().size());
    }
    @Test
    void interactuarDondeNoHayNadaAvisaYNoCuentaComoPaso() {
        VistaFalsa vista = new VistaFalsa(Accion.INTERACTUAR, Accion.SALIR);

        new ControladorJuego(this.partida, vista).jugar();

        assertEquals(1, vista.vecesMostrada);
        assertEquals(0, this.partida.getPasos());
        assertEquals(1, vista.avisos);
    }
}
