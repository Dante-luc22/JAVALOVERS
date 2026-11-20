package ar.uba.fi.cb100.tp2.modelo.cofres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.efectos.Envenenado;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests del par CofreDeVeneno + Envenenado: al abrirse, el cofre envenena al
 * héroe (no va al inventario) y durante 10 pasos cada movimiento cuesta 1 más.
 * Costos de prueba: piso 1, agua 3, lava 8.
 */
class CofreDeVenenoTest {

    private static final Posicion LUGAR = new Posicion(3, 3);
    private static final int PISO = 1;
    private static final int AGUA = 3;
    private static final int LAVA = 8;

    private Heroe heroe;
    private CofreDeVeneno cofre;

    @BeforeEach
    void crear() {
        this.heroe = new Heroe(new Ubicacion(0, 0, LUGAR), 100);
        this.cofre = new CofreDeVeneno(LUGAR);
    }

    // =================== EL EFECTO =================== //

    @Test
    void envenenadoSumaUnoEnCualquierTerreno() {
        Envenenado veneno = new Envenenado(10);
        assertEquals(PISO + 1, veneno.ajustarCosto(Terreno.PISO, PISO, PISO));
        assertEquals(AGUA + 1, veneno.ajustarCosto(Terreno.AGUA, AGUA, PISO));
        assertEquals(LAVA + 1, veneno.ajustarCosto(Terreno.LAVA, LAVA, PISO));
    }

    @Test
    void envenenadoRechazaDuracionNoPositiva() {
        assertThrows(IllegalArgumentException.class, () -> new Envenenado(0));
    }

    // =================== EL COFRE =================== //

    @Test
    void recienCreadoEstaEnSuPosicionYCerrado() {
        assertEquals(LUGAR, this.cofre.getPosicion());
        assertFalse(this.cofre.debeSalirDelPlano());
    }

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class, () -> new CofreDeVeneno(null));
    }

    @Test
    void alAbrirseEnvenenaEnseguida() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(PISO + 1, this.heroe.ajustarCosto(Terreno.PISO, PISO, PISO));
    }

    @Test
    void noVaAlInventario() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertTrue(this.heroe.getInventario().estaVacio());
    }

    @Test
    void alAbrirseNoQuitaEnergiaDeInmediato() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(100, this.heroe.getEnergia());
    }

    @Test
    void aunqueSeaTrampaSumaElCofreYQuedaParaSacarDelPlano() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(1, this.heroe.getCofresAbiertos());
        assertTrue(this.cofre.debeSalirDelPlano());
    }

    @Test
    void elVenenoDura10Pasos() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        for (int paso = 1; paso <= 9; paso++) {
            this.heroe.pasarUnPaso();
        }
        assertEquals(PISO + 1, this.heroe.ajustarCosto(Terreno.PISO, PISO, PISO));

        this.heroe.pasarUnPaso();
        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.PISO, PISO, PISO));
    }

    @Test
    void conZapatosYVenenoElAguaCuestaPisoMasUno() {
        new ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua().usar(this.heroe, new Agenda());
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(PISO + 1, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void sePuedeRegistrarEnElCatalogo() {
        CatalogoDeCofres catalogo = new CatalogoDeCofres();
        catalogo.registrar("VENENO", CofreDeVeneno::new);

        assertTrue(catalogo.crearAlAzar(LUGAR, new Random(1)) instanceof CofreDeVeneno);
    }
}
