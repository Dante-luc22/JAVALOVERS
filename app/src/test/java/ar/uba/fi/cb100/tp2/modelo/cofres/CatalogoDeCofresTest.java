package ar.uba.fi.cb100.tp2.modelo.cofres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Tests de CatalogoDeCofres: registro de tipos y creación al azar.
 * Para distinguir dos "tipos" sin depender de los cofres que todavía no
 * existen, se usa un cofre de prueba que guarda una etiqueta.
 */
class CatalogoDeCofresTest {

    private static final Posicion LUGAR = new Posicion(4, 5);

    private CatalogoDeCofres catalogo;

    /** Un cofre de prueba que solo sabe qué etiqueta le pusieron. */
    private static class CofreDePrueba extends Cofre {
        private final String etiqueta;

        CofreDePrueba(Posicion posicion, String etiqueta) {
            super(posicion);
            this.etiqueta = etiqueta;
        }

        @Override
        protected void abrir(Heroe heroe, Agenda agenda) {
        }
    }

    @BeforeEach
    void crear() {
        this.catalogo = new CatalogoDeCofres();
    }

    private static FabricaDeCofre cofreConEtiqueta(String etiqueta) {
        return posicion -> new CofreDePrueba(posicion, etiqueta);
    }

    private static String etiqueta(Cofre cofre) {
        return ((CofreDePrueba) cofre).etiqueta;
    }

    // =================== REGISTRAR =================== //

    @Test
    void recienCreadoEstaVacio() {
        assertEquals(0, this.catalogo.cantidadDeTipos());
        assertFalse(this.catalogo.estaRegistrado("PUNTOS"));
    }

    @Test
    void registrarAgregaElTipo() {
        this.catalogo.registrar("PUNTOS", cofreConEtiqueta("A"));

        assertEquals(1, this.catalogo.cantidadDeTipos());
        assertTrue(this.catalogo.estaRegistrado("PUNTOS"));
        assertFalse(this.catalogo.estaRegistrado("POCION"));
    }

    @Test
    void registrarVariosTiposLosCuentaATodos() {
        this.catalogo.registrar("PUNTOS", cofreConEtiqueta("A"));
        this.catalogo.registrar("OTRO", cofreConEtiqueta("B"));

        assertEquals(2, this.catalogo.cantidadDeTipos());
    }

    @Test
    void registrarRechazaNombreNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.registrar(null, cofreConEtiqueta("A")));
    }

    @Test
    void registrarRechazaNombreVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.registrar("   ", cofreConEtiqueta("A")));
    }

    @Test
    void registrarRechazaFabricaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.registrar("PUNTOS", null));
    }

    @Test
    void registrarRechazaNombreRepetidoYConservaElPrimero() {
        this.catalogo.registrar("PUNTOS", cofreConEtiqueta("A"));

        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.registrar("PUNTOS", cofreConEtiqueta("Z")));

        assertEquals(1, this.catalogo.cantidadDeTipos());
        Cofre creado = this.catalogo.crearAlAzar(LUGAR, new Random(5));
        assertEquals("A", etiqueta(creado));
    }

    @Test
    void estaRegistradoRechazaNombreNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.estaRegistrado(null));
    }

    // =================== CREAR AL AZAR =================== //

    @Test
    void crearAlAzarConCatalogoVacioLanzaIllegalState() {
        assertThrows(IllegalStateException.class,
                () -> this.catalogo.crearAlAzar(LUGAR, new Random(5)));
    }

    @Test
    void crearAlAzarRechazaPosicionNula() {
        this.catalogo.registrar("PUNTOS", cofreConEtiqueta("A"));
        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.crearAlAzar(null, new Random(5)));
    }

    @Test
    void crearAlAzarRechazaAzarNulo() {
        this.catalogo.registrar("PUNTOS", cofreConEtiqueta("A"));
        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.crearAlAzar(LUGAR, null));
    }

    @Test
    void conUnSoloTipoSiempreCreaEseTipoEnLaPosicionPedida() {
        this.catalogo.registrar("PUNTOS", cofreConEtiqueta("A"));
        Random sorteo = new Random(5);

        for (int i = 0; i < 20; i++) {
            Cofre creado = this.catalogo.crearAlAzar(LUGAR, sorteo);
            assertEquals("A", etiqueta(creado));
            assertEquals(LUGAR, creado.getPosicion());
        }
    }

    @Test
    void cadaLlamadaCreaUnCofreNuevo() {
        this.catalogo.registrar("PUNTOS", cofreConEtiqueta("A"));
        Random sorteo = new Random(5);

        Cofre uno = this.catalogo.crearAlAzar(LUGAR, sorteo);
        Cofre otro = this.catalogo.crearAlAzar(LUGAR, sorteo);

        assertTrue(uno != otro, "Devolvio dos veces el mismo objeto");
    }

    @Test
    void conDosTiposSortealosDos() {
        this.catalogo.registrar("PUNTOS", cofreConEtiqueta("A"));
        this.catalogo.registrar("POCION", cofreConEtiqueta("B"));
        Random sorteo = new Random(5);

        boolean salioA = false;
        boolean salioB = false;
        for (int i = 0; i < 100; i++) {
            String salio = etiqueta(this.catalogo.crearAlAzar(LUGAR, sorteo));
            salioA = salioA || salio.equals("A");
            salioB = salioB || salio.equals("B");
        }

        assertTrue(salioA, "En 100 sorteos nunca salio el primer tipo");
        assertTrue(salioB, "En 100 sorteos nunca salio el segundo tipo");
    }

    @Test
    void mismaSemillaMismosTipos() {
        CatalogoDeCofres otroCatalogo = new CatalogoDeCofres();
        this.catalogo.registrar("PUNTOS", cofreConEtiqueta("A"));
        this.catalogo.registrar("POCION", cofreConEtiqueta("B"));
        otroCatalogo.registrar("PUNTOS", cofreConEtiqueta("A"));
        otroCatalogo.registrar("POCION", cofreConEtiqueta("B"));

        Random sorteoUno = new Random(42);
        Random sorteoOtro = new Random(42);
        for (int i = 0; i < 30; i++) {
            assertEquals(
                    etiqueta(this.catalogo.crearAlAzar(LUGAR, sorteoUno)),
                    etiqueta(otroCatalogo.crearAlAzar(LUGAR, sorteoOtro)),
                    "Distinto tipo en el sorteo " + i);
        }
    }
}
