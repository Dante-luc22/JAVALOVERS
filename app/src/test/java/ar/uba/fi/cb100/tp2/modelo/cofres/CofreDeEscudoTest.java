package ar.uba.fi.cb100.tp2.modelo.cofres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Cazador;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Ladron;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de CofreDeEscudo y del escudo del héroe.
 * Decisiones del grupo que se prueban: el escudo se activa al abrir el cofre
 * (no va al inventario); bloquea el próximo encuentro con un monstruo (ataque
 * o robo) y el monstruo vuelve a su inicio; no protege de las trampas; los
 * escudos se acumulan.
 *
 * Plano de prueba: un pasillo en la fila 2, columnas 1 a 5; el héroe en (2,3)
 * y un cazador que arranca en (2,1).
 */
class CofreDeEscudoTest {

    private static final Posicion HEROE = new Posicion(2, 3);
    private static final Posicion INICIO = new Posicion(2, 1);
    private static final int DANIO = 30;

    private Heroe heroe;
    private Plano plano;
    private Cazador cazador;
    private CofreDeEscudo cofre;

    @BeforeEach
    void crear() {
        Tablero tablero = new Tablero(3, 5);
        for (int columna = 1; columna <= 5; columna++) {
            tablero.setTerreno(new Posicion(2, columna), Terreno.PISO);
        }
        this.plano = new Plano(tablero, INICIO, new Posicion(2, 5));
        this.heroe = new Heroe(new Ubicacion(1, 1, HEROE), 100);
        this.cazador = new Cazador(INICIO, DANIO);
        this.cofre = new CofreDeEscudo(HEROE);
    }

    /** El cazador avanza hasta la celda del héroe (2 pasos). */
    private void cazadorLlegaAlHeroe() {
        this.cazador.actuar(this.plano, this.heroe);
        this.cazador.actuar(this.plano, this.heroe);
        assertEquals(HEROE, this.cazador.getPosicion());
    }

    // =================== EL ESCUDO DEL HÉROE =================== //

    @Test
    void elHeroeArrancaSinEscudo() {
        assertFalse(this.heroe.tieneEscudo());
        assertEquals(0, this.heroe.getEscudos());
    }

    @Test
    void agregarYUsarEscudo() {
        this.heroe.agregarEscudo();
        assertTrue(this.heroe.tieneEscudo());

        this.heroe.usarEscudo();
        assertFalse(this.heroe.tieneEscudo());
    }

    @Test
    void usarEscudoSinTenerLanzaExcepcionYNoQuedaNegativo() {
        assertThrows(IllegalStateException.class, () -> this.heroe.usarEscudo());

        assertEquals(0, this.heroe.getEscudos());
        this.heroe.agregarEscudo();
        assertTrue(this.heroe.tieneEscudo(), "El contador quedo negativo");
    }

    @Test
    void losEscudosSeAcumulan() {
        this.heroe.agregarEscudo();
        this.heroe.agregarEscudo();

        this.heroe.usarEscudo();

        assertTrue(this.heroe.tieneEscudo());
        assertEquals(1, this.heroe.getEscudos());
    }

    // =================== CONTRA LOS MONSTRUOS =================== //

    @Test
    void conEscudoElMonstruoNoLeQuitaEnergia() {
        this.heroe.agregarEscudo();
        cazadorLlegaAlHeroe();

        this.cazador.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(100, this.heroe.getEnergia());
    }

    @Test
    void elEscudoSeGastaYElMonstruoVuelveASuInicio() {
        this.heroe.agregarEscudo();
        cazadorLlegaAlHeroe();

        this.cazador.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertFalse(this.heroe.tieneEscudo());
        assertEquals(INICIO, this.cazador.getPosicion());
    }

    @Test
    void sinEscudoElSegundoEncuentroSiHaceDanio() {
        this.heroe.agregarEscudo();
        this.cazador.alEncontrarseConHeroe(this.heroe, new Agenda());

        this.cazador.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(100 - DANIO, this.heroe.getEnergia());
    }

    @Test
    void dosEscudosBloqueanDosEncuentros() {
        this.heroe.agregarEscudo();
        this.heroe.agregarEscudo();

        this.cazador.alEncontrarseConHeroe(this.heroe, new Agenda());
        this.cazador.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(100, this.heroe.getEnergia());
        assertFalse(this.heroe.tieneEscudo());
    }

    @Test
    void conEscudoElLadronNoRoba() {
        Ladron ladron = new Ladron(INICIO, DANIO, new Random(1));
        this.heroe.getInventario().agregar(new ZapatosDeAgua());
        this.heroe.agregarEscudo();

        ladron.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(1, this.heroe.getInventario().cantidad(ZapatosDeAgua.ID));
        assertFalse(this.heroe.tieneEscudo());
    }

    @Test
    void conUnMonstruoAdormecidoNoSeGastaElEscudo() {
        // Primero se chequea si está adormecido: lo despierta y no hace nada más.
        this.heroe.agregarEscudo();
        this.cazador.adormecer();

        this.cazador.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertTrue(this.heroe.tieneEscudo());
        assertFalse(this.cazador.estaAdormecido());
        assertEquals(100, this.heroe.getEnergia());
    }

    @Test
    void elEscudoNoProtegeDeLasTrampas() {
        this.heroe.agregarEscudo();

        new CofreDeTrampaDePinchos(HEROE, 15).alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(85, this.heroe.getEnergia());
        assertTrue(this.heroe.tieneEscudo());
    }

    // =================== EL COFRE =================== //

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class, () -> new CofreDeEscudo(null));
    }

    @Test
    void alAbrirseElHeroeQuedaConEscudoEnEseMomento() {
        Agenda agenda = new Agenda();

        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);

        assertTrue(this.heroe.tieneEscudo());
        assertTrue(agenda.getPendientes().isEmpty());
    }

    @Test
    void noVaAlInventario() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertTrue(this.heroe.getInventario().estaVacio());
    }

    @Test
    void dosCofresDanDosEscudos() {
        Agenda agenda = new Agenda();
        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);
        new CofreDeEscudo(new Posicion(2, 4)).alEncontrarseConHeroe(this.heroe, agenda);

        assertEquals(2, this.heroe.getEscudos());
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
        catalogo.registrar("ESCUDO", CofreDeEscudo::new);

        assertTrue(catalogo.crearAlAzar(HEROE, new Random(1)) instanceof CofreDeEscudo);
    }
}
