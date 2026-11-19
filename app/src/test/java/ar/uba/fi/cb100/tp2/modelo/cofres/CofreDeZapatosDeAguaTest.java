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
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests del par CofreDeZapatosDeAgua + ZapatosDeAgua: el cofre deja los
 * zapatos en el inventario, y usarlos hace que el agua cueste como el piso
 * durante 20 pasos. Costos de prueba: piso 1, agua 3.
 */
class CofreDeZapatosDeAguaTest {

    private static final Posicion LUGAR = new Posicion(3, 3);
    private static final int PISO = 1;
    private static final int AGUA = 3;

    private Heroe heroe;
    private CofreDeZapatosDeAgua cofre;

    @BeforeEach
    void crear() {
        this.heroe = new Heroe(new Ubicacion(0, 0, LUGAR), 100);
        this.cofre = new CofreDeZapatosDeAgua(LUGAR);
    }

    // =================== EL COFRE =================== //

    @Test
    void recienCreadoEstaEnSuPosicionYCerrado() {
        assertEquals(LUGAR, this.cofre.getPosicion());
        assertFalse(this.cofre.debeSalirDelPlano());
    }

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class, () -> new CofreDeZapatosDeAgua(null));
    }

    @Test
    void alAbrirseDejaLosZapatosEnElInventario() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(1, this.heroe.getInventario().cantidad(ZapatosDeAgua.ID));
    }

    @Test
    void alAbrirseNoAplicaElEfectoTodavia() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertTrue(this.heroe.getEfectosActivos().isEmpty());
        assertEquals(AGUA, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }

    @Test
    void alAbrirseSumaElCofreYQuedaParaSacarDelPlano() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(1, this.heroe.getCofresAbiertos());
        assertTrue(this.cofre.debeSalirDelPlano());
    }

    @Test
    void dosCofresDejanDosZapatos() {
        Agenda agenda = new Agenda();
        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);
        new CofreDeZapatosDeAgua(new Posicion(3, 4)).alEncontrarseConHeroe(this.heroe, agenda);

        assertEquals(2, this.heroe.getInventario().cantidad(ZapatosDeAgua.ID));
    }

    @Test
    void sePuedeRegistrarEnElCatalogo() {
        CatalogoDeCofres catalogo = new CatalogoDeCofres();
        catalogo.registrar("ZAPATOS_DE_AGUA", CofreDeZapatosDeAgua::new);

        Cofre creado = catalogo.crearAlAzar(LUGAR, new Random(1));

        assertTrue(creado instanceof CofreDeZapatosDeAgua);
    }

    // =================== LOS ZAPATOS =================== //

    @Test
    void losZapatosTienenSuId() {
        assertEquals("ZAPATOS_DE_AGUA", new ZapatosDeAgua().getId());
    }

    @Test
    void usarlosHaceQueElAguaCuesteComoElPiso() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        this.heroe.getInventario().usar(ZapatosDeAgua.ID, this.heroe, new Agenda());

        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
        assertFalse(this.heroe.getInventario().contiene(ZapatosDeAgua.ID));
    }

    @Test
    void elEfectoDura20Pasos() {
        new ZapatosDeAgua().usar(this.heroe, new Agenda());

        for (int paso = 1; paso <= 19; paso++) {
            this.heroe.pasarUnPaso();
        }
        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));

        this.heroe.pasarUnPaso();
        assertEquals(AGUA, this.heroe.ajustarCosto(Terreno.AGUA, AGUA, PISO));
    }
}
