package ar.uba.fi.cb100.tp2.modelo.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

class PartidaTest {

    private static final Posicion ENTRADA = new Posicion(2, 2);
    private static final Posicion SALIDA = new Posicion(2, 5);
    private static final Map<Terreno, Integer> COSTOS =
            Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8);

    private Heroe heroe;

    /**
     * Un solo plano de 5 x 5, todo pared salvo:
     *   fila 2:    (2,2) piso = entrada, (2,3) piso, (2,4) agua, (2,5) piso = salida
     *   (3,2): lava
     */
    private Partida partidaConEnergia(int energia) {
        Tablero tablero = new Tablero(5, 5);
        tablero.setTerreno(new Posicion(2, 2), Terreno.PISO);
        tablero.setTerreno(new Posicion(2, 3), Terreno.PISO);
        tablero.setTerreno(new Posicion(2, 4), Terreno.AGUA);
        tablero.setTerreno(new Posicion(2, 5), Terreno.PISO);
        tablero.setTerreno(new Posicion(3, 2), Terreno.LAVA);
        Plano plano = new Plano(tablero, ENTRADA, SALIDA);
        Mazmorra mazmorra = new Mazmorra(List.of(plano), 1);
        this.heroe = new Heroe(mazmorra.getEntrada(), energia);
        return new Partida(mazmorra, this.heroe, COSTOS);
    }

    @Test
    void unaPartidaNuevaEmpiezaSinPasosNiCofres() {
        Partida partida = partidaConEnergia(100);

        assertEquals(0, partida.getPasos());
        assertEquals(0, partida.getCofresAbiertos());
    }

    @Test
    void registrarPasosYCofresSumaALosContadores() {
        Partida partida = partidaConEnergia(100);

        partida.registrarPaso();
        partida.registrarPaso();
        partida.registrarCofreAbierto();

        assertEquals(2, partida.getPasos());
        assertEquals(1, partida.getCofresAbiertos());
    }

    @Test
    void elPuntajeSumaCofresYEnergiaYRestaLosPasos() {
        // 3 cofres abiertos, 50 de energía y 20 pasos: 300 + 100 - 20
        assertEquals(380, Partida.calcularPuntaje(3, 50, 20));
    }

    @Test
    void elPuntajeDeUnaPartidaSinPasosNiCofresEsLaEnergiaPorDos() {
        // Sin cofres ni pasos y con la energía inicial: 0 + 200 - 0
        assertEquals(200, Partida.calcularPuntaje(0, 100, 0));
    }

    @Test
    void elPuntajePuedeSerNegativoSiSeDieronMuchosPasos() {
        // Sin cofres ni energía y con 10 pasos: 0 + 0 - 10
        assertEquals(-10, Partida.calcularPuntaje(0, 0, 10));
    }

    @Test
    void costoDeDevuelveElCostoConfiguradoYLaParedNoTieneCosto() {
        Partida partida = partidaConEnergia(100);

        assertEquals(1, partida.costoDe(Terreno.PISO));
        assertEquals(3, partida.costoDe(Terreno.AGUA));
        assertEquals(8, partida.costoDe(Terreno.LAVA));
        assertThrows(IllegalArgumentException.class, () -> partida.costoDe(Terreno.PARED));
    }

    @Test
    void moverAPisoCuestaUnoYSumaUnPaso() {
        Partida partida = partidaConEnergia(100);

        ResultadoTurno resultado = partida.moverHeroe(Direccion.DERECHA);

        assertTrue(resultado.accionValida());
        assertEquals(EstadoPartida.EN_CURSO, resultado.estado());
        assertEquals(new Posicion(2, 3), this.heroe.getUbicacion().posicion());
        assertEquals(99, this.heroe.getEnergia());
        assertEquals(1, partida.getPasos());
    }

    @Test
    void entrarAlAguaCuestaTresYALaLavaOcho() {
        Partida partida = partidaConEnergia(100);

        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);
        assertEquals(96, this.heroe.getEnergia());

        Partida otra = partidaConEnergia(100);
        otra.moverHeroe(Direccion.ABAJO);
        assertEquals(92, this.heroe.getEnergia());
    }

    @Test
    void moverseContraUnaParedNoCuestaNiCuentaComoPaso() {
        Partida partida = partidaConEnergia(100);

        ResultadoTurno resultado = partida.moverHeroe(Direccion.ARRIBA);

        assertFalse(resultado.accionValida());
        assertEquals(EstadoPartida.EN_CURSO, resultado.estado());
        assertEquals(ENTRADA, this.heroe.getUbicacion().posicion());
        assertEquals(100, this.heroe.getEnergia());
        assertEquals(0, partida.getPasos());
    }

    @Test
    void pisarLaSalidaGanaLaPartida() {
        Partida partida = partidaConEnergia(100);

        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);
        ResultadoTurno resultado = partida.moverHeroe(Direccion.DERECHA);

        assertEquals(EstadoPartida.GANADA, resultado.estado());
        assertTrue(partida.estaGanada());
        assertFalse(partida.estaPerdida());
    }

    @Test
    void sePierdeCuandoLaEnergiaLlegaACero() {
        Partida partida = partidaConEnergia(5);

        ResultadoTurno resultado = partida.moverHeroe(Direccion.ABAJO);

        assertEquals(0, this.heroe.getEnergia());
        assertEquals(EstadoPartida.PERDIDA, resultado.estado());
        assertTrue(partida.estaPerdida());
    }

    @Test
    void pisarLaSalidaConLaUltimaEnergiaGanaEnVezDePerder() {
        // piso 1 + agua 3 + piso 1 = 5: la energía queda en 0 justo en la salida
        Partida partida = partidaConEnergia(5);

        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);
        ResultadoTurno resultado = partida.moverHeroe(Direccion.DERECHA);

        assertEquals(0, this.heroe.getEnergia());
        assertEquals(EstadoPartida.GANADA, resultado.estado());
    }

    @Test
    void unaPartidaTerminadaNoAceptaMasMovimientos() {
        Partida partida = partidaConEnergia(5);
        partida.moverHeroe(Direccion.ABAJO);

        ResultadoTurno resultado = partida.moverHeroe(Direccion.ARRIBA);

        assertFalse(resultado.accionValida());
        assertEquals(1, partida.getPasos());
    }

    @Test
    void elPuntajeUsaLaEnergiaRealDelHeroeYLosPasosDados() {
        Partida partida = partidaConEnergia(100);

        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);

        // 0 cofres + 95 de energía × 2 - 3 pasos
        assertEquals(187, partida.puntaje());
    }

    @Test
    void laPartidaSabeEnQuePlanoEstaElHeroe() {
        Partida partida = partidaConEnergia(100);

        assertEquals(new Ubicacion(0, 0, ENTRADA), this.heroe.getUbicacion());
        assertEquals(ENTRADA, partida.getPlanoActual().getEntrada());
    }
}
