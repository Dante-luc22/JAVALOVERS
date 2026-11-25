package ar.uba.fi.cb100.tp2.modelo.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Monstruo;
import ar.uba.fi.cb100.tp2.modelo.monstruos.TipoDeMonstruo;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Tests de Partida para usar objetos como acción del turno.
 */
class PartidaObjetosTest {

    private static final Posicion ENTRADA = new Posicion(2, 2);
        private static final Posicion AGUA = new Posicion(3, 2);
    private static final Map<Terreno, Integer> COSTOS =
            Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8);

    private Heroe heroe;
    private Mazmorra mazmorra;

    /** Un monstruo quieto que solo cuenta cuántas veces le tocó jugar. */
    private static class MonstruoContador extends Monstruo {

        private int turnosJugados;

        MonstruoContador(Posicion inicio) {
            super(inicio, 30);
            this.turnosJugados = 0;
        }

        @Override
        public TipoDeMonstruo getTipoDeMonstruo() {
            return TipoDeMonstruo.ERRANTE;
        }

        @Override
        protected Posicion decidirMovimiento(Plano plano, Posicion heroe) {
            this.turnosJugados++;
            return this.getPosicion();
        }

        @Override
        protected void alAlcanzarAlHeroe(Heroe heroe, Agenda agenda) {
        }

        int getTurnosJugados() {
            return this.turnosJugados;
        }
    }

    /**
     * Un piso de 6 x 6: la fila 2 (columnas 2 a 5) es piso y (3,2) es agua.
     */
    private static Plano unPiso() {
        Tablero tablero = new Tablero(6, 6);
        for (int columna = 2; columna <= 5; columna++) {
            tablero.setTerreno(new Posicion(2, columna), Terreno.PISO);
        }
        tablero.setTerreno(AGUA, Terreno.AGUA);
        return new Plano(tablero, ENTRADA, null);
    }

    /** Un solo piso sin escaleras, para probar objetos. */
    private Partida unSoloPiso() {
        Plano plano = new Plano(unPiso().getTablero(), ENTRADA, new Posicion(2, 5));
        this.mazmorra = new Mazmorra(List.of(plano), 1);
        this.heroe = new Heroe(this.mazmorra.getEntrada(), 100);
        return new Partida(this.mazmorra, this.heroe, COSTOS);
    }

    // =================== OBJETOS =================== //

    @Test
    void usarUnObjetoCuentaUnPasoNoCuestaEnergiaYLoSacaDelInventario() {
        Partida partida = unSoloPiso();
        this.heroe.getInventario().agregar(new ZapatosDeAgua());

        ResultadoTurno resultado = partida.usarObjeto(ZapatosDeAgua.ID);

        assertTrue(resultado.accionValida());
        assertEquals(1, partida.getPasos());
        assertEquals(100, this.heroe.getEnergia());
        assertFalse(this.heroe.getInventario().contiene(ZapatosDeAgua.ID));
    }

    @Test
    void usarUnObjetoQueElHeroeNoTieneNoEsValidoNiCuentaComoPaso() {
        Partida partida = unSoloPiso();

        ResultadoTurno resultado = partida.usarObjeto(ZapatosDeAgua.ID);

        assertFalse(resultado.accionValida());
        assertEquals(0, partida.getPasos());
    }

    @Test
    void conLosZapatosDeAguaElAguaCuestaComoPiso() {
        Partida partida = unSoloPiso();
        this.heroe.getInventario().agregar(new ZapatosDeAgua());

        partida.usarObjeto(ZapatosDeAgua.ID);
        partida.moverHeroe(Direccion.ABAJO);

        assertEquals(99, this.heroe.getEnergia());
    }

    @Test
    void sinLosZapatosDeAguaElAguaCuestaTres() {
        Partida partida = unSoloPiso();

        partida.moverHeroe(Direccion.ABAJO);

        assertEquals(97, this.heroe.getEnergia());
    }

    @Test
    void usarUnObjetoTambienLesDaSuTurnoALosMonstruos() {
        Plano plano = new Plano(unPiso().getTablero(), ENTRADA, new Posicion(2, 5));
        MonstruoContador monstruo = new MonstruoContador(new Posicion(2, 5));
        plano.agregarEntidad(monstruo);
        this.mazmorra = new Mazmorra(List.of(plano), 1);
        this.heroe = new Heroe(this.mazmorra.getEntrada(), 100);
        this.heroe.getInventario().agregar(new ZapatosDeAgua());
        Partida partida = new Partida(this.mazmorra, this.heroe, COSTOS);

        partida.usarObjeto(ZapatosDeAgua.ID);

        assertEquals(1, monstruo.getTurnosJugados());
    }
}
