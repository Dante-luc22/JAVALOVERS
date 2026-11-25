package ar.uba.fi.cb100.tp2.modelo.partida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeBotasIgnifugas;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeEscudo;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePico;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeRepelente;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeSomnifero;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeVeneno;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Cazador;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.objetos.BotasIgnifugas;
import ar.uba.fi.cb100.tp2.modelo.objetos.Pico;
import ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Tests de integración: los cofres con efectos temporales funcionando dentro
 * de una Partida real. Nadie llama a la agenda a mano: los vencimientos
 * ocurren porque la partida ejecuta la agenda al empezar cada paso.
 *
 * <p>El mundo es un pasillo horizontal en la fila 3, columnas 2 a 11, rodeado
 * de pared (el tablero numera desde 1). El héroe arranca en (3,2) y el cofre
 * está en (3,3): el primer
 * paso de cada test es pisarlo. La salida está encerrada entre paredes,
 * para que ningún test gane la partida sin querer.</p>
 *
 * <pre>
 *     1 2 3 4 5 6 7 8 9 10 11 12
 *   1 # # # # # # # # # #  #  #     fila 1: borde
 *   2 # # # # # # # # # #  #  #     fila 2: pared interior
 *   3 # H C . . . . . . .  .  #     H: héroe   C: cofre
 *   4 # # # # # # # # # #  #  #
 *   5 # # # # # # # # # #  S  #     S: salida (inalcanzable)
 *   6 # # # # # # # # # #  #  #
 * </pre>
 */
class PartidaEfectosTemporalesTest {

    private static final Map<Terreno, Integer> COSTOS =
            Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8);
    private static final int ENERGIA = 100;
    private static final int FILA = 3;
    private static final Posicion INICIO_HEROE = new Posicion(FILA, 2);
    private static final Posicion COFRE = new Posicion(FILA, 3);
    /** La salida está encerrada: ningún test la alcanza por accidente. */
    private static final Posicion SALIDA = new Posicion(5, 11);
    private static final int DURACION_DE_ZAPATOS_Y_BOTAS = 20;
    private static final int DURACION_DEL_VENENO = 10;
    private static final int DURACION_DEL_SOMNIFERO = 5;

    private Heroe heroe;
    private Plano plano;

    // =================== ARMADO DEL MUNDO =================== //

    /**
     * Arma una partida con el pasillo: el cofre en (3,3) es piso y las
     * columnas 4 a 11 tienen el terreno indicado.
     */
    private Partida pasilloDe(Terreno terreno, Entidad... entidades) {
        Tablero tablero = new Tablero(7, 12);
        tablero.setTerreno(INICIO_HEROE, Terreno.PISO);
        tablero.setTerreno(SALIDA, Terreno.PISO);
        tablero.setTerreno(COFRE, Terreno.PISO);
        for (int columna = 4; columna <= 11; columna++) {
            tablero.setTerreno(new Posicion(FILA, columna), terreno);
        }
        this.plano = new Plano(tablero, INICIO_HEROE, SALIDA);
        for (Entidad entidad : entidades) {
            this.plano.agregarEntidad(entidad);
        }
        Mazmorra mazmorra = new Mazmorra(List.of(this.plano), 1);
        this.heroe = new Heroe(mazmorra.getEntrada(), ENERGIA);
        return new Partida(mazmorra, this.heroe, COSTOS);
    }

    /** Mueve al héroe y devuelve cuánta energía le costó ese paso. */
    private int costoDeMover(Partida partida, Direccion direccion) {
        int antes = this.heroe.getEnergia();
        assertTrue(partida.moverHeroe(direccion).accionValida(),
                "el movimiento " + direccion + " tendría que ser válido");
        return antes - this.heroe.getEnergia();
    }

    /**
     * Da un paso de ida y vuelta por el pasillo sin volver al cofre: si está
     * en la columna 4 o antes va a la derecha; si no, a la izquierda.
     */
    private Direccion siguienteDireccionDentroDelPasillo() {
        int columna = this.heroe.getUbicacion().posicion().columna();
        return columna <= 4 ? Direccion.DERECHA : Direccion.IZQUIERDA;
    }

    /** Lleva al héroe del cofre a la columna 4 y lo hace caminar hasta el paso indicado. */
    private void caminarHastaElPaso(Partida partida, int paso) {
        while (partida.getPasos() < paso) {
            partida.moverHeroe(siguienteDireccionDentroDelPasillo());
        }
    }

    // =================== ZAPATOS DE AGUA =================== //

    @Test
    void conLosZapatosDelCofreElAguaCuestaComoPisoHastaQueVencen() {
        Partida partida = pasilloDe(Terreno.AGUA, new CofreDeZapatosDeAgua(COFRE));

        costoDeMover(partida, Direccion.DERECHA);              // paso 1: abre el cofre
        assertTrue(this.heroe.getInventario().contiene(ZapatosDeAgua.ID));
        partida.usarObjeto(ZapatosDeAgua.ID);                  // paso 2: los usa
        int pasoDeUso = partida.getPasos();

        // Del paso 3 al 21, cada paso por el agua cuesta como piso.
        while (partida.getPasos() < pasoDeUso + DURACION_DE_ZAPATOS_Y_BOTAS - 1) {
            assertEquals(1, costoDeMover(partida, siguienteDireccionDentroDelPasillo()),
                    "en el paso " + (partida.getPasos() + 1) + " el agua tendría que costar 1");
        }
        // En el paso 22 vence: el agua vuelve a costar 3.
        assertEquals(3, costoDeMover(partida, siguienteDireccionDentroDelPasillo()));
        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    @Test
    void sinUsarLosZapatosElAguaSigueCostandoTres() {
        Partida partida = pasilloDe(Terreno.AGUA, new CofreDeZapatosDeAgua(COFRE));

        costoDeMover(partida, Direccion.DERECHA);              // abre el cofre, no los usa

        assertEquals(3, costoDeMover(partida, Direccion.DERECHA));
        assertTrue(this.heroe.getInventario().contiene(ZapatosDeAgua.ID));
    }

    @Test
    void usarLosZapatosCuentaComoPasoYNoGastaEnergia() {
        Partida partida = pasilloDe(Terreno.AGUA, new CofreDeZapatosDeAgua(COFRE));
        costoDeMover(partida, Direccion.DERECHA);
        int energiaAntes = this.heroe.getEnergia();

        assertTrue(partida.usarObjeto(ZapatosDeAgua.ID).accionValida());

        assertEquals(2, partida.getPasos());
        assertEquals(energiaAntes, this.heroe.getEnergia());
        assertFalse(this.heroe.getInventario().contiene(ZapatosDeAgua.ID));
    }

    @Test
    void dosZapatosUsadosEnPasosDistintosVencenCadaUnoASuTiempo() {
        Partida partida = pasilloDe(Terreno.AGUA, new CofreDeZapatosDeAgua(COFRE));
        costoDeMover(partida, Direccion.DERECHA);              // paso 1: abre el cofre
        this.heroe.getInventario().agregar(new ZapatosDeAgua()); // un segundo par
        partida.usarObjeto(ZapatosDeAgua.ID);                  // paso 2: vence en el 22
        costoDeMover(partida, Direccion.DERECHA);              // paso 3
        partida.usarObjeto(ZapatosDeAgua.ID);                  // paso 4: vence en el 24

        caminarHastaElPaso(partida, 21);
        assertEquals(2, this.heroe.getEfectosActivos().size());
        // Paso 22: vence el primero, pero el segundo sigue protegiendo.
        assertEquals(1, costoDeMover(partida, siguienteDireccionDentroDelPasillo()));
        assertEquals(1, this.heroe.getEfectosActivos().size());
        // Paso 23: el segundo todavía no venció.
        assertEquals(1, costoDeMover(partida, siguienteDireccionDentroDelPasillo()));
        // Paso 24: vence el segundo.
        assertEquals(3, costoDeMover(partida, siguienteDireccionDentroDelPasillo()));
        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    // =================== BOTAS IGNÍFUGAS =================== //

    @Test
    void conLasBotasDelCofreLaLavaCuestaComoPisoHastaQueVencen() {
        Partida partida = pasilloDe(Terreno.LAVA, new CofreDeBotasIgnifugas(COFRE));

        costoDeMover(partida, Direccion.DERECHA);              // paso 1: abre el cofre
        partida.usarObjeto(BotasIgnifugas.ID);                 // paso 2: las usa
        int pasoDeUso = partida.getPasos();

        while (partida.getPasos() < pasoDeUso + DURACION_DE_ZAPATOS_Y_BOTAS - 1) {
            assertEquals(1, costoDeMover(partida, siguienteDireccionDentroDelPasillo()));
        }
        assertEquals(8, costoDeMover(partida, siguienteDireccionDentroDelPasillo()));
    }

    // =================== VENENO =================== //

    @Test
    void elVenenoSumaUnoPorPasoDuranteDiezPasosYDespuesSeVa() {
        Partida partida = pasilloDe(Terreno.PISO, new CofreDeVeneno(COFRE));

        // Paso 1: lo abre. Ese paso ya se cobró antes de abrirlo: cuesta 1.
        assertEquals(1, costoDeMover(partida, Direccion.DERECHA));
        int pasoDeApertura = partida.getPasos();

        // Del paso 2 al 10, cada paso cuesta 1 + 1.
        while (partida.getPasos() < pasoDeApertura + DURACION_DEL_VENENO - 1) {
            assertEquals(2, costoDeMover(partida, siguienteDireccionDentroDelPasillo()),
                    "en el paso " + (partida.getPasos() + 1) + " el veneno tendría que sumar 1");
        }
        // Paso 11: vence.
        assertEquals(1, costoDeMover(partida, siguienteDireccionDentroDelPasillo()));
        assertTrue(this.heroe.getEfectosActivos().isEmpty());
    }

    @Test
    void elVenenoNoCobraNadaSiElHeroeEspera() {
        Partida partida = pasilloDe(Terreno.PISO, new CofreDeVeneno(COFRE));
        costoDeMover(partida, Direccion.DERECHA);
        int energiaAntes = this.heroe.getEnergia();

        partida.esperar();

        assertEquals(energiaAntes, this.heroe.getEnergia());
    }

    @Test
    void elVenenoYLosZapatosSeCombinanEnLaPartida() {
        Partida partida = pasilloDe(Terreno.AGUA, new CofreDeVeneno(COFRE));
        this.heroe.getInventario().agregar(new ZapatosDeAgua());
        costoDeMover(partida, Direccion.DERECHA);              // paso 1: abre el veneno
        partida.usarObjeto(ZapatosDeAgua.ID);                  // paso 2

        // Agua como piso (1) más el veneno (+1).
        assertEquals(2, costoDeMover(partida, Direccion.DERECHA));
    }

    // =================== SOMNÍFERO =================== //

    @Test
    void elSomniferoDuermeALosMonstruosCincoPasosDePartida() {
        Cazador cazador = new Cazador(new Posicion(FILA, 11), 30, COSTOS);
        Partida partida = pasilloDe(Terreno.PISO, new CofreDeSomnifero(COFRE), cazador);

        partida.moverHeroe(Direccion.DERECHA);                 // paso 1: abre el cofre
        // En el paso de la apertura el cazador todavía se mueve.
        assertEquals(new Posicion(FILA, 10), cazador.getPosicion());

        for (int paso = 2; paso <= 1 + DURACION_DEL_SOMNIFERO; paso++) {
            partida.esperar();
            assertTrue(cazador.estaAdormecido(), "en el paso " + paso + " tendría que dormir");
            assertEquals(new Posicion(FILA, 10), cazador.getPosicion(),
                    "en el paso " + paso + " no se tendría que mover");
        }

        partida.esperar();                                     // paso 7: se despierta
        assertFalse(cazador.estaAdormecido());
        assertEquals(new Posicion(FILA, 9), cazador.getPosicion());
    }

    @Test
    void unMonstruoDormidoQueElHeroePisaSeDespiertaSinHacerleDanio() {
        Cazador cazador = new Cazador(new Posicion(FILA, 6), 30, COSTOS);
        Partida partida = pasilloDe(Terreno.PISO, new CofreDeSomnifero(COFRE), cazador);
        partida.moverHeroe(Direccion.DERECHA);                 // paso 1: abre; el cazador va a (3,5)
        partida.moverHeroe(Direccion.DERECHA);                 // paso 2: se duerme; héroe a (3,4)
        assertTrue(cazador.estaAdormecido());
        int energiaAntes = this.heroe.getEnergia();

        partida.moverHeroe(Direccion.DERECHA);                 // paso 3: el héroe lo pisa

        assertFalse(cazador.estaAdormecido());
        assertEquals(energiaAntes - 1, this.heroe.getEnergia());
    }

    // =================== REPELENTE =================== //

    @Test
    void elRepelenteMandaAlCazadorASuInicioEnElPasoSiguiente() {
        Posicion inicioDelCazador = new Posicion(FILA, 11);
        Cazador cazador = new Cazador(inicioDelCazador, 30, COSTOS);
        Partida partida = pasilloDe(Terreno.PISO, new CofreDeRepelente(COFRE), cazador);
        partida.esperar();
        partida.esperar();
        partida.esperar();                                     // el cazador llega a (3,8)
        assertEquals(new Posicion(FILA, 8), cazador.getPosicion());

        partida.moverHeroe(Direccion.DERECHA);                 // paso 4: abre; el cazador va a (3,7)
        assertEquals(new Posicion(FILA, 7), cazador.getPosicion());

        partida.esperar();                                     // paso 5: vuelve al inicio y da un paso

        assertEquals(new Posicion(FILA, 10), cazador.getPosicion());
    }

    // =================== PICO =================== //

    @Test
    void elPicoRompeLaParedAlEmpezarElPasoSiguienteDeUsarlo() {
        Partida partida = pasilloDe(Terreno.PISO, new CofreDePico(COFRE));
        Posicion arriba = new Posicion(FILA - 1, 3);

        partida.moverHeroe(Direccion.DERECHA);                 // paso 1: abre el cofre en (3,3)
        assertTrue(this.heroe.getInventario().contiene(Pico.ID));
        partida.usarObjeto(Pico.ID);                           // paso 2: lo usa
        assertEquals(Terreno.PARED, this.plano.getTablero().getTerreno(arriba));

        partida.esperar();                                     // paso 3: se rompe

        assertEquals(Terreno.PISO, this.plano.getTablero().getTerreno(arriba));
        assertTrue(partida.moverHeroe(Direccion.ARRIBA).accionValida());
        assertEquals(arriba, this.heroe.getUbicacion().posicion());
    }

    // =================== ESCUDO =================== //

    @Test
    void elEscudoDelCofreBloqueaAlCazadorDentroDeLaPartida() {
        Posicion inicioDelCazador = new Posicion(FILA, 5);
        Cazador cazador = new Cazador(inicioDelCazador, 30, COSTOS);
        Partida partida = pasilloDe(Terreno.PISO, new CofreDeEscudo(COFRE), cazador);

        partida.moverHeroe(Direccion.DERECHA);                 // paso 1: abre; el cazador va a (3,4)
        assertEquals(1, this.heroe.getEscudos());

        partida.esperar();                                     // paso 2: el cazador lo alcanza

        assertEquals(ENERGIA - 1, this.heroe.getEnergia());
        assertEquals(0, this.heroe.getEscudos());
        assertEquals(inicioDelCazador, cazador.getPosicion());
    }
}
