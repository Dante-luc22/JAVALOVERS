package ar.uba.fi.cb100.tp2.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.partida.Interaccion;

/**
 * Tests del aviso de la tecla E en la barra de estado.
 */
class VistaGraficaAvisoTest {

    @Test
    void sinNadaConQueInteractuarNoSeAgregaNada() {
        assertEquals("", VistaGrafica.avisoDeInteraccion(Interaccion.NINGUNA));
    }

    @Test
    void cadaInteraccionDiceQueHaceLaTeclaE() {
        assertEquals(" | E: abrir cofre", VistaGrafica.avisoDeInteraccion(Interaccion.ABRIR_COFRE));
        assertEquals(" | E: usar escalera", VistaGrafica.avisoDeInteraccion(Interaccion.USAR_ESCALERA));
        assertEquals(" | E: usar pasillo", VistaGrafica.avisoDeInteraccion(Interaccion.USAR_PASILLO));
    }
}
