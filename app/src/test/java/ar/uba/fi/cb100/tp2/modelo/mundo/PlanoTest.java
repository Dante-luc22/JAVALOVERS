package ar.uba.fi.cb100.tp2.modelo.mundo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;


/**
 * Tests de Plano: entrada, salida y manejo de la lista de entidades.
 */

public class PlanoTest {

    /**
     * Una entidad mínima que no hace nada, solo para poder probar el Plano
     * sin depender de Monstruo ni de Cofre.
     */
    private static class EntidadDePrueba extends Entidad {

        EntidadDePrueba(Posicion posicion) {
            super(posicion);
        }

        @Override
        public void actuar(Plano plano, Heroe heroe) {
        }

        @Override
        public void alEncontrarseConHeroe(Heroe heroe, Agenda agenda) {
        }
    }

    @Test
    void unPlanoNuevoGuardaEntradaYSalidaYNoTieneEntidades() {
        Tablero tablero = new Tablero(5, 5);
        Posicion entrada = new Posicion(2, 2);
        Posicion salida = new Posicion(4, 4);

        Plano plano = new Plano(tablero, entrada, salida);

        assertSame(tablero, plano.getTablero());
        assertEquals(entrada, plano.getEntrada());
        assertEquals(salida, plano.getSalida());
        assertTrue(plano.getEntidades().isEmpty());
    }

    @Test
    void unPlanoPuedeNoTenerSalida() {
        Plano plano = new Plano(new Tablero(5, 5), new Posicion(2, 2), null);

        assertNull(plano.getSalida());
    }

    @Test
    void obtenerEntidadesDevuelveLasQueEstanEnEsaCelda() {
        Plano plano = new Plano(new Tablero(5, 5), new Posicion(2, 2), null);
        Entidad entidad = new EntidadDePrueba(new Posicion(3, 3));

        plano.agregarEntidad(entidad);

        assertEquals(List.of(entidad), plano.obtenerEntidades(new Posicion(3, 3)));
        assertTrue(plano.obtenerEntidades(new Posicion(1, 1)).isEmpty());
    }

        @Test
    void dosEntidadesEnLaMismaCeldaSeDevuelvenLasDos() {
        Plano plano = new Plano(new Tablero(5, 5), new Posicion(2, 2), null);
        Entidad primera = new EntidadDePrueba(new Posicion(3, 3));
        Entidad segunda = new EntidadDePrueba(new Posicion(3, 3));

        plano.agregarEntidad(primera);
        plano.agregarEntidad(segunda);

        assertEquals(List.of(primera, segunda), plano.obtenerEntidades(new Posicion(3, 3)));
    }

    @Test
    void quitarEntidadLaSacaDelPlano() {
        Plano plano = new Plano(new Tablero(5, 5), new Posicion(2, 2), null);
        Entidad entidad = new EntidadDePrueba(new Posicion(3, 3));
        plano.agregarEntidad(entidad);

        plano.quitarEntidad(entidad);

        assertTrue(plano.getEntidades().isEmpty());
        assertTrue(plano.obtenerEntidades(new Posicion(3, 3)).isEmpty());
    }

    @Test
    void modificarLaListaDeGetEntidadesNoCambiaElPlano() {
        Plano plano = new Plano(new Tablero(5, 5), new Posicion(2, 2), null);
        plano.agregarEntidad(new EntidadDePrueba(new Posicion(3, 3)));

        plano.getEntidades().clear();

        assertEquals(1, plano.getEntidades().size());
    }

    @Test
    void sePuedeQuitarEntidadesMientrasSeRecorrenSinQueSeRompa() {
        Plano plano = new Plano(new Tablero(5, 5), new Posicion(2, 2), null);
        plano.agregarEntidad(new EntidadDePrueba(new Posicion(3, 3)));
        plano.agregarEntidad(new EntidadDePrueba(new Posicion(4, 4)));

        for (Entidad entidad : plano.getEntidades()) {
            plano.quitarEntidad(entidad);
        }

        assertTrue(plano.getEntidades().isEmpty());
    }
    
}
