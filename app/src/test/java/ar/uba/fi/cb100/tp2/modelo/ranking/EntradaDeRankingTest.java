package ar.uba.fi.cb100.tp2.modelo.ranking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void rechazaUnNombreDeMenosDeTresCaracteres() {
        assertThrows(IllegalArgumentException.class, () -> entrada("JU", 100, HOY));
        assertThrows(IllegalArgumentException.class, () -> entrada("", 100, HOY));
    }

    @Test
    void rechazaUnNombreDeMasDeTresCaracteres() {
        assertThrows(IllegalArgumentException.class, () -> entrada("JUAN", 100, HOY));
    }

    @Test
    void rechazaUnNombreConEspacios() {
        assertThrows(IllegalArgumentException.class, () -> entrada("J A", 100, HOY));
    }

    @Test
    void aceptaUnNombreDeExactamenteTresCaracteres() {
        assertEquals("JCP", entrada("JCP", 100, HOY).nombre());
        assertEquals("A1!", entrada("A1!", 100, HOY).nombre());
    }

    @Test
    void esNombreValidoSoloConTresCaracteresSinEspacios() {
        assertTrue(EntradaDeRanking.esNombreValido("JCP"));
        assertTrue(EntradaDeRanking.esNombreValido("A1!"));
        assertFalse(EntradaDeRanking.esNombreValido(null));
        assertFalse(EntradaDeRanking.esNombreValido(""));
        assertFalse(EntradaDeRanking.esNombreValido("JU"));
        assertFalse(EntradaDeRanking.esNombreValido("JUAN"));
        assertFalse(EntradaDeRanking.esNombreValido("J A"));
        assertFalse(EntradaDeRanking.esNombreValido("   "));
    }

    @Test
    void rechazaUnaDificultadNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new EntradaDeRanking("JUA", 100, null, HOY));
    }

    @Test
    void rechazaUnaFechaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new EntradaDeRanking("JUA", 100, Dificultad.FACIL, null));
    }

    @Test
    void rechazaUnPuntajeNegativo() {
        assertThrows(IllegalArgumentException.class, () -> entrada("JUA", -1, HOY));
    }

    @Test
    void aceptaUnPuntajeCero() {
        EntradaDeRanking entrada = entrada("JUA", 0, HOY);

        assertEquals(0, entrada.puntaje());
    }

    @Test
    void elPuntajeMasAltoVaPrimero() {
        EntradaDeRanking mejor = entrada("JUA", 300, HOY);
        EntradaDeRanking peor = entrada("FAC", 100, AYER);

        assertTrue(mejor.compareTo(peor) < 0);
        assertTrue(peor.compareTo(mejor) > 0);
    }

    @Test
    void conElMismoPuntajeVaPrimeroLaQueSeLogroAntes() {
        EntradaDeRanking anterior = entrada("JUA", 200, AYER);
        EntradaDeRanking posterior = entrada("FAC", 200, HOY);

        assertTrue(anterior.compareTo(posterior) < 0);
        assertTrue(posterior.compareTo(anterior) > 0);
    }

    @Test
    void conElMismoPuntajeYLaMismaFechaDesempataElNombre() {
        EntradaDeRanking facu = entrada("FAC", 200, HOY);
        EntradaDeRanking juan = entrada("JUA", 200, HOY);

        assertTrue(facu.compareTo(juan) < 0);
        assertTrue(juan.compareTo(facu) > 0);
    }

    @Test
    void dosEntradasConLosMismosDatosSonIgualesComparanCeroYTienenElMismoHash() {
        EntradaDeRanking una = entrada("JUA", 200, HOY);
        EntradaDeRanking otra = entrada("JUA", 200, HOY);

        assertEquals(una, otra);
        assertEquals(0, una.compareTo(otra));
        assertEquals(una.hashCode(), otra.hashCode());
    }

    @Test
    void dosEntradasQueDifierenEnUnSoloCampoNuncaComparanCero() {
        EntradaDeRanking base = new EntradaDeRanking("JUA", 200, Dificultad.FACIL, HOY);
        List<EntradaDeRanking> distintas = List.of(
                new EntradaDeRanking("FAC", 200, Dificultad.FACIL, HOY),
                new EntradaDeRanking("JUA", 201, Dificultad.FACIL, HOY),
                new EntradaDeRanking("JUA", 200, Dificultad.MEDIO, HOY),
                new EntradaDeRanking("JUA", 200, Dificultad.FACIL, AYER));

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
        EntradaDeRanking primera = entrada("JUA", 500, HOY);
        EntradaDeRanking segunda = entrada("FAC", 300, AYER);
        EntradaDeRanking tercera = entrada("MAT", 300, HOY);
        EntradaDeRanking cuarta = entrada("OWE", 0, AYER);
        List<EntradaDeRanking> entradas = new ArrayList<>(List.of(tercera, cuarta, primera, segunda));

        Collections.sort(entradas);

        assertEquals(List.of(primera, segunda, tercera, cuarta), entradas);
    }
}
