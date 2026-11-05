package ar.uba.fi.cb100.tp2.modelo.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePuntos;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Monstruo;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
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
    private static final int DANIO_DEL_MONSTRUO = 30;
    private static final Map<Terreno, Integer> COSTOS =
            Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8);

    private Heroe heroe;

    /**
     * Un monstruo de prueba: siempre quiere ir a la misma celda y cuenta
     * cuántas veces le tocó jugar. Al alcanzar al héroe ataca y vuelve al inicio.
     */
    private static class MonstruoDePrueba extends Monstruo {

        private final Posicion destino;
        private int turnosJugados;

        MonstruoDePrueba(Posicion inicio, Posicion destino) {
            super(inicio, DANIO_DEL_MONSTRUO);
            this.destino = destino;
            this.turnosJugados = 0;
        }

        @Override
        protected Posicion decidirMovimiento(Plano plano, Posicion heroe) {
            this.turnosJugados++;
            return this.destino;
        }

        @Override
        protected void alAlcanzarAlHeroe(Heroe heroe, Agenda agenda) {
            this.atacarYVolver(heroe);
        }

        int getTurnosJugados() {
            return this.turnosJugados;
        }
    }

    /**
     * Un solo plano de 5 x 5, todo pared salvo:
     *   fila 2:    (2,2) piso = entrada, (2,3) piso, (2,4) agua, (2,5) piso = salida
     *   (3,2): lava
     * con las entidades que se pasen puestas en el plano.
     */
    private Partida partidaConEnergia(int energia, Entidad... entidades) {
        Tablero tablero = new Tablero(5, 5);
        tablero.setTerreno(new Posicion(2, 2), Terreno.PISO);
        tablero.setTerreno(new Posicion(2, 3), Terreno.PISO);
        tablero.setTerreno(new Posicion(2, 4), Terreno.AGUA);
        tablero.setTerreno(new Posicion(2, 5), Terreno.PISO);
        tablero.setTerreno(new Posicion(3, 2), Terreno.LAVA);
        Plano plano = new Plano(tablero, ENTRADA, SALIDA);
        for (Entidad entidad : entidades) {
            plano.agregarEntidad(entidad);
        }
        Mazmorra mazmorra = new Mazmorra(List.of(plano), 1);
        this.heroe = new Heroe(mazmorra.getEntrada(), energia);
        return new Partida(mazmorra, this.heroe, COSTOS);
    }

    // =================== CONTADORES Y PUNTAJE =================== //

    @Test
    void unaPartidaNuevaEmpiezaSinPasosNiCofres() {
        Partida partida = partidaConEnergia(100);

        assertEquals(0, partida.getPasos());
        assertEquals(0, partida.getCofresAbiertos());
    }

    @Test
    void losCofresAbiertosSonLosQueCuentaElHeroe() {
        Partida partida = partidaConEnergia(100);

        this.heroe.sumarCofreAbierto();
        this.heroe.sumarCofreAbierto();

        assertEquals(2, partida.getCofresAbiertos());
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
    void elPuntajeDeLaPartidaCuentaLosCofresQueAbrioElHeroe() {
        Partida partida = partidaConEnergia(100);
        this.heroe.sumarCofreAbierto();
        this.heroe.sumarCofreAbierto();

        // 2 cofres + 100 de energía × 2 - 0 pasos
        assertEquals(400, partida.puntaje());
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

    // =================== MOVERSE Y TERRENO =================== //

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
    void entrarAlAguaCuestaTres() {
        Partida partida = partidaConEnergia(100);

        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);

        assertEquals(96, this.heroe.getEnergia());
    }

    @Test
    void entrarALaLavaCuestaOcho() {
        Partida partida = partidaConEnergia(100);

        partida.moverHeroe(Direccion.ABAJO);

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

    // =================== GANAR Y PERDER =================== //

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
    void laPartidaSabeEnQuePlanoEstaElHeroe() {
        Partida partida = partidaConEnergia(100);

        assertEquals(new Ubicacion(0, 0, ENTRADA), this.heroe.getUbicacion());
        assertEquals(ENTRADA, partida.getPlanoActual().getEntrada());
    }

    // =================== ESPERAR =================== //

    @Test
    void esperarCuentaUnPasoSinGastarEnergiaNiMoverAlHeroe() {
        Partida partida = partidaConEnergia(100);

        ResultadoTurno resultado = partida.esperar();

        assertTrue(resultado.accionValida());
        assertEquals(EstadoPartida.EN_CURSO, resultado.estado());
        assertEquals(1, partida.getPasos());
        assertEquals(100, this.heroe.getEnergia());
        assertEquals(ENTRADA, this.heroe.getUbicacion().posicion());
    }

    @Test
    void unaPartidaTerminadaNoAceptaEsperar() {
        Partida partida = partidaConEnergia(5);
        partida.moverHeroe(Direccion.ABAJO);

        ResultadoTurno resultado = partida.esperar();

        assertFalse(resultado.accionValida());
        assertEquals(EstadoPartida.PERDIDA, resultado.estado());
        assertEquals(1, partida.getPasos());
    }

    // =================== TURNO DE LOS MONSTRUOS =================== //

    @Test
    void despuesDeCadaPasoValidoLosMonstruosSeMueven() {
        MonstruoDePrueba monstruo = new MonstruoDePrueba(new Posicion(3, 2), new Posicion(2, 4));
        Partida partida = partidaConEnergia(100, monstruo);

        partida.moverHeroe(Direccion.DERECHA);

        assertEquals(1, monstruo.getTurnosJugados());
        assertEquals(new Posicion(2, 4), monstruo.getPosicion());
    }

    @Test
    void esperarTambienLesDaSuTurnoALosMonstruos() {
        MonstruoDePrueba monstruo = new MonstruoDePrueba(new Posicion(3, 2), new Posicion(2, 4));
        Partida partida = partidaConEnergia(100, monstruo);

        partida.esperar();

        assertEquals(1, monstruo.getTurnosJugados());
    }

    @Test
    void unaAccionInvalidaNoLesDaTurnoALosMonstruos() {
        MonstruoDePrueba monstruo = new MonstruoDePrueba(new Posicion(3, 2), new Posicion(2, 4));
        Partida partida = partidaConEnergia(100, monstruo);

        partida.moverHeroe(Direccion.ARRIBA);

        assertEquals(0, monstruo.getTurnosJugados());
        assertEquals(new Posicion(3, 2), monstruo.getPosicion());
    }

    @Test
    void siElHeroeGanaEnEsePasoLosMonstruosNoJuegan() {
        MonstruoDePrueba monstruo = new MonstruoDePrueba(new Posicion(3, 2), new Posicion(2, 4));
        Partida partida = partidaConEnergia(100, monstruo);

        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);
        partida.moverHeroe(Direccion.DERECHA);

        assertTrue(partida.estaGanada());
        // jugó en los dos pasos anteriores, pero no en el que ganó
        assertEquals(2, monstruo.getTurnosJugados());
    }

    @Test
    void unMonstruoQueLlegaALaCeldaDelHeroeLoAtacaYVuelveAlInicio() {
        Posicion inicio = new Posicion(2, 4);
        MonstruoDePrueba monstruo = new MonstruoDePrueba(inicio, new Posicion(2, 3));
        Partida partida = partidaConEnergia(100, monstruo);

        // el héroe pisa (2,3) y el monstruo se mueve justo hasta ahí
        partida.moverHeroe(Direccion.DERECHA);

        // 100 - 1 de piso - 30 del monstruo
        assertEquals(69, this.heroe.getEnergia());
        assertEquals(inicio, monstruo.getPosicion());
    }

    @Test
    void siElHeroePisaAlMonstruoLoAlcanzaEnEseMismoPaso() {
        MonstruoDePrueba monstruo = new MonstruoDePrueba(new Posicion(2, 3), new Posicion(2, 3));
        Partida partida = partidaConEnergia(100, monstruo);

        partida.moverHeroe(Direccion.DERECHA);

        // 100 - 1 de piso - 30 del monstruo, y el monstruo se queda donde estaba
        assertEquals(69, this.heroe.getEnergia());
    }

    // =================== COFRES =================== //

    @Test
    void pisarUnCofreLoAbreUnaSolaVezPorPaso() {
        CofreDePuntos cofre = new CofreDePuntos(new Posicion(2, 3));
        Partida partida = partidaConEnergia(100, cofre);

        partida.moverHeroe(Direccion.DERECHA);

        // el turno de los monstruos no vuelve a abrir el cofre que está bajo el héroe
        assertEquals(1, this.heroe.getCofresAbiertos());
        assertEquals(1, partida.getCofresAbiertos());
        // 1 cofre × 100 + 99 de energía × 2 - 1 paso
        assertEquals(297, partida.puntaje());
    }
}
