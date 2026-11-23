package ar.uba.fi.cb100.tp2.modelo.ranking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

/**
 * Tests de EntradaDeRanking: las validaciones, el orden del ranking (más
 * puntaje primero, empate por fecha) y que compareTo sea coherente con
 * equals y hashCode, como pide el enunciado para los tipos que van en árboles.
 */
class EntradaDeRankingTest {

    private static final LocalDateTime AYER = LocalDateTime.of(2026, 11, 21, 18, 0);
    private static final LocalDateTime HOY = LocalDateTime.of(2026, 11, 22, 15, 30, 12);

    /** Una entrada de Fácil con el nombre, el puntaje y la fecha dados. */
    private static EntradaDeRanking entrada(String nombre, int puntaje, LocalDateTime fecha) {
        return new EntradaDeRanking(nombre, puntaje, Dificultad.FACIL, fecha);
    }

    @Test
    void rechazaUnNombreNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> new EntradaDeRanking(null, 100, Dificultad.FACIL, HOY));
    }

    @Test
    void rechazaUnNombreVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> new EntradaDeRanking("   ", 100, Dificultad.FACIL, HOY));
    }

    @Test
    void rechazaUnaDificultadNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new EntradaDeRanking("Juan", 100, null, HOY));
    }

    @Test
    void rechazaUnaFechaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new EntradaDeRanking("Juan", 100, Dificultad.FACIL, null));
    }

    @Test
    void rechazaUnPuntajeNegativo() {
        assertThrows(IllegalArgumentException.class, () -> entrada("Juan", -1, HOY));
    }

    @Test
    void aceptaUnPuntajeCero() {
        EntradaDeRanking entrada = entrada("Juan", 0, HOY);

        assertEquals(0, entrada.puntaje());
    }

    @Test
    void elPuntajeMasAltoVaPrimero() {
        EntradaDeRanking mejor = entrada("Juan", 300, HOY);
        EntradaDeRanking peor = entrada("Facu", 100, AYER);

        assertTrue(mejor.compareTo(peor) < 0);
        assertTrue(peor.compareTo(mejor) > 0);
    }

    @Test
    void conElMismoPuntajeVaPrimeroLaQueSeLogroAntes() {
        EntradaDeRanking anterior = entrada("Juan", 200, AYER);
        EntradaDeRanking posterior = entrada("Facu", 200, HOY);

        assertTrue(anterior.compareTo(posterior) < 0);
        assertTrue(posterior.compareTo(anterior) > 0);
    }

    @Test
    void conElMismoPuntajeYLaMismaFechaDesempataElNombre() {
        EntradaDeRanking facu = entrada("Facu", 200, HOY);
        EntradaDeRanking juan = entrada("Juan", 200, HOY);

        assertTrue(facu.compareTo(juan) < 0);
        assertTrue(juan.compareTo(facu) > 0);
    }

    @Test
    void dosEntradasConLosMismosDatosSonIgualesComparanCeroYTienenElMismoHash() {
        EntradaDeRanking una = entrada("Juan", 200, HOY);
        EntradaDeRanking otra = entrada("Juan", 200, HOY);

        assertEquals(una, otra);
        assertEquals(0, una.compareTo(otra));
        assertEquals(una.hashCode(), otra.hashCode());
    }

    @Test
    void dosEntradasQueDifierenEnUnSoloCampoNuncaComparanCero() {
        EntradaDeRanking base = new EntradaDeRanking("Juan", 200, Dificultad.FACIL, HOY);
        List<EntradaDeRanking> distintas = List.of(
                new EntradaDeRanking("Facu", 200, Dificultad.FACIL, HOY),
                new EntradaDeRanking("Juan", 201, Dificultad.FACIL, HOY),
                new EntradaDeRanking("Juan", 200, Dificultad.MEDIO, HOY),
                new EntradaDeRanking("Juan", 200, Dificultad.FACIL, AYER));

        for (EntradaDeRanking distinta : distintas) {
            assertNotEquals(base, distinta);
            assertNotEquals(0, base.compareTo(distinta));
            // antisimetría: si una va antes, la otra va después
            assertEquals(Integer.signum(base.compareTo(distinta)),
                    -Integer.signum(distinta.compareTo(base)));
        }
    }

    @Test
    void ordenarUnaListaDejaElOrdenDelRanking() {
        EntradaDeRanking primera = entrada("Juan", 500, HOY);
        EntradaDeRanking segunda = entrada("Facu", 300, AYER);
        EntradaDeRanking tercera = entrada("Mati", 300, HOY);
        EntradaDeRanking cuarta = entrada("Owen", 0, AYER);
        List<EntradaDeRanking> entradas = new ArrayList<>(List.of(tercera, cuarta, primera, segunda));

        Collections.sort(entradas);

        assertEquals(List.of(primera, segunda, tercera, cuarta), entradas);
    }
}
