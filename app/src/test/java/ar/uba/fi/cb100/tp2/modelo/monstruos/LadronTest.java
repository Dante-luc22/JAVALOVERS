package ar.uba.fi.cb100.tp2.modelo.monstruos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.objetos.BotasIgnifugas;
import ar.uba.fi.cb100.tp2.modelo.objetos.Inventario;
import ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de Ladron: se mueve como un errante; al alcanzar al héroe le roba un
 * objeto al azar y vuelve al inicio. Si el héroe no tiene nada, lo ataca como
 * un errante.
 */
class LadronTest {

    private static final int DANIO = 30;
    private static final Posicion INICIO = new Posicion(3, 3);
    private static final Posicion HEROE = new Posicion(3, 4);

    private Heroe heroe;
    private Ladron ladron;

    @BeforeEach
    void crear() {
        this.heroe = new Heroe(new Ubicacion(0, 0, HEROE), 100);
        this.ladron = new Ladron(INICIO, DANIO, new Random(1));
    }

    /** Un pasillo de dos celdas: el ladrón solo puede ir a la del héroe. */
    private static Plano pasillo() {
        Tablero tablero = new Tablero(5, 5);
        tablero.setTerreno(INICIO, Terreno.PISO);
        tablero.setTerreno(HEROE, Terreno.PISO);
        return new Plano(tablero, INICIO, null);
    }

    private static int cantidadTotal(Inventario inventario) {
        return inventario.cantidad(ZapatosDeAgua.ID) + inventario.cantidad(BotasIgnifugas.ID);
    }

    // =================== CONSTRUCTOR Y MOVIMIENTO =================== //

    @Test
    void esUnErrante() {
        assertTrue(this.ladron instanceof Errante);
    }

    @Test
    void constructorRechazaAzarNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Ladron(INICIO, DANIO, null));
    }

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class, () -> new Ladron(null, DANIO, new Random(1)));
    }

    @Test
    void seMueveComoUnErrante() {
        Plano plano = pasillo();
        List<Posicion> vecinas = plano.getTablero().vecinasTransitables(INICIO);

        this.ladron.actuar(plano, this.heroe);

        assertTrue(vecinas.contains(this.ladron.getPosicion()));
    }

    // =================== ROBA =================== //

    @Test
    void alAlcanzarAlHeroeLeRobaUnObjeto() {
        this.heroe.getInventario().agregar(new ZapatosDeAgua());

        this.ladron.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertTrue(this.heroe.getInventario().estaVacio());
    }

    @Test
    void robaUnoSoloAunqueTengaVarios() {
        this.heroe.getInventario().agregar(new ZapatosDeAgua());
        this.heroe.getInventario().agregar(new ZapatosDeAgua());
        this.heroe.getInventario().agregar(new BotasIgnifugas());

        this.ladron.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(2, cantidadTotal(this.heroe.getInventario()));
    }

    @Test
    void siRobaNoLeQuitaEnergia() {
        this.heroe.getInventario().agregar(new ZapatosDeAgua());

        this.ladron.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(100, this.heroe.getEnergia());
    }

    @Test
    void despuesDeRobarVuelveAlInicio() {
        this.heroe.getInventario().agregar(new ZapatosDeAgua());
        this.ladron.actuar(pasillo(), this.heroe);
        assertEquals(HEROE, this.ladron.getPosicion());

        this.ladron.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(INICIO, this.ladron.getPosicion());
    }

    @Test
    void noLeRobaDosVecesSeguidas() {
        // Si se quedara encima del héroe, en el paso siguiente le robaría otra vez.
        this.heroe.getInventario().agregar(new ZapatosDeAgua());
        this.heroe.getInventario().agregar(new BotasIgnifugas());
        Plano plano = pasillo();

        this.ladron.actuar(plano, this.heroe);
        this.ladron.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertTrue(!this.ladron.getPosicion().equals(HEROE),
                "Se quedo encima del heroe despues de robar");
        assertEquals(1, cantidadTotal(this.heroe.getInventario()));
    }

    @Test
    void conLaMismaSemillaRobaLoMismo() {
        Heroe otroHeroe = new Heroe(new Ubicacion(0, 0, HEROE), 100);
        for (Heroe cada : new Heroe[] {this.heroe, otroHeroe}) {
            cada.getInventario().agregar(new ZapatosDeAgua());
            cada.getInventario().agregar(new BotasIgnifugas());
        }
        Ladron otroLadron = new Ladron(INICIO, DANIO, new Random(1));

        this.ladron.alEncontrarseConHeroe(this.heroe, new Agenda());
        otroLadron.alEncontrarseConHeroe(otroHeroe, new Agenda());

        assertEquals(this.heroe.getInventario().cantidad(ZapatosDeAgua.ID),
                otroHeroe.getInventario().cantidad(ZapatosDeAgua.ID));
    }

    // =================== INVENTARIO VACÍO =================== //

    @Test
    void conElInventarioVacioLoAtacaComoUnErrante() {
        this.ladron.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(100 - DANIO, this.heroe.getEnergia());
    }

    @Test
    void conElInventarioVacioTambienVuelveAlInicio() {
        this.ladron.actuar(pasillo(), this.heroe);

        this.ladron.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(INICIO, this.ladron.getPosicion());
    }

    @Test
    void alEncontrarseRechazaHeroeNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> this.ladron.alEncontrarseConHeroe(null, new Agenda()));
    }

    @Test
    void sePuedeRegistrarEnElCatalogo() {
        Random azar = new Random(1);
        CatalogoDeMonstruos catalogo = new CatalogoDeMonstruos();
        catalogo.registrar("LADRON", posicion -> new Ladron(posicion, DANIO, azar));

        Monstruo creado = catalogo.crearAlAzar(INICIO, azar);

        assertTrue(creado instanceof Ladron);
    }
}
