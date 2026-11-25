package ar.uba.fi.cb100.tp2.modelo.partida;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AzarPersistibleTest {

    @Test
    void mismaSemillaProduceLaMismaSecuenciaQueRandom() {

        long semilla = 123456789L;

        Random esperado = new Random(semilla);
        AzarPersistible obtenido = new AzarPersistible(semilla);

        for (int i = 0; i < 100; i++) {
            assertEquals(esperado.nextInt(), obtenido.nextInt());
        }

        for (int i = 0; i < 100; i++) {
            assertEquals(esperado.nextInt(7), obtenido.nextInt(7));
        }

        for (int i = 0; i < 100; i++) {
            assertEquals(esperado.nextLong(), obtenido.nextLong());
        }

        for (int i = 0; i < 100; i++) {
            assertEquals(esperado.nextBoolean(), obtenido.nextBoolean());
        }
    }

    @Test
    void restaurarEstadoContinuaLaMismaSecuencia() {

        AzarPersistible original = new AzarPersistible(987654321L);

        // Consumir varios números antes de guardar.
        for (int i = 0; i < 25; i++) {
            original.nextInt();
        }

        // Simular el guardado del estado.
        long estadoGuardado = original.getEstadoInterno();

        // Simular la carga de la partida.
        AzarPersistible restaurado =
                AzarPersistible.desdeEstado(estadoGuardado);

        // Ambos deben continuar exactamente igual.
        for (int i = 0; i < 100; i++) {
            assertEquals(original.nextInt(), restaurado.nextInt());
        }
    }

    @Test
    void rechazaEstadosFueraDelRangoPermitido() {

        assertThrows(
                IllegalArgumentException.class,
                () -> AzarPersistible.desdeEstado(-1L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> AzarPersistible.desdeEstado(1L << 48)
        );
    }

    @Test
    void permiteRestaurarElEstadoDeUnGeneradorExistente() {

        AzarPersistible original = new AzarPersistible(12345L);

        for (int i = 0; i < 10; i++) {
            original.nextInt();
        }

        long estado = original.getEstadoInterno();

        AzarPersistible otro = new AzarPersistible(999L);
        otro.restaurarEstado(estado);

        assertEquals(original.nextInt(), otro.nextInt());
    }
}
