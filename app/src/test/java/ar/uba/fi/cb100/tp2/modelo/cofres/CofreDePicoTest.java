package ar.uba.fi.cb100.tp2.modelo.cofres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.RomperPared;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.objetos.Pico;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests del pico: RomperPared, el objeto Pico y CofreDePico.
 * Tablero de prueba de 5 x 5: el borde es pared; el interior (filas y
 * columnas 2 a 4) es piso salvo las paredes que pone cada test.
 */
class CofreDePicoTest {

    private static final Posicion CENTRO = new Posicion(3, 3);
    private static final Posicion ARRIBA = new Posicion(2, 3);
    private static final Posicion ABAJO = new Posicion(4, 3);
    private static final Posicion IZQUIERDA = new Posicion(3, 2);
    private static final Posicion DERECHA = new Posicion(3, 4);

    private Tablero tablero;
    private Heroe heroe;
    private Partida partida;

    /** Arma el tablero con el interior de piso, menos las paredes pedidas, y el héroe en la posición dada. */
    private void armar(Posicion heroeEn, Posicion... paredes) {
        this.tablero = new Tablero(5, 5);
        for (int fila = 2; fila <= 4; fila++) {
            for (int columna = 2; columna <= 4; columna++) {
                this.tablero.setTerreno(new Posicion(fila, columna), Terreno.PISO);
            }
        }
        for (Posicion pared : paredes) {
            this.tablero.setTerreno(pared, Terreno.PARED);
        }
        Plano plano = new Plano(this.tablero, heroeEn, heroeEn);
        this.heroe = new Heroe(new Ubicacion(1, 1, heroeEn), 100);
        this.partida = new Partida(new Mazmorra(List.of(plano), 1), this.heroe,
                Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8));
    }

    private Terreno en(Posicion posicion) {
        return this.tablero.getTerreno(posicion);
    }

    // =================== ROMPER PARED =================== //

    @Test
    void rompeUnaParedVecinaYLaConvierteEnPiso() {
        armar(CENTRO, DERECHA);

        new RomperPared().ejecutar(this.partida);

        assertEquals(Terreno.PISO, en(DERECHA));
    }

    @Test
    void conVariasParedesRompeLaPrimeraEnElOrdenDeDireccion() {
        armar(CENTRO, ARRIBA, ABAJO, IZQUIERDA, DERECHA);

        new RomperPared().ejecutar(this.partida);

        assertEquals(Terreno.PISO, en(ARRIBA));
        assertEquals(Terreno.PARED, en(ABAJO));
        assertEquals(Terreno.PARED, en(IZQUIERDA));
        assertEquals(Terreno.PARED, en(DERECHA));
    }

    @Test
    void siArribaEsPisoSigueConLaDeAbajo() {
        armar(CENTRO, ABAJO, DERECHA);

        new RomperPared().ejecutar(this.partida);

        assertEquals(Terreno.PISO, en(ABAJO));
        assertEquals(Terreno.PARED, en(DERECHA));
    }

    @Test
    void nuncaRompeElBorde() {
        // En (2,2) arriba y a la izquierda está el borde; abajo y a la derecha, piso.
        Posicion esquina = new Posicion(2, 2);
        armar(esquina);

        new RomperPared().ejecutar(this.partida);

        assertEquals(Terreno.PARED, en(new Posicion(1, 2)));
        assertEquals(Terreno.PARED, en(new Posicion(2, 1)));
    }

    @Test
    void saltaElBordeYRompeUnaParedInterior() {
        // Arriba está el borde; la primera pared interior es la de abajo.
        Posicion esquina = new Posicion(2, 2);
        Posicion debajo = new Posicion(3, 2);
        armar(esquina, debajo);

        new RomperPared().ejecutar(this.partida);

        assertEquals(Terreno.PARED, en(new Posicion(1, 2)));
        assertEquals(Terreno.PISO, en(debajo));
    }

    @Test
    void sinParedesParaRomperNoCambiaNada() {
        armar(CENTRO);

        new RomperPared().ejecutar(this.partida);

        for (int fila = 1; fila <= 5; fila++) {
            for (int columna = 1; columna <= 5; columna++) {
                boolean esBorde = fila == 1 || fila == 5 || columna == 1 || columna == 5;
                Posicion celda = new Posicion(fila, columna);
                assertEquals(esBorde ? Terreno.PARED : Terreno.PISO, en(celda), "Cambio la celda " + celda);
            }
        }
    }

    // =================== EL OBJETO PICO =================== //

    @Test
    void elPicoTieneSuId() {
        assertEquals("PICO", new Pico().getId());
    }

    @Test
    void usarElPicoProgramaRomperParedParaElProximoPaso() {
        armar(CENTRO, DERECHA);
        Agenda agenda = new Agenda();

        new Pico().usar(this.heroe, agenda);

        assertEquals(1, agenda.getPendientes().size());
        assertEquals(1, agenda.getPendientes().get(0).paso());
        assertTrue(agenda.getPendientes().get(0).evento() instanceof RomperPared);
        assertEquals(Terreno.PARED, en(DERECHA), "Rompio la pared en el momento");
    }

    @Test
    void alEmpezarElProximoPasoLaParedEstaRota() {
        armar(CENTRO, DERECHA);
        Agenda agenda = new Agenda();
        new Pico().usar(this.heroe, agenda);

        agenda.ejecutarLosQueVencen(1, this.partida);

        assertEquals(Terreno.PISO, en(DERECHA));
    }

    @Test
    void elPicoSeGastaAunqueNoHayaParedes() {
        armar(CENTRO);
        this.heroe.getInventario().agregar(new Pico());

        this.heroe.getInventario().usar(Pico.ID, this.heroe, new Agenda());

        assertFalse(this.heroe.getInventario().contiene(Pico.ID));
    }

    // =================== EL COFRE =================== //

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class, () -> new CofreDePico(null));
    }

    @Test
    void alAbrirseDejaElPicoEnElInventarioSinRomperNada() {
        armar(CENTRO, DERECHA);
        Agenda agenda = new Agenda();

        new CofreDePico(CENTRO).alEncontrarseConHeroe(this.heroe, agenda);

        assertEquals(1, this.heroe.getInventario().cantidad(Pico.ID));
        assertTrue(agenda.getPendientes().isEmpty());
        assertEquals(Terreno.PARED, en(DERECHA));
    }

    @Test
    void alAbrirseSumaElCofreYQuedaParaSacarDelPlano() {
        armar(CENTRO);
        CofreDePico cofre = new CofreDePico(CENTRO);

        cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(1, this.heroe.getCofresAbiertos());
        assertTrue(cofre.debeSalirDelPlano());
    }

    @Test
    void sePuedeRegistrarEnElCatalogo() {
        CatalogoDeCofres catalogo = new CatalogoDeCofres();
        catalogo.registrar("PICO", CofreDePico::new);

        assertTrue(catalogo.crearAlAzar(CENTRO, new Random(1)) instanceof CofreDePico);
    }
}
