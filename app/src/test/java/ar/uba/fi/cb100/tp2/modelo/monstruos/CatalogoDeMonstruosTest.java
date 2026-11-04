package ar.uba.fi.cb100.tp2.modelo.monstruos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Tests de CatalogoDeMonstruos: registro de tipos y creación al azar.
 * Para distinguir dos "tipos" se usan errantes con distinto daño.
 */
class CatalogoDeMonstruosTest {

    private static final Posicion LUGAR = new Posicion(4, 5);

    private Random azarDeLosMonstruos;
    private CatalogoDeMonstruos catalogo;

    @BeforeEach
    void crear() {
        this.azarDeLosMonstruos = new Random(1);
        this.catalogo = new CatalogoDeMonstruos();
    }

    private FabricaDeMonstruo erranteConDanio(int danio) {
        return posicion -> new Errante(posicion, danio, this.azarDeLosMonstruos);
    }

    // =================== REGISTRAR =================== //

    @Test
    void recienCreadoEstaVacio() {
        assertEquals(0, this.catalogo.cantidadDeTipos());
        assertFalse(this.catalogo.estaRegistrado("ERRANTE"));
    }

    @Test
    void registrarAgregaElTipo() {
        this.catalogo.registrar("ERRANTE", erranteConDanio(30));

        assertEquals(1, this.catalogo.cantidadDeTipos());
        assertTrue(this.catalogo.estaRegistrado("ERRANTE"));
        assertFalse(this.catalogo.estaRegistrado("CAZADOR"));
    }

    @Test
    void registrarVariosTiposLosCuentaATodos() {
        this.catalogo.registrar("ERRANTE", erranteConDanio(30));
        this.catalogo.registrar("OTRO", erranteConDanio(10));

        assertEquals(2, this.catalogo.cantidadDeTipos());
    }

    @Test
    void registrarRechazaNombreNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.registrar(null, erranteConDanio(30)));
    }

    @Test
    void registrarRechazaNombreVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.registrar("   ", erranteConDanio(30)));
    }

    @Test
    void registrarRechazaFabricaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.registrar("ERRANTE", null));
    }

    @Test
    void registrarRechazaNombreRepetidoYConservaElPrimero() {
        this.catalogo.registrar("ERRANTE", erranteConDanio(30));

        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.registrar("ERRANTE", erranteConDanio(99)));

        assertEquals(1, this.catalogo.cantidadDeTipos());
        Monstruo creado = this.catalogo.crearAlAzar(LUGAR, new Random(5));
        assertEquals(30, creado.getDanio());
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
        this.catalogo.registrar("ERRANTE", erranteConDanio(30));
        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.crearAlAzar(null, new Random(5)));
    }

    @Test
    void crearAlAzarRechazaAzarNulo() {
        this.catalogo.registrar("ERRANTE", erranteConDanio(30));
        assertThrows(IllegalArgumentException.class,
                () -> this.catalogo.crearAlAzar(LUGAR, null));
    }

    @Test
    void conUnSoloTipoSiempreCreaEseTipoEnLaPosicionPedida() {
        this.catalogo.registrar("ERRANTE", erranteConDanio(30));
        Random sorteo = new Random(5);

        for (int i = 0; i < 20; i++) {
            Monstruo creado = this.catalogo.crearAlAzar(LUGAR, sorteo);
            assertTrue(creado instanceof Errante);
            assertEquals(LUGAR, creado.getPosicion());
            assertEquals(LUGAR, creado.getPosicionInicial());
        }
    }

    @Test
    void cadaLlamadaCreaUnMonstruoNuevo() {
        this.catalogo.registrar("ERRANTE", erranteConDanio(30));
        Random sorteo = new Random(5);

        Monstruo uno = this.catalogo.crearAlAzar(LUGAR, sorteo);
        Monstruo otro = this.catalogo.crearAlAzar(LUGAR, sorteo);

        assertTrue(uno != otro, "Devolvio dos veces el mismo objeto");
    }

    @Test
    void conDosTiposSortealosDos() {
        this.catalogo.registrar("FUERTE", erranteConDanio(30));
        this.catalogo.registrar("DEBIL", erranteConDanio(10));
        Random sorteo = new Random(5);

        boolean salioFuerte = false;
        boolean salioDebil = false;
        for (int i = 0; i < 100; i++) {
            int danio = this.catalogo.crearAlAzar(LUGAR, sorteo).getDanio();
            salioFuerte = salioFuerte || danio == 30;
            salioDebil = salioDebil || danio == 10;
        }

        assertTrue(salioFuerte, "En 100 sorteos nunca salio el primer tipo");
        assertTrue(salioDebil, "En 100 sorteos nunca salio el segundo tipo");
    }

    @Test
    void mismaSemillaMismosTipos() {
        CatalogoDeMonstruos otroCatalogo = new CatalogoDeMonstruos();
        this.catalogo.registrar("FUERTE", erranteConDanio(30));
        this.catalogo.registrar("DEBIL", erranteConDanio(10));
        otroCatalogo.registrar("FUERTE", erranteConDanio(30));
        otroCatalogo.registrar("DEBIL", erranteConDanio(10));

        Random sorteoUno = new Random(42);
        Random sorteoOtro = new Random(42);
        for (int i = 0; i < 30; i++) {
            assertEquals(
                    this.catalogo.crearAlAzar(LUGAR, sorteoUno).getDanio(),
                    otroCatalogo.crearAlAzar(LUGAR, sorteoOtro).getDanio(),
                    "Distinto tipo en el sorteo " + i);
        }
    }
}
