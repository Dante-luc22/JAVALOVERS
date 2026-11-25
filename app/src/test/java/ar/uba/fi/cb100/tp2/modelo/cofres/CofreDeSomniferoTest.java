package ar.uba.fi.cb100.tp2.modelo.cofres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.AdormecerMonstruos;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.DespertarMonstruos;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Cazador;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Ladron;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de CofreDeSomnifero, de sus eventos y del adormecimiento de Monstruo.
 * Decisiones del grupo que se prueban: un monstruo adormecido no se mueve ni
 * ataca; si el héroe lo pisa, se despierta del todo sin hacerle daño; dos
 * somníferos se suman (contador).
 *
 * Plano de prueba: un pasillo en la fila 2, columnas 1 a 7; el héroe en (2,4)
 * y un cazador que arranca en (2,1).
 */
class CofreDeSomniferoTest {

    private static final Posicion HEROE = new Posicion(2, 4);
    private static final Posicion INICIO = new Posicion(2, 1);
    private static final int DURACION = 5;
    private static final Map<Terreno, Integer> COSTOS =
            Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8);

    private Heroe heroe;
    private Plano plano;
    private Partida partida;
    private Cazador cazador;
    private CofreDeSomnifero cofre;

    @BeforeEach
    void crear() {
        Tablero tablero = new Tablero(3, 7);
        for (int columna = 1; columna <= 7; columna++) {
            tablero.setTerreno(new Posicion(2, columna), Terreno.PISO);
        }
        this.plano = new Plano(tablero, INICIO, new Posicion(2, 7));
        this.heroe = new Heroe(new Ubicacion(1, 1, HEROE), 100);
        this.partida = new Partida(new Mazmorra(List.of(this.plano), 1), this.heroe,
                Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8));

        this.cazador = new Cazador(INICIO, 30, COSTOS);
        this.cofre = new CofreDeSomnifero(HEROE);
        this.plano.agregarEntidad(this.cazador);
        this.plano.agregarEntidad(this.cofre);
    }

    // =================== EL CONTADOR DE MONSTRUO =================== //

    @Test
    void recienCreadoEstaDespierto() {
        assertFalse(this.cazador.estaAdormecido());
    }

    @Test
    void adormecerYDespertar() {
        this.cazador.adormecer();
        assertTrue(this.cazador.estaAdormecido());

        this.cazador.despertar();
        assertFalse(this.cazador.estaAdormecido());
    }

    @Test
    void despertarAUnoDespiertoNoFallaYSigueDespierto() {
        this.cazador.despertar();
        this.cazador.despertar();

        assertFalse(this.cazador.estaAdormecido());
        this.cazador.adormecer();
        assertTrue(this.cazador.estaAdormecido(), "El contador quedo negativo");
    }

    @Test
    void dosAdormecimientosNecesitanDosDespertares() {
        this.cazador.adormecer();
        this.cazador.adormecer();

        this.cazador.despertar();
        assertTrue(this.cazador.estaAdormecido());

        this.cazador.despertar();
        assertFalse(this.cazador.estaAdormecido());
    }

    // =================== ADORMECIDO =================== //

    @Test
    void adormecidoNoSeMueve() {
        this.cazador.adormecer();

        this.cazador.actuar(this.plano, this.heroe);

        assertEquals(INICIO, this.cazador.getPosicion());
    }

    @Test
    void despiertoVuelveAMoverse() {
        this.cazador.adormecer();
        this.cazador.despertar();

        this.cazador.actuar(this.plano, this.heroe);

        assertEquals(new Posicion(2, 2), this.cazador.getPosicion());
    }

    @Test
    void siElHeroeLoPisaSeDespiertaDelTodoSinHacerleDanio() {
        this.cazador.adormecer();
        this.cazador.adormecer();

        this.cazador.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertFalse(this.cazador.estaAdormecido());
        assertEquals(100, this.heroe.getEnergia());
        assertEquals(INICIO, this.cazador.getPosicion());
    }

    @Test
    void despiertoAtacaNormalmente() {
        this.cazador.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(70, this.heroe.getEnergia());
    }

    @Test
    void unLadronAdormecidoNoRoba() {
        Ladron ladron = new Ladron(INICIO, 30, new Random(1));
        this.heroe.getInventario().agregar(new ZapatosDeAgua());
        ladron.adormecer();

        ladron.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(1, this.heroe.getInventario().cantidad(ZapatosDeAgua.ID));
        assertFalse(ladron.estaAdormecido());
    }

    // =================== LOS EVENTOS =================== //

    @Test
    void adormecerMonstruosDuermeATodosLosMonstruosDelPlano() {
        Cazador otro = new Cazador(new Posicion(2, 7), 30, COSTOS);
        this.plano.agregarEntidad(otro);

        new AdormecerMonstruos().ejecutar(this.partida);

        assertTrue(this.cazador.estaAdormecido());
        assertTrue(otro.estaAdormecido());
    }

    @Test
    void despertarMonstruosLosDespierta() {
        new AdormecerMonstruos().ejecutar(this.partida);

        new DespertarMonstruos().ejecutar(this.partida);

        assertFalse(this.cazador.estaAdormecido());
    }

    // =================== EL COFRE =================== //

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class, () -> new CofreDeSomnifero(null));
    }

    @Test
    void alAbrirseProgramaDormirEnElPaso1YDespertarEnElPaso6() {
        Agenda agenda = new Agenda();

        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);

        assertEquals(2, agenda.getPendientes().size());
        assertEquals(1, agenda.getPendientes().get(0).paso());
        assertTrue(agenda.getPendientes().get(0).evento() instanceof AdormecerMonstruos);
        assertEquals(1 + DURACION, agenda.getPendientes().get(1).paso());
        assertTrue(agenda.getPendientes().get(1).evento() instanceof DespertarMonstruos);
    }

    @Test
    void alAbrirseNoLosDuermeEnEseMomento() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertFalse(this.cazador.estaAdormecido());
    }

    @Test
    void duermenDuranteCincoPasosYDespuesSeDespiertan() {
        Agenda agenda = new Agenda();
        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);

        agenda.ejecutarLosQueVencen(1, this.partida);
        assertTrue(this.cazador.estaAdormecido());

        agenda.ejecutarLosQueVencen(DURACION, this.partida);
        assertTrue(this.cazador.estaAdormecido());

        agenda.ejecutarLosQueVencen(1 + DURACION, this.partida);
        assertFalse(this.cazador.estaAdormecido());
        assertTrue(agenda.getPendientes().isEmpty());
    }

    @Test
    void dosSomniferosSuperpuestosNoDespiertanAntesDeTiempo() {
        // El primero duerme del paso 1 al 6; el segundo, abierto en el paso 3,
        // del 4 al 9. Con un boolean se despertarían en el 6.
        Agenda agenda = new Agenda();
        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);
        agenda.ejecutarLosQueVencen(3, this.partida);
        new CofreDeSomnifero(HEROE).alEncontrarseConHeroe(this.heroe, agenda);

        agenda.ejecutarLosQueVencen(6, this.partida);
        assertTrue(this.cazador.estaAdormecido(), "Se desperto con el primer somnifero");

        agenda.ejecutarLosQueVencen(9, this.partida);
        assertFalse(this.cazador.estaAdormecido());
    }

    @Test
    void siElHeroeLoPisaAntesDeTiempoElDespertarDeLaAgendaNoFalla() {
        Agenda agenda = new Agenda();
        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);
        agenda.ejecutarLosQueVencen(1, this.partida);

        this.cazador.alEncontrarseConHeroe(this.heroe, agenda);
        assertFalse(this.cazador.estaAdormecido());

        agenda.ejecutarLosQueVencen(1 + DURACION, this.partida);
        assertFalse(this.cazador.estaAdormecido());
        this.cazador.adormecer();
        assertTrue(this.cazador.estaAdormecido(), "El contador quedo negativo");
    }

    @Test
    void sumaElCofreSinTocarLaEnergiaYQuedaParaSacarDelPlano() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(1, this.heroe.getCofresAbiertos());
        assertEquals(100, this.heroe.getEnergia());
        assertTrue(this.cofre.debeSalirDelPlano());
    }

    @Test
    void sePuedeRegistrarEnElCatalogo() {
        CatalogoDeCofres catalogo = new CatalogoDeCofres();
        catalogo.registrar("SOMNIFERO", CofreDeSomnifero::new);

        assertTrue(catalogo.crearAlAzar(HEROE, new Random(1)) instanceof CofreDeSomnifero);
    }
}
