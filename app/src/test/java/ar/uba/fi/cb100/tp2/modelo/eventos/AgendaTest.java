package ar.uba.fi.cb100.tp2.modelo.eventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests de la Agenda con eventos de prueba que anotan su nombre en un log.
 * Los eventos no usan la partida, por eso se pasa null.
 */
class AgendaTest {

    private Agenda agenda;
    private List<String> log;

    @BeforeEach
    void preparar() {
        this.agenda = new Agenda();
        this.log = new ArrayList<>();
    }

    private Evento anota(String nombre) {
        return partida -> this.log.add(nombre);
    }

    private void avanzarHasta(int paso) {
        for (int p = this.agenda.getPasoActual() + 1; p <= paso; p++) {
            this.agenda.ejecutarLosQueVencen(p, null);
        }
    }

    @Test
    void recienCreadaNoTienePendientes() {
        assertTrue(this.agenda.getPendientes().isEmpty());
        assertEquals(0, this.agenda.getPasoActual());
    }

    @Test
    void unEventoSeEjecutaRecienCuandoLlegaSuPaso() {
        this.agenda.programar(3, anota("despertar"));

        avanzarHasta(2);
        assertTrue(this.log.isEmpty());

        avanzarHasta(3);
        assertEquals(List.of("despertar"), this.log);
    }

    @Test
    void seEjecutaUnaSolaVez() {
        this.agenda.programar(1, anota("una vez"));

        avanzarHasta(5);

        assertEquals(List.of("una vez"), this.log);
        assertTrue(this.agenda.getPendientes().isEmpty());
    }

    @Test
    void vaPrimeroElQueVenceAntesAunqueSeHayaProgramadoDespues() {
        this.agenda.programar(5, anota("tarde"));
        this.agenda.programar(2, anota("temprano"));

        avanzarHasta(5);

        assertEquals(List.of("temprano", "tarde"), this.log);
    }

    @Test
    void siVencenEnElMismoPasoSalenEnElOrdenEnQueSeProgramaron() {
        this.agenda.programar(2, anota("primero"));
        this.agenda.programar(2, anota("segundo"));
        this.agenda.programar(2, anota("tercero"));

        avanzarHasta(2);

        assertEquals(List.of("primero", "segundo", "tercero"), this.log);
    }

    @Test
    void siSeSalteanPasosIgualSeEjecutanLosVencidos() {
        this.agenda.programar(2, anota("vencido"));

        this.agenda.ejecutarLosQueVencen(5, null);

        assertEquals(List.of("vencido"), this.log);
    }

    @Test
    void programarCuentaDesdeElPasoActual() {
        avanzarHasta(10);
        this.agenda.programar(1, anota("en el 11"));

        assertEquals(11, this.agenda.getPendientes().get(0).paso());
    }

    @Test
    void unEventoQueProgramaOtroNoLoEjecutaEnElMismoPaso() {
        this.agenda.programar(1, partida -> {
            this.log.add("trampa");
            this.agenda.programar(1, anota("se rearma"));
        });

        avanzarHasta(1);
        assertEquals(List.of("trampa"), this.log);

        avanzarHasta(2);
        assertEquals(List.of("trampa", "se rearma"), this.log);
    }

    @Test
    void getPendientesDevuelveUnaCopiaEnOrden() {
        this.agenda.programar(4, anota("c"));
        this.agenda.programar(1, anota("a"));
        this.agenda.programar(2, anota("b"));

        List<EventoProgramado> pendientes = this.agenda.getPendientes();
        pendientes.clear();

        List<EventoProgramado> deNuevo = this.agenda.getPendientes();
        assertEquals(3, deNuevo.size());
        assertEquals(List.of(1, 2, 4), deNuevo.stream().map(EventoProgramado::paso).toList());
    }

    @Test
    void restaurarDejaLaAgendaComoEstaba() {
        this.agenda.programar(3, anota("a"));
        this.agenda.programar(3, anota("b"));
        this.agenda.programar(5, anota("c"));
        avanzarHasta(2);
        int paso = this.agenda.getPasoActual();
        List<EventoProgramado> guardados = this.agenda.getPendientes();

        Agenda restaurada = new Agenda();
        restaurada.restaurar(paso, guardados);
        restaurada.programar(1, anota("nuevo"));
        restaurada.ejecutarLosQueVencen(3, null);
        restaurada.ejecutarLosQueVencen(4, null);
        restaurada.ejecutarLosQueVencen(5, null);

        assertEquals(List.of("a", "b", "nuevo", "c"), this.log);
    }

    @Test
    void rechazaProgramarParaAhoraOParaAtras() {
        assertThrows(IllegalArgumentException.class, () -> this.agenda.programar(0, anota("x")));
        assertThrows(IllegalArgumentException.class, () -> this.agenda.programar(-1, anota("x")));
    }

    @Test
    void rechazaUnEventoNulo() {
        assertThrows(IllegalArgumentException.class, () -> this.agenda.programar(1, null));
    }

    @Test
    void rechazaVolverAtras() {
        avanzarHasta(5);

        assertThrows(IllegalArgumentException.class, () -> this.agenda.ejecutarLosQueVencen(4, null));
    }
}