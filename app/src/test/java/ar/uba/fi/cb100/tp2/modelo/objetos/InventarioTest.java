package ar.uba.fi.cb100.tp2.modelo.objetos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de Inventario: tabla hash id → cantidad, con un objeto representante
 * por id. Usa un objeto de prueba que, al usarse, le devuelve energía al héroe.
 */
class InventarioTest {

    /** Un objeto de prueba: al usarse, suma su "fuerza" a la energía del héroe. */
    private static class ObjetoDePrueba extends Objeto {
        private final int fuerza;

        ObjetoDePrueba(String id, int fuerza) {
            super(id);
            this.fuerza = fuerza;
        }

        @Override
        public void usar(Heroe heroe, Agenda agenda) {
            heroe.sumarEnergia(this.fuerza);
        }
    }

    private Inventario inventario;
    private Heroe heroe;

    @BeforeEach
    void crear() {
        this.inventario = new Inventario();
        this.heroe = new Heroe(new Ubicacion(0, 0, new Posicion(2, 2)), 100);
    }

    private static Objeto zapatos() {
        return new ObjetoDePrueba("ZAPATOS_DE_AGUA", 10);
    }

    private static Objeto botas() {
        return new ObjetoDePrueba("BOTAS_IGNIFUGAS", 20);
    }

    // =================== VACÍO =================== //

    @Test
    void recienCreadoEstaVacio() {
        assertTrue(this.inventario.estaVacio());
        assertEquals(0, this.inventario.cantidad("ZAPATOS_DE_AGUA"));
        assertFalse(this.inventario.contiene("ZAPATOS_DE_AGUA"));
    }

    // =================== AGREGAR =================== //

    @Test
    void agregarUnoLoContiene() {
        this.inventario.agregar(zapatos());

        assertFalse(this.inventario.estaVacio());
        assertTrue(this.inventario.contiene("ZAPATOS_DE_AGUA"));
        assertEquals(1, this.inventario.cantidad("ZAPATOS_DE_AGUA"));
    }

    @Test
    void agregarDosIgualesSumaLaCantidad() {
        this.inventario.agregar(zapatos());
        this.inventario.agregar(zapatos());

        assertEquals(2, this.inventario.cantidad("ZAPATOS_DE_AGUA"));
    }

    @Test
    void agregarDistintosLosCuentaPorSeparado() {
        this.inventario.agregar(zapatos());
        this.inventario.agregar(botas());
        this.inventario.agregar(botas());

        assertEquals(1, this.inventario.cantidad("ZAPATOS_DE_AGUA"));
        assertEquals(2, this.inventario.cantidad("BOTAS_IGNIFUGAS"));
    }

    @Test
    void agregarRechazaNulo() {
        assertThrows(IllegalArgumentException.class, () -> this.inventario.agregar(null));
        assertTrue(this.inventario.estaVacio());
    }

    // =================== QUITAR =================== //

    @Test
    void quitarDevuelveElObjetoConEseId() {
        this.inventario.agregar(zapatos());

        Objeto quitado = this.inventario.quitar("ZAPATOS_DE_AGUA");

        assertEquals("ZAPATOS_DE_AGUA", quitado.getId());
    }

    @Test
    void quitarUnoDeDosDejaUno() {
        this.inventario.agregar(zapatos());
        this.inventario.agregar(zapatos());

        this.inventario.quitar("ZAPATOS_DE_AGUA");

        assertEquals(1, this.inventario.cantidad("ZAPATOS_DE_AGUA"));
        assertTrue(this.inventario.contiene("ZAPATOS_DE_AGUA"));
    }

    @Test
    void quitarElUltimoLoSacaDelTodo() {
        this.inventario.agregar(zapatos());

        this.inventario.quitar("ZAPATOS_DE_AGUA");

        assertEquals(0, this.inventario.cantidad("ZAPATOS_DE_AGUA"));
        assertFalse(this.inventario.contiene("ZAPATOS_DE_AGUA"));
        assertTrue(this.inventario.estaVacio());
    }

    @Test
    void quitarNoTocaLosOtrosObjetos() {
        this.inventario.agregar(zapatos());
        this.inventario.agregar(botas());

        this.inventario.quitar("ZAPATOS_DE_AGUA");

        assertEquals(1, this.inventario.cantidad("BOTAS_IGNIFUGAS"));
    }

    @Test
    void sePuedeVolverAAgregarDespuesDeQuitarElUltimo() {
        this.inventario.agregar(zapatos());
        this.inventario.quitar("ZAPATOS_DE_AGUA");

        this.inventario.agregar(zapatos());

        assertEquals(1, this.inventario.cantidad("ZAPATOS_DE_AGUA"));
        assertEquals("ZAPATOS_DE_AGUA", this.inventario.quitar("ZAPATOS_DE_AGUA").getId());
    }

    @Test
    void quitarAlgoQueNoTieneLanzaExcepcionConElId() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> this.inventario.quitar("PICO"));
        assertTrue(error.getMessage().contains("PICO"));
    }

    @Test
    void lasConsultasRechazanIdNuloOVacio() {
        assertThrows(IllegalArgumentException.class, () -> this.inventario.cantidad(null));
        assertThrows(IllegalArgumentException.class, () -> this.inventario.contiene("  "));
        assertThrows(IllegalArgumentException.class, () -> this.inventario.quitar(null));
    }

    // =================== USAR =================== //

    @Test
    void usarAplicaElEfectoYConsumeElObjeto() {
        this.heroe.restarEnergia(50);
        this.inventario.agregar(zapatos());

        this.inventario.usar("ZAPATOS_DE_AGUA", this.heroe, new Agenda());

        assertEquals(60, this.heroe.getEnergia());
        assertFalse(this.inventario.contiene("ZAPATOS_DE_AGUA"));
    }

    @Test
    void usarUnoDeDosDejaElOtro() {
        this.inventario.agregar(zapatos());
        this.inventario.agregar(zapatos());

        this.inventario.usar("ZAPATOS_DE_AGUA", this.heroe, new Agenda());

        assertEquals(1, this.inventario.cantidad("ZAPATOS_DE_AGUA"));
    }

    @Test
    void usarAlgoQueNoTieneLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> this.inventario.usar("PICO", this.heroe, new Agenda()));
    }

    @Test
    void usarConHeroeNuloNoPierdeElObjeto() {
        this.inventario.agregar(zapatos());

        assertThrows(IllegalArgumentException.class,
                () -> this.inventario.usar("ZAPATOS_DE_AGUA", null, new Agenda()));

        assertEquals(1, this.inventario.cantidad("ZAPATOS_DE_AGUA"));
    }

    // =================== QUITAR UNO AL AZAR (el ladrón) =================== //

    @Test
    void quitarUnoAlAzarConInventarioVacioDevuelveNull() {
        assertNull(this.inventario.quitarUnoAlAzar(new Random(1)));
    }

    @Test
    void quitarUnoAlAzarConUnSoloObjetoQuitaEse() {
        this.inventario.agregar(zapatos());

        Objeto robado = this.inventario.quitarUnoAlAzar(new Random(1));

        assertEquals("ZAPATOS_DE_AGUA", robado.getId());
        assertTrue(this.inventario.estaVacio());
    }

    @Test
    void quitarUnoAlAzarSacaUnoSolo() {
        this.inventario.agregar(zapatos());
        this.inventario.agregar(zapatos());
        this.inventario.agregar(botas());

        this.inventario.quitarUnoAlAzar(new Random(1));

        int quedan = this.inventario.cantidad("ZAPATOS_DE_AGUA")
                + this.inventario.cantidad("BOTAS_IGNIFUGAS");
        assertEquals(2, quedan);
    }

    @Test
    void quitarUnoAlAzarConMismaSemillaRobaLoMismo() {
        Inventario otro = new Inventario();
        for (Inventario cada : new Inventario[] {this.inventario, otro}) {
            cada.agregar(zapatos());
            cada.agregar(botas());
            cada.agregar(new ObjetoDePrueba("PICO", 0));
        }
        Random azarUno = new Random(42);
        Random azarOtro = new Random(42);

        for (int i = 0; i < 3; i++) {
            assertEquals(
                    this.inventario.quitarUnoAlAzar(azarUno).getId(),
                    otro.quitarUnoAlAzar(azarOtro).getId(),
                    "Robaron distinto en el robo " + (i + 1));
        }
        assertTrue(this.inventario.estaVacio());
    }

    @Test
    void quitarUnoAlAzarPuedeRobarCualquierTipo() {
        boolean roboZapatos = false;
        boolean roboBotas = false;
        Random azar = new Random(7);
        for (int i = 0; i < 50; i++) {
            Inventario lleno = new Inventario();
            lleno.agregar(zapatos());
            lleno.agregar(botas());
            String robado = lleno.quitarUnoAlAzar(azar).getId();
            roboZapatos = roboZapatos || robado.equals("ZAPATOS_DE_AGUA");
            roboBotas = roboBotas || robado.equals("BOTAS_IGNIFUGAS");
        }
        assertTrue(roboZapatos && roboBotas, "En 50 robos nunca salio uno de los dos tipos");
    }

    @Test
    void quitarUnoAlAzarRechazaAzarNulo() {
        this.inventario.agregar(zapatos());
        assertThrows(IllegalArgumentException.class, () -> this.inventario.quitarUnoAlAzar(null));
    }
}
