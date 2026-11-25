package ar.uba.fi.cb100.tp2.modelo.efectos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.EventoProgramado;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de QuitarEfecto: el evento que, al ejecutarlo la agenda, saca del
 * héroe un efecto del tipo que guarda. Guarda el tipo y no el objeto, para
 * que siga andando después de guardar y retomar la partida.
 */
class QuitarEfectoTest {

    private Heroe heroe;
    private Partida partida;

    /** Una partida mínima: un solo plano con una celda de piso. */
    static Partida partidaMinima(Heroe heroe) {
        Posicion celda = new Posicion(2, 2);
        Tablero tablero = new Tablero(3, 3);
        tablero.setTerreno(celda, Terreno.PISO);
        Plano plano = new Plano(tablero, celda, celda);
        Mazmorra mazmorra = new Mazmorra(List.of(plano), 1);
        return new Partida(mazmorra, heroe, Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8));
    }

    @BeforeEach
    void crear() {
        this.heroe = new Heroe(new Ubicacion(1, 1, new Posicion(2, 2)), 100);
        this.partida = partidaMinima(this.heroe);
    }

    @Test
    void constructorRechazaTipoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new QuitarEfecto(null));
    }

    @Test
    void guardaElTipoQueRecibe() {
        // Es lo que necesita la persistencia para escribirlo en el JSON.
        QuitarEfecto evento = new QuitarEfecto(TipoDeEfecto.LAVA_COMO_PISO);

        assertEquals(TipoDeEfecto.LAVA_COMO_PISO, evento.getTipo());
    }

    @Test
    void alEjecutarseSacaElEfectoDelHeroe() {
        this.heroe.agregarEfecto(new AguaComoPiso());

        new QuitarEfecto(TipoDeEfecto.AGUA_COMO_PISO).ejecutar(this.partida);

        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    @Test
    void sacaElEfectoAunqueElHeroeTengaOtroObjetoDelMismoTipo() {
        // Simula retomar una partida: el evento se creó con un efecto, pero
        // el héroe tiene otro objeto nuevo del mismo tipo.
        Efecto original = new AguaComoPiso();
        QuitarEfecto evento = new QuitarEfecto(original.getTipo());
        this.heroe.agregarEfecto(new AguaComoPiso());

        evento.ejecutar(this.partida);

        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    @Test
    void conDosDelMismoTipoSacaUnoSolo() {
        this.heroe.agregarEfecto(new AguaComoPiso());
        this.heroe.agregarEfecto(new AguaComoPiso());

        new QuitarEfecto(TipoDeEfecto.AGUA_COMO_PISO).ejecutar(this.partida);

        assertEquals(1, this.heroe.getEfectosActivos().size());
    }

    @Test
    void saleDeLaAgendaRecienCuandoLlegaSuPaso() {
        Agenda agenda = new Agenda();
        this.heroe.agregarEfecto(new AguaComoPiso());
        agenda.programar(3, new QuitarEfecto(TipoDeEfecto.AGUA_COMO_PISO));

        agenda.ejecutarLosQueVencen(2, this.partida);
        assertEquals(1, this.heroe.getEfectosActivos().size());

        agenda.ejecutarLosQueVencen(3, this.partida);
        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    @Test
    void dosEfectosVencenCadaUnoEnSuPaso() {
        Agenda agenda = new Agenda();
        this.heroe.agregarEfecto(new AguaComoPiso());
        this.heroe.agregarEfecto(new Envenenado());
        agenda.programar(5, new QuitarEfecto(TipoDeEfecto.AGUA_COMO_PISO));
        agenda.programar(2, new QuitarEfecto(TipoDeEfecto.ENVENENADO));

        agenda.ejecutarLosQueVencen(2, this.partida);
        assertEquals(1, this.heroe.getEfectosActivos().size());
        assertEquals(TipoDeEfecto.AGUA_COMO_PISO, this.heroe.getEfectosActivos().get(0).getTipo());

        agenda.ejecutarLosQueVencen(5, this.partida);
        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    @Test
    void quedaRegistradoEnLaAgendaConSuPaso() {
        Agenda agenda = new Agenda();
        agenda.programar(20, new QuitarEfecto(TipoDeEfecto.AGUA_COMO_PISO));

        List<EventoProgramado> pendientes = agenda.getPendientes();

        assertEquals(1, pendientes.size());
        assertEquals(20, pendientes.get(0).paso());
        assertTrue(pendientes.get(0).evento() instanceof QuitarEfecto);
    }

    // =================== TIPO DE CADA EFECTO =================== //

    @Test
    void cadaEfectoDiceSuTipo() {
        assertEquals(TipoDeEfecto.AGUA_COMO_PISO, new AguaComoPiso().getTipo());
        assertEquals(TipoDeEfecto.LAVA_COMO_PISO, new LavaComoPiso().getTipo());
        assertEquals(TipoDeEfecto.ENVENENADO, new Envenenado().getTipo());
    }
}
