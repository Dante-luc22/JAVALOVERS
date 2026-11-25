package ar.uba.fi.cb100.tp2.modelo.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.DespertarMonstruos;
import ar.uba.fi.cb100.tp2.modelo.efectos.AguaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.efectos.Envenenado;
import ar.uba.fi.cb100.tp2.modelo.efectos.QuitarEfecto;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Monstruo;
import ar.uba.fi.cb100.tp2.modelo.monstruos.TipoDeMonstruo;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Hito 3: qué pasa cuando en un mismo paso coinciden un vencimiento de la
 * agenda, un movimiento del héroe y un encuentro con un monstruo.
 *
 * El orden de un paso es: 1) vencen los eventos de la agenda, 2) el héroe se
 * mueve (con el costo ya ajustado por los efectos que quedan), 3) se
 * encuentra con lo que haya en su celda, 4) juegan los monstruos.
 */
class PartidaCoincidenciasTest {

    private static final Posicion ENTRADA = new Posicion(2, 2);
    private static final Posicion AGUA = new Posicion(3, 2);
    private static final Posicion DERECHA_DE_ENTRADA = new Posicion(2, 3);
    private static final Posicion LEJOS = new Posicion(2, 4);
    private static final Posicion SALIDA = new Posicion(2, 5);
    private static final int DANIO = 30;
    private static final Map<Terreno, Integer> COSTOS =
            Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8);

    private Heroe heroe;
    private Plano plano;

    /**
     * Un monstruo que no se mueve, o que va derecho hacia el héroe, y que al
     * alcanzarlo le saca energía y vuelve a su celda inicial.
     */
    private static class MonstruoDePrueba extends Monstruo {

        private final boolean persigueAlHeroe;

        MonstruoDePrueba(Posicion inicio, boolean persigueAlHeroe) {
            super(inicio, DANIO);
            this.persigueAlHeroe = persigueAlHeroe;
        }

        @Override
        public TipoDeMonstruo getTipoDeMonstruo() {
            return TipoDeMonstruo.ERRANTE;
        }

        @Override
        protected Posicion decidirMovimiento(Plano plano, Posicion heroe) {
            return this.persigueAlHeroe ? heroe : this.getPosicion();
        }

        @Override
        protected void alAlcanzarAlHeroe(Heroe heroe, Agenda agenda) {
            this.atacarYVolver(heroe);
        }
    }

    /** Fila 2, columnas 2 a 5, de piso; (3,2) es agua. */
    private static Tablero unTablero() {
        Tablero tablero = new Tablero(6, 6);
        for (int columna = 2; columna <= 5; columna++) {
            tablero.setTerreno(new Posicion(2, columna), Terreno.PISO);
        }
        tablero.setTerreno(AGUA, Terreno.AGUA);
        return tablero;
    }

    private Partida partida(Posicion entrada, int energia, Monstruo... monstruos) {
        this.plano = new Plano(unTablero(), entrada, SALIDA);
        for (Monstruo monstruo : monstruos) {
            this.plano.agregarEntidad(monstruo);
        }
        Mazmorra mazmorra = new Mazmorra(List.of(this.plano), 1);
        this.heroe = new Heroe(mazmorra.getEntrada(), energia);
        return new Partida(mazmorra, this.heroe, COSTOS);
    }

    // ===================== VENCIMIENTOS Y COSTO ===================== //

    @Test
    void unEfectoQueVenceEnElPasoDeEntrarAlAguaYaNoCuentaParaEseMovimiento() {
        Partida partida = partida(ENTRADA, 100);
        AguaComoPiso zapatos = new AguaComoPiso();
        this.heroe.agregarEfecto(zapatos);
        partida.getAgenda().programar(1, new QuitarEfecto(zapatos));

        partida.moverHeroe(Direccion.ABAJO);

        assertEquals(97, this.heroe.getEnergia());
    }

    @Test
    void unEfectoQueVenceUnPasoDespuesSiCuentaParaElPasoAnterior() {
        Partida partida = partida(ENTRADA, 100);
        AguaComoPiso zapatos = new AguaComoPiso();
        this.heroe.agregarEfecto(zapatos);
        partida.getAgenda().programar(2, new QuitarEfecto(zapatos));

        partida.moverHeroe(Direccion.ABAJO);
        assertEquals(99, this.heroe.getEnergia());

        partida.moverHeroe(Direccion.ARRIBA);
        partida.moverHeroe(Direccion.ABAJO);
        assertEquals(95, this.heroe.getEnergia());
    }

    @Test
    void elVenenoSumaUnoAlCostoDeCadaPasoMientrasDura() {
        Partida partida = partida(ENTRADA, 100);
        Envenenado veneno = new Envenenado();
        this.heroe.agregarEfecto(veneno);
        partida.getAgenda().programar(2, new QuitarEfecto(veneno));

        partida.moverHeroe(Direccion.DERECHA);
        assertEquals(98, this.heroe.getEnergia());

        partida.moverHeroe(Direccion.DERECHA);
        assertEquals(97, this.heroe.getEnergia());
    }

    @Test
    void conVenenoPisarLaSalidaConLaEnergiaJustaGanaEnVezDePerder() {
        Partida partida = partida(LEJOS, 2);
        this.heroe.agregarEfecto(new Envenenado());

        ResultadoTurno resultado = partida.moverHeroe(Direccion.DERECHA);

        assertEquals(0, this.heroe.getEnergia());
        assertEquals(EstadoPartida.GANADA, resultado.estado());
    }

    @Test
    void conVenenoQuedarseSinEnergiaFueraDeLaSalidaPierde() {
        Partida partida = partida(ENTRADA, 2);
        this.heroe.agregarEfecto(new Envenenado());

        ResultadoTurno resultado = partida.moverHeroe(Direccion.DERECHA);

        assertEquals(EstadoPartida.PERDIDA, resultado.estado());
    }

    // ===================== ORDEN DE LOS EVENTOS ===================== //

    @Test
    void losEventosQueVencenEnElMismoPasoSeEjecutanEnElOrdenEnQueSeProgramaron() {
        Partida partida = partida(ENTRADA, 100);
        List<String> orden = new ArrayList<>();
        partida.getAgenda().programar(1, p -> orden.add("A"));
        partida.getAgenda().programar(1, p -> orden.add("B"));
        partida.getAgenda().programar(1, p -> orden.add("C"));

        partida.esperar();

        assertEquals(List.of("A", "B", "C"), orden);
    }

    @Test
    void unEventoEjecutaEnSuPasoYNoAntesNiDespues() {
        Partida partida = partida(ENTRADA, 100);
        List<Integer> pasosEnQueCorrio = new ArrayList<>();
        partida.getAgenda().programar(2, p -> pasosEnQueCorrio.add(p.getPasos()));

        partida.esperar();
        assertTrue(pasosEnQueCorrio.isEmpty());

        partida.esperar();
        partida.esperar();
        assertEquals(List.of(2), pasosEnQueCorrio);
    }

    @Test
    void unaAccionInvalidaNoHaceVencerLosEventos() {
        Partida partida = partida(ENTRADA, 100);
        List<String> corrieron = new ArrayList<>();
        partida.getAgenda().programar(1, p -> corrieron.add("evento"));

        ResultadoTurno contraLaPared = partida.moverHeroe(Direccion.ARRIBA);

        assertFalse(contraLaPared.accionValida());
        assertTrue(corrieron.isEmpty());

        partida.esperar();
        assertEquals(List.of("evento"), corrieron);
    }

    // ============== VENCIMIENTOS, MOVIMIENTOS Y ENCUENTROS ============== //

    @Test
    void unMonstruoQueDespiertaEnEstePasoYaJuegaEnEstePaso() {
        MonstruoDePrueba monstruo = new MonstruoDePrueba(LEJOS, true);
        monstruo.adormecer();
        Partida partida = partida(ENTRADA, 100, monstruo);
        partida.getAgenda().programar(1, new DespertarMonstruos());

        partida.esperar();

        // Despertó, fue hasta el héroe, lo atacó y volvió a su celda.
        assertFalse(monstruo.estaAdormecido());
        assertEquals(100 - DANIO, this.heroe.getEnergia());
        assertEquals(LEJOS, monstruo.getPosicion());
    }

    @Test
    void unMonstruoQueSigueDormidoNoJuegaAunqueElHeroeEsperaCerca() {
        MonstruoDePrueba monstruo = new MonstruoDePrueba(LEJOS, true);
        monstruo.adormecer();
        Partida partida = partida(ENTRADA, 100, monstruo);
        partida.getAgenda().programar(2, new DespertarMonstruos());

        partida.esperar();

        assertTrue(monstruo.estaAdormecido());
        assertEquals(100, this.heroe.getEnergia());
        assertEquals(LEJOS, monstruo.getPosicion());
    }

    @Test
    void siElSuenioVenceEnElPasoEnQueElHeroePisaAlMonstruoEsteLoAtaca() {
        MonstruoDePrueba monstruo = new MonstruoDePrueba(DERECHA_DE_ENTRADA, false);
        monstruo.adormecer();
        Partida partida = partida(ENTRADA, 100, monstruo);
        partida.getAgenda().programar(1, new DespertarMonstruos());

        partida.moverHeroe(Direccion.DERECHA);

        // Primero venció el sueño, después el héroe pisó al monstruo ya despierto.
        assertEquals(100 - 1 - DANIO, this.heroe.getEnergia());
    }

    @Test
    void siElHeroePisaAlMonstruoDormidoAntesDeQueVenzaElSuenioNoLoAtacaYLoDespierta() {
        MonstruoDePrueba monstruo = new MonstruoDePrueba(DERECHA_DE_ENTRADA, false);
        monstruo.adormecer();
        Partida partida = partida(ENTRADA, 100, monstruo);
        partida.getAgenda().programar(2, new DespertarMonstruos());

        partida.moverHeroe(Direccion.DERECHA);

        assertEquals(99, this.heroe.getEnergia());
        assertFalse(monstruo.estaAdormecido());
    }

    @Test
    void elEventoDeDespertarQueLlegaTardeNoDejaAlMonstruoConSuenioNegativo() {
        MonstruoDePrueba monstruo = new MonstruoDePrueba(DERECHA_DE_ENTRADA, false);
        monstruo.adormecer();
        Partida partida = partida(ENTRADA, 100, monstruo);
        partida.getAgenda().programar(2, new DespertarMonstruos());

        partida.moverHeroe(Direccion.DERECHA);   // el héroe lo despierta del todo
        partida.moverHeroe(Direccion.IZQUIERDA); // vence el evento, ya no hay nada que despertar

        assertFalse(monstruo.estaAdormecido());
        monstruo.adormecer();
        assertTrue(monstruo.estaAdormecido());
    }

    @Test
    void unEventoYUnEfectoQueVencenAlMismoTiempoConUnMonstruoEnElCaminoSeResuelvenEnOrden() {
        MonstruoDePrueba monstruo = new MonstruoDePrueba(AGUA, false);
        monstruo.adormecer();
        Partida partida = partida(ENTRADA, 100, monstruo);
        AguaComoPiso zapatos = new AguaComoPiso();
        this.heroe.agregarEfecto(zapatos);
        partida.getAgenda().programar(1, new QuitarEfecto(zapatos));
        partida.getAgenda().programar(1, new DespertarMonstruos());

        partida.moverHeroe(Direccion.ABAJO);

        // Sin zapatos: el agua cuesta 3. Y el monstruo despierto ataca.
        assertEquals(100 - 3 - DANIO, this.heroe.getEnergia());
    }
}
