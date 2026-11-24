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

import ar.uba.fi.cb100.tp2.modelo.efectos.Envenenado;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
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
        this.heroe = new Heroe(new Ubicacion(1, 1, LUGAR), 100);
        this.cofre = new CofreDeVeneno(LUGAR);
    }

    /** Una partida mínima (un plano con una celda de piso) para que la agenda pueda ejecutar eventos. */
    private Partida partidaMinima() {
        Posicion celda = new Posicion(2, 2);
        Tablero tablero = new Tablero(3, 3);
        tablero.setTerreno(celda, Terreno.PISO);
        Plano plano = new Plano(tablero, celda, celda);
        return new Partida(new Mazmorra(List.of(plano), 1), this.heroe,
                Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8));
    }

    // =================== EL EFECTO =================== //

    @Test
    void envenenadoSumaUnoEnCualquierTerreno() {
        Envenenado veneno = new Envenenado();
        assertEquals(PISO + 1, veneno.ajustarCosto(Terreno.PISO, PISO, PISO));
        assertEquals(AGUA + 1, veneno.ajustarCosto(Terreno.AGUA, AGUA, PISO));
        assertEquals(LAVA + 1, veneno.ajustarCosto(Terreno.LAVA, LAVA, PISO));
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
        Agenda agenda = new Agenda();
        Partida partida = partidaMinima();
        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);

        agenda.ejecutarLosQueVencen(9, partida);
        assertEquals(PISO + 1, this.heroe.ajustarCosto(Terreno.PISO, PISO, PISO));

        agenda.ejecutarLosQueVencen(10, partida);
        assertEquals(PISO, this.heroe.ajustarCosto(Terreno.PISO, PISO, PISO));
        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    @Test
    void programaSuVencimientoEnLaAgenda() {
        Agenda agenda = new Agenda();
        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);

        assertEquals(1, agenda.getPendientes().size());
        assertEquals(10, agenda.getPendientes().get(0).paso());
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
