package ar.uba.fi.cb100.tp2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test de prueba: si pasa, Gradle, Java 25 y JUnit estan bien configurados.
 */
class PruebaTest {

    @Test
    void gradleYJunitFuncionan() {
        assertEquals(4, 2 + 2);
    }
}