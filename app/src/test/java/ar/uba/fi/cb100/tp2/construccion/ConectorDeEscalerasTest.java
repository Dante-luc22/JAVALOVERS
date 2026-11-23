package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Conexion;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConectorDeEscalerasTest {

    private final ConectorDeEscaleras conector = new ConectorDeEscaleras(new Random(42L));

    @Test
    void conectaTresPisosEnAmbosSentidosSinReutilizarLaCeldaDelPisoDos() {
        Posicion entrada = new Posicion(2, 2);
        Posicion primeraEscalera = new Posicion(2, 3);
        Posicion segundaEscalera = new Posicion(2, 4);

        Plano piso1 = crearPlano(entrada, primeraEscalera);
        Plano piso2 = crearPlano(entrada, primeraEscalera, segundaEscalera);
        Plano piso3 = crearPlano(entrada, primeraEscalera, segundaEscalera);

        List<Plano> planos = List.of(piso1, piso2, piso3);
        Mazmorra mazmorra = new Mazmorra(planos, 3);

        conector.conectarPisos(mazmorra, 1, planos);

        assertEquals(2, mazmorra.getConexiones().size());
        assertConexion(mazmorra, 1, 2, primeraEscalera);
        assertConexion(mazmorra, 2, 3, segundaEscalera);
    }

    @Test
    void informaElErrorSiNoPuedeConectarDosPisos() {
        Plano piso1 = crearPlano(new Posicion(2, 2));
        Plano piso2 = crearPlano(new Posicion(4, 4));

        List<Plano> planos = List.of(piso1, piso2);
        Mazmorra mazmorra = new Mazmorra(planos, 2);

        assertThrows(IllegalStateException.class,
                () -> conector.conectarPisos(mazmorra, 1, planos));
        assertTrue(mazmorra.getConexiones().isEmpty());
    }

    @Test
    void laMismaSemillaRepiteLasConexiones() {
        assertEquals(conexionesCon(42L), conexionesCon(42L));
    }

    private List<Conexion> conexionesCon(long semilla) {
        Posicion entrada = new Posicion(2, 2);
        Posicion a = new Posicion(2, 3);
        Posicion b = new Posicion(2, 4);
        Posicion c = new Posicion(3, 4);
        List<Plano> planos = List.of(
                crearPlano(entrada, a, b, c),
                crearPlano(entrada, a, b, c),
                crearPlano(entrada, a, b, c)
        );
        Mazmorra mazmorra = new Mazmorra(planos, 3);
        new ConectorDeEscaleras(new Random(semilla)).conectarPisos(mazmorra, 1, planos);
        return mazmorra.getConexiones();
    }

    private void assertConexion(Mazmorra mazmorra, int inferior, int superior, Posicion celda) {
        Ubicacion abajo = new Ubicacion(1, inferior, celda);
        Ubicacion arriba = new Ubicacion(1, superior, celda);

        assertEquals(Optional.of(arriba), mazmorra.destinoDesde(abajo));
        assertEquals(Optional.of(abajo), mazmorra.destinoDesde(arriba));
    }

    private Plano crearPlano(Posicion entrada, Posicion... pisos) {
        Tablero tablero = new Tablero(5, 5);
        tablero.setTerreno(entrada, Terreno.PISO);
        for (Posicion piso : pisos) {
            tablero.setTerreno(piso, Terreno.PISO);
        }
        return new Plano(tablero, entrada, null);
    }
}