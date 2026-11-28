package ar.uba.fi.cb100.tp2.modelo.objetos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Tests de Inventario.primerId: el objeto que se guardó primero.
 */
class InventarioPrimerIdTest {

    @Test
    void unInventarioVacioNoTienePrimerId() {
        assertTrue(new Inventario().primerId().isEmpty());
    }

    @Test
    void elPrimerIdEsElDelObjetoQueSeGuardoPrimero() {
        Inventario inventario = new Inventario();
        inventario.agregar(new ZapatosDeAgua());
        inventario.agregar(new BotasIgnifugas());

        assertEquals(Optional.of(ZapatosDeAgua.ID), inventario.primerId());
    }

    @Test
    void alUsarElPrimeroElPrimerIdPasaAlSiguiente() {
        Inventario inventario = new Inventario();
        inventario.agregar(new ZapatosDeAgua());
        inventario.agregar(new BotasIgnifugas());

        inventario.quitar(ZapatosDeAgua.ID);

        assertEquals(Optional.of(BotasIgnifugas.ID), inventario.primerId());
    }
}
