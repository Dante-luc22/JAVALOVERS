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

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.RepelerMonstruos;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Cazador;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de CofreDeRepelente, del evento RepelerMonstruos y de Monstruo.repeler:
 * al abrirse, el cofre programa para el próximo paso que todos los monstruos
 * del plano vuelvan a su posición inicial.
 *
 * Plano de prueba: un pasillo en la fila 2, columnas 1 a 7; el héroe en (2,4).
 */
class CofreDeRepelenteTest {

    private static final Posicion HEROE = new Posicion(2, 4);
    private static final Posicion INICIO_IZQUIERDA = new Posicion(2, 1);
    private static final Posicion INICIO_DERECHA = new Posicion(2, 7);
    private static final Map<Terreno, Integer> COSTOS =
            Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8);

    private Heroe heroe;
    private Plano plano;
    private Partida partida;
    private Cazador izquierdo;
    private Cazador derecho;
    private CofreDeRepelente cofre;

    @BeforeEach
    void crear() {
        Tablero tablero = new Tablero(3, 7);
        for (int columna = 1; columna <= 7; columna++) {
            tablero.setTerreno(new Posicion(2, columna), Terreno.PISO);
        }
        this.plano = new Plano(tablero, INICIO_IZQUIERDA, INICIO_DERECHA);
        this.heroe = new Heroe(new Ubicacion(1, 1, HEROE), 100);
        this.partida = new Partida(new Mazmorra(List.of(this.plano), 1), this.heroe,
                Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8));

        this.izquierdo = new Cazador(INICIO_IZQUIERDA, 30, COSTOS);
        this.derecho = new Cazador(INICIO_DERECHA, 30, COSTOS);
        this.cofre = new CofreDeRepelente(HEROE);
        this.plano.agregarEntidad(this.izquierdo);
        this.plano.agregarEntidad(this.derecho);
        this.plano.agregarEntidad(this.cofre);
    }

    /** Los dos cazadores avanzan dos pasos hacia el héroe: quedan lejos de su inicio. */
    private void acercarLosMonstruos() {
        for (int paso = 0; paso < 2; paso++) {
            this.izquierdo.actuar(this.plano, this.heroe);
            this.derecho.actuar(this.plano, this.heroe);
        }
        assertEquals(new Posicion(2, 3), this.izquierdo.getPosicion());
        assertEquals(new Posicion(2, 5), this.derecho.getPosicion());
    }

    // =================== MONSTRUO.REPELER =================== //

    @Test
    void repelerDevuelveAlMonstruoASuInicio() {
        acercarLosMonstruos();

        this.izquierdo.repeler();

        assertEquals(INICIO_IZQUIERDA, this.izquierdo.getPosicion());
    }

    @Test
    void repelerNoLeHaceNadaAlHeroe() {
        acercarLosMonstruos();

        this.izquierdo.repeler();

        assertEquals(100, this.heroe.getEnergia());
    }

    // =================== EL EVENTO =================== //

    @Test
    void elEventoMandaATodosLosMonstruosASuInicio() {
        acercarLosMonstruos();

        new RepelerMonstruos().ejecutar(this.partida);

        assertEquals(INICIO_IZQUIERDA, this.izquierdo.getPosicion());
        assertEquals(INICIO_DERECHA, this.derecho.getPosicion());
    }

    @Test
    void elEventoNoMueveLosCofres() {
        new RepelerMonstruos().ejecutar(this.partida);

        assertEquals(HEROE, this.cofre.getPosicion());
    }

    @Test
    void conLosMonstruosEnSuInicioNoCambiaNada() {
        new RepelerMonstruos().ejecutar(this.partida);

        assertEquals(INICIO_IZQUIERDA, this.izquierdo.getPosicion());
        assertEquals(INICIO_DERECHA, this.derecho.getPosicion());
    }

    // =================== EL COFRE =================== //

    @Test
    void constructorRechazaPosicionNula() {
        assertThrows(IllegalArgumentException.class, () -> new CofreDeRepelente(null));
    }

    @Test
    void alAbrirseProgramaElEventoParaElProximoPaso() {
        Agenda agenda = new Agenda();

        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);

        assertEquals(1, agenda.getPendientes().size());
        assertEquals(1, agenda.getPendientes().get(0).paso());
        assertTrue(agenda.getPendientes().get(0).evento() instanceof RepelerMonstruos);
    }

    @Test
    void alAbrirseNoMueveALosMonstruosEnEseMomento() {
        acercarLosMonstruos();
        Agenda agenda = new Agenda();

        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);

        assertEquals(new Posicion(2, 3), this.izquierdo.getPosicion());
        assertEquals(new Posicion(2, 5), this.derecho.getPosicion());
    }

    @Test
    void alEmpezarElProximoPasoLosMonstruosVuelvenASuInicio() {
        acercarLosMonstruos();
        Agenda agenda = new Agenda();
        this.cofre.alEncontrarseConHeroe(this.heroe, agenda);

        agenda.ejecutarLosQueVencen(1, this.partida);

        assertEquals(INICIO_IZQUIERDA, this.izquierdo.getPosicion());
        assertEquals(INICIO_DERECHA, this.derecho.getPosicion());
        assertTrue(agenda.getPendientes().isEmpty());
    }

    @Test
    void sumaElCofreSinTocarLaEnergiaYQuedaParaSacarDelPlano() {
        this.cofre.alEncontrarseConHeroe(this.heroe, new Agenda());

        assertEquals(1, this.heroe.getCofresAbiertos());
        assertEquals(100, this.heroe.getEnergia());
        assertTrue(this.cofre.debeSalirDelPlano());
        assertTrue(this.heroe.getInventario().estaVacio());
    }

    @Test
    void recienCreadoEstaCerrado() {
        assertFalse(new CofreDeRepelente(HEROE).debeSalirDelPlano());
    }

    @Test
    void sePuedeRegistrarEnElCatalogo() {
        CatalogoDeCofres catalogo = new CatalogoDeCofres();
        catalogo.registrar("REPELENTE", CofreDeRepelente::new);

        assertTrue(catalogo.crearAlAzar(HEROE, new Random(1)) instanceof CofreDeRepelente);
    }
}
