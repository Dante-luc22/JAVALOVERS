package ar.uba.fi.cb100.tp2.modelo.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeVeneno;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Monstruo;
import ar.uba.fi.cb100.tp2.modelo.monstruos.TipoDeMonstruo;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Tests de qué cambios de energía quedan anotados para mostrar en cada turno:
 * el daño del agua, de la lava y de los ataques, y lo que se recupera al
 * esperar. Caminar por un piso común no se anota.
 */
class PartidaCambiosDeEnergiaTest {

    private static final Posicion ENTRADA = new Posicion(2, 2);
    private static final Posicion SALIDA = new Posicion(2, 6);
    private static final Map<Terreno, Integer> COSTOS =
            Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8);

    /** Un monstruo que va directo hacia una celda y, al alcanzar al héroe, lo ataca. */
    private static class AtacanteDePrueba extends Monstruo {

        private final Posicion destino;

        AtacanteDePrueba(Posicion inicio, Posicion destino, int danio) {
            super(inicio, danio);
            this.destino = destino;
        }

        @Override
        public TipoDeMonstruo getTipoDeMonstruo() {
            return TipoDeMonstruo.ERRANTE;
        }

        @Override
        protected Posicion decidirMovimiento(Plano plano, Posicion heroe) {
            return this.destino;
        }

        @Override
        protected void alAlcanzarAlHeroe(Heroe heroe, Agenda agenda) {
            this.atacarYVolver(heroe);
        }
    }

    private Heroe heroe;

    /**
     * Un plano de 5 x 7, todo pared salvo:
     *   fila 2: (2,2) piso = entrada, (2,3) piso, (2,4) agua, (2,5) piso, (2,6) piso = salida
     *   (3,2): lava
     */
    private Partida partida(int energia, Map<Terreno, Integer> costos, Entidad... entidades) {
        Tablero tablero = new Tablero(5, 7);
        tablero.setTerreno(new Posicion(2, 2), Terreno.PISO);
        tablero.setTerreno(new Posicion(2, 3), Terreno.PISO);
        tablero.setTerreno(new Posicion(2, 4), Terreno.AGUA);
        tablero.setTerreno(new Posicion(2, 5), Terreno.PISO);
        tablero.setTerreno(new Posicion(2, 6), Terreno.PISO);
        tablero.setTerreno(new Posicion(3, 2), Terreno.LAVA);
        Plano plano = new Plano(tablero, ENTRADA, SALIDA);
        for (Entidad entidad : entidades) {
            plano.agregarEntidad(entidad);
        }
        Mazmorra mazmorra = new Mazmorra(List.of(plano), 1);
        this.heroe = new Heroe(mazmorra.getEntrada(), energia);
        return new Partida(mazmorra, this.heroe, costos);
    }

    private Partida partida(int energia, Entidad... entidades) {
        return partida(energia, COSTOS, entidades);
    }

    // =================== EL TERRENO =================== //

    @Test
    void caminarPorUnPisoComunNoAnotaNada() {
        Partida partida = partida(100);

        partida.moverHeroe(Direccion.DERECHA);

        assertEquals(99, this.heroe.getEnergia());
        assertTrue(this.heroe.retirarCambiosDeEnergia().isEmpty());
    }

    @Test
    void pisarAguaAnotaLoQueCuesta() {
        Partida partida = partida(100);
        partida.moverHeroe(Direccion.DERECHA);
        this.heroe.retirarCambiosDeEnergia();

        partida.moverHeroe(Direccion.DERECHA);

        assertEquals(List.of(-3), this.heroe.retirarCambiosDeEnergia());
    }

    @Test
    void pisarLavaAnotaLoQueCuesta() {
        Partida partida = partida(100);

        partida.moverHeroe(Direccion.ABAJO);

        assertEquals(List.of(-8), this.heroe.retirarCambiosDeEnergia());
    }

    @Test
    void siElAguaCuestaComoPisoNoAnotaNada() {
        Map<Terreno, Integer> aguaComoPiso = Map.of(Terreno.PISO, 1, Terreno.AGUA, 1, Terreno.LAVA, 8);
        Partida partida = partida(100, aguaComoPiso);
        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);

        assertTrue(this.heroe.retirarCambiosDeEnergia().isEmpty());
    }

    // =================== ESPERAR =================== //

    @Test
    void esperarConLaEnergiaBajaAnotaMasUno() {
        Partida partida = partida(100);
        partida.moverHeroe(Direccion.ABAJO);
        this.heroe.retirarCambiosDeEnergia();

        partida.esperar();

        assertEquals(List.of(1), this.heroe.retirarCambiosDeEnergia());
    }

    @Test
    void esperarConLaEnergiaLlenaNoAnotaNada() {
        Partida partida = partida(100);

        partida.esperar();

        assertTrue(this.heroe.retirarCambiosDeEnergia().isEmpty());
    }

    // =================== LOS ATAQUES =================== //

    @Test
    void unAtaqueAnotaElDanio() {
        // el monstruo está en (2,4) y camina hasta (2,3), donde va a estar el héroe
        AtacanteDePrueba atacante = new AtacanteDePrueba(new Posicion(2, 4), new Posicion(2, 3), 30);
        Partida partida = partida(100, atacante);

        partida.moverHeroe(Direccion.DERECHA);

        assertEquals(List.of(-30), this.heroe.retirarCambiosDeEnergia());
    }

    @Test
    void siPisaAguaYLoAtacanEnElMismoTurnoSeAnotanLosDosEnOrden() {
        // el monstruo espera quieto en (2,4), el agua: al pisarla el héroe paga el agua y lo atacan
        AtacanteDePrueba atacante = new AtacanteDePrueba(new Posicion(2, 4), new Posicion(2, 4), 30);
        Partida partida = partida(100, atacante);
        partida.moverHeroe(Direccion.DERECHA);
        this.heroe.retirarCambiosDeEnergia();

        partida.moverHeroe(Direccion.DERECHA);

        assertEquals(List.of(-3, -30), this.heroe.retirarCambiosDeEnergia());
    }

    // =================== EL VENENO =================== //

    @Test
    void sinVenenoNoFaltaNingunPaso() {
        Partida partida = partida(100);

        assertEquals(0, partida.pasosParaQueSePaseElVeneno());
    }

    @Test
    void alAbrirUnCofreDeVenenoFaltanDiezPasos() {
        Partida partida = partida(100, new CofreDeVeneno(new Posicion(2, 3)));

        partida.moverHeroe(Direccion.DERECHA);
        partida.interactuar();

        assertEquals(10, partida.pasosParaQueSePaseElVeneno());
    }

    @Test
    void cadaPasoBajaElVenenoEnUno() {
        Partida partida = partida(100, new CofreDeVeneno(new Posicion(2, 3)));
        partida.moverHeroe(Direccion.DERECHA);
        partida.interactuar();

        partida.esperar();
        partida.esperar();

        assertEquals(8, partida.pasosParaQueSePaseElVeneno());
    }

    @Test
    void alPasarseElVenenoNoFaltaNada() {
        Partida partida = partida(100, new CofreDeVeneno(new Posicion(2, 3)));
        partida.moverHeroe(Direccion.DERECHA);
        partida.interactuar();

        for (int i = 0; i < 10; i++) {
            partida.esperar();
        }

        assertEquals(0, partida.pasosParaQueSePaseElVeneno());
        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }
}
