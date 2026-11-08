package ar.uba.fi.cb100.tp2.modelo.cofres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de CofreDePocion: devuelve energía, sin pasarse del máximo.
 */
class CofreDePocionTest {

    private static final Posicion LUGAR = new Posicion(3, 3);
    private static final int CURA = 25;

    private Heroe heroe;
    private CofreDePocion pocion;

    @BeforeEach
    void crear() {
        this.heroe = new Heroe(new Ubicacion(0, 0, LUGAR), 100);
        this.pocion = new CofreDePocion(LUGAR, CURA);
    }

    // =================== CONSTRUCTOR =================== //

    @Test
    void recienCreadoEstaEnSuPosicionYCerrado() {
        assertEquals(LUGAR, this.pocion.getPosicion());
        assertEquals(false, this.pocion.debeSalirDelPlano());
    }

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new CofreDePocion(null, CURA));
    }

    @Test
    void constructorRechazaCantidadCero() {
        assertThrows(IllegalArgumentException.class,
                () -> new CofreDePocion(LUGAR, 0));
    }

    @Test
    void constructorRechazaCantidadNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> new CofreDePocion(LUGAR, -10));
    }

    // =================== AL ABRIRSE =================== //

    @Test
    void alAbrirseDevuelveLaEnergia() {
        this.heroe.restarEnergia(50);

        this.pocion.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(75, this.heroe.getEnergia());
    }

    @Test
    void alAbrirseNoPasaDeLaEnergiaMaxima() {
        this.heroe.restarEnergia(10);

        this.pocion.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(100, this.heroe.getEnergia());
    }

    @Test
    void conEnergiaMaximaMayorA100CuraLaCantidadCompleta() {
        Heroe fuerte = new Heroe(new Ubicacion(0, 0, LUGAR), 150);
        fuerte.restarEnergia(90);

        new CofreDePocion(LUGAR, 50).alEncontrarseConHeroe(fuerte, new Agenda());

        assertEquals(110, fuerte.getEnergia());
    }

    @Test
    void alAbrirseSumaElCofreAbierto() {
        this.pocion.alEncontrarseConHeroe(this.heroe, new Agenda());
        assertEquals(1, this.heroe.getCofresAbiertos());
    }

    @Test
    void alAbrirseQuedaParaSacarDelPlano() {
        this.pocion.alEncontrarseConHeroe(this.heroe, new Agenda());
        assertTrue(this.pocion.debeSalirDelPlano());
    }

    @Test
    void alEncontrarseRechazaHeroeNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> this.pocion.alEncontrarseConHeroe(null, new Agenda()));
    }

    @Test
    void sePuedeRegistrarEnElCatalogoConSuCantidad() {
        CatalogoDeCofres catalogo = new CatalogoDeCofres();
        catalogo.registrar("POCION", posicion -> new CofreDePocion(posicion, CURA));
        this.heroe.restarEnergia(50);

        Cofre creado = catalogo.crearAlAzar(LUGAR, new java.util.Random(1));
        creado.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertTrue(creado instanceof CofreDePocion);
        assertEquals(75, this.heroe.getEnergia());
    }
}
