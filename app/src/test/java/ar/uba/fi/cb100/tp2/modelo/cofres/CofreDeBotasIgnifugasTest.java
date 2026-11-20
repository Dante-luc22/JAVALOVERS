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
import ar.uba.fi.cb100.tp2.modelo.objetos.BotasIgnifugas;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests del par CofreDeBotasIgnifugas + BotasIgnifugas: el cofre deja las
 * botas en el inventario, y usarlas hace que la lava cueste como el piso
 * durante 20 pasos. Costos de prueba: piso 1, agua 3, lava 8.
 */
class CofreDeBotasIgnifugasTest {

    private static final Posicion LUGAR = new Posicion(3, 3);
    private static final int PISO = 1;
    private static final int LAVA = 8;

    private Heroe heroe;
    private CofreDeBotasIgnifugas cofre;

    @BeforeEach
    void crear() {
        this.heroe = new Heroe(new Ubicacion(0, 0, LUGAR), 100);
        this.cofre = new CofreDeBotasIgnifugas(LUGAR);
    }

    // =================== EL COFRE =================== //

    @Test
    void recienCreadoEstaEnSuPosicionYCerrado() {
        assertEquals(LUGAR, this.cofre.getPosicion());
        assertFalse(this.cofre.debeSalirDelPlano());
    }

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class, () -> new CofreDeBotasIgnifugas(null));
    }

    @Test
    void alAbrirseDejaLasBotasEnElInventario() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(1, this.heroe.getInventario().cantidad(BotasIgnifugas.ID));
    }

    @Test
    void alAbrirseNoAplicaElEfectoTodavia() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertTrue(this.heroe.getEfectosActivos().isEmpty());
        assertEquals(LAVA, this.heroe.ajustarCosto(Terreno.LAVA, LAVA, PISO));
    }

    @Test
    void alAbrirseSumaElCofreYQuedaParaSacarDelPlano() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(1, this.heroe.getCofresAbiertos());
        assertTrue(this.cofre.debeSalirDelPlano());
    }

    @Test
    void dosCofresDejanDosBotas() {
        Agenda agenda = new Agenda();
        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);
        new CofreDeBotasIgnifugas(new Posicion(3, 4)).alEncontrarseConHeroe(this.heroe, agenda);

        assertEquals(2, this.heroe.getInventario().cantidad(BotasIgnifugas.ID));
    }

    @Test
    void sePuedeRegistrarEnElCatalogo() {
        CatalogoDeCofres catalogo = new CatalogoDeCofres();
        catalogo.registrar("BOTAS_IGNIFUGAS", CofreDeBotasIgnifugas::new);

        Cofre creado = catalogo.crearAlAzar(LUGAR, new Random(1));

        assertTrue(creado instanceof CofreDeBotasIgnifugas);
    }

    // =================== LAS BOTAS =================== //

    @Test
    void lasBotasTienenSuId() {
        assertEquals("BOTAS_IGNIFUGAS", new BotasIgnifugas().getId());
    }

    @Test
    void usarlasHaceQueLaLavaCuesteComoElPiso() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        this.heroe.getInventario().usar(BotasIgnifugas.ID, this.heroe, new Agenda());

        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.LAVA, LAVA, PISO));
        assertFalse(this.heroe.getInventario().contiene(BotasIgnifugas.ID));
    }

    @Test
    void noCambianElCostoDelAgua() {
        new BotasIgnifugas().usar(this.heroe, new Agenda());

        assertEquals(3, this.heroe.ajustarCosto(Terreno.AGUA, 3, PISO));
    }

    @Test
    void conBotasYZapatosNiElAguaNiLaLavaCuestanMasQueElPiso() {
        new BotasIgnifugas().usar(this.heroe, new Agenda());
        new ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua().usar(this.heroe, new Agenda());

        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.LAVA, LAVA, PISO));
        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.AGUA, 3, PISO));
    }

    @Test
    void elEfectoDura20Pasos() {
        new BotasIgnifugas().usar(this.heroe, new Agenda());

        for (int paso = 1; paso <= 19; paso++) {
            this.heroe.pasarUnPaso();
        }
        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.LAVA, LAVA, PISO));

        this.heroe.pasarUnPaso();
        assertEquals(LAVA, this.heroe.ajustarCosto(Terreno.LAVA, LAVA, PISO));
    }
}
