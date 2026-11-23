package ar.uba.fi.cb100.tp2.modelo.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Monstruo;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de Partida para las escaleras (pisar una conexión traslada al héroe
 * una sola vez y el turno sigue en el plano de destino).
 */
class PartidaEscalerasTest {

    private static final Posicion ENTRADA = new Posicion(2, 2);
    private static final Posicion ESCALERA = new Posicion(2, 3);
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

    /**
     * Un tablero de dos pisos unidos por una escalera en (2,3), con la salida
     * en el piso 2, y el héroe en la entrada del piso 1.
     */
    private Partida dosPisosConEscalera(Plano piso1, Plano piso2) {
        piso2 = new Plano(piso2.getTablero(), ENTRADA, new Posicion(2, 5));
        this.mazmorra = new Mazmorra(List.of(piso1, piso2), 2);
        this.mazmorra.conectar(new Ubicacion(1, 1, ESCALERA), new Ubicacion(1, 2, ESCALERA));
        this.heroe = new Heroe(this.mazmorra.getEntrada(), 100);
        return new Partida(this.mazmorra, this.heroe, COSTOS);
    }

    private Partida dosPisosConEscalera() {
        return dosPisosConEscalera(unPiso(), unPiso());
    }

    // =================== ESCALERAS =================== //

    @Test
    void pisarUnaEscaleraLlevaAlHeroeAlPisoDeDestinoEnLaMismaCelda() {
        Partida partida = dosPisosConEscalera();

        partida.moverHeroe(Direccion.DERECHA);

        assertEquals(new Ubicacion(1, 2, ESCALERA), this.heroe.getUbicacion());
        assertEquals(this.mazmorra.getPlano(new Ubicacion(1, 2, ESCALERA)), partida.getPlanoActual());
    }

    @Test
    void pisarUnaEscaleraCuestaUnSoloPasoYElCostoDeLaCeldaPisada() {
        Partida partida = dosPisosConEscalera();

        partida.moverHeroe(Direccion.DERECHA);

        assertEquals(1, partida.getPasos());
        assertEquals(99, this.heroe.getEnergia());
    }

    @Test
    void elTrasladoOcurreUnaSolaVezAunqueElDestinoTambienSeaEscalera() {
        Partida partida = dosPisosConEscalera();

        partida.moverHeroe(Direccion.DERECHA);
        partida.esperar();

        // no rebota al piso 1: sigue en el piso 2, parado en la escalera
        assertEquals(new Ubicacion(1, 2, ESCALERA), this.heroe.getUbicacion());
    }

    @Test
    void salirDeLaEscaleraYVolverAPisarlaTrasladaDeNuevo() {
        Partida partida = dosPisosConEscalera();

        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);
        assertEquals(new Ubicacion(1, 2, new Posicion(2, 4)), this.heroe.getUbicacion());

        partida.moverHeroe(Direccion.IZQUIERDA);

        assertEquals(new Ubicacion(1, 1, ESCALERA), this.heroe.getUbicacion());
    }

    @Test
    void despuesDeSubirJueganLosMonstruosDelPisoDeDestinoYNoLosDelPisoDeOrigen() {
        Plano piso1 = unPiso();
        Plano piso2 = unPiso();
        MonstruoContador deAbajo = new MonstruoContador(new Posicion(2, 5));
        MonstruoContador deArriba = new MonstruoContador(new Posicion(2, 5));
        piso1.agregarEntidad(deAbajo);
        Partida partida = dosPisosConEscalera(piso1, piso2);
        this.mazmorra.getPlano(new Ubicacion(1, 2, ENTRADA)).agregarEntidad(deArriba);

        partida.moverHeroe(Direccion.DERECHA);

        assertEquals(1, deArriba.getTurnosJugados());
        assertEquals(0, deAbajo.getTurnosJugados());
    }

    @Test
    void noSeTrasladaSiElPasoLoDejoSinEnergia() {
        Partida partida = dosPisosConEscalera();
        this.heroe.restarEnergia(99);

        partida.moverHeroe(Direccion.DERECHA);

        assertTrue(partida.estaPerdida());
        assertEquals(new Ubicacion(1, 1, ESCALERA), this.heroe.getUbicacion());
    }

    @Test
    void sePuedeGanarEnElPisoDeArribaDespuesDeSubir() {
        Partida partida = dosPisosConEscalera();

        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);

        assertTrue(partida.estaGanada());
    }
}
