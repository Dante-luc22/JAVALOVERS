package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.objetos.Objeto;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.HeroeDTO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del mapeo del héroe a su DTO.
 */
class MapeadorHeroeTest {

    private final MapeadorHeroe mapeador =
            new MapeadorHeroe();

    @Test
    void mapearConservaAtributosDelHeroe() {

        // Crear héroe.
        Heroe heroe = new Heroe(
                new Ubicacion(1, 2, new Posicion(2, 3)),
                100
        );

        // Modificar su estado.
        heroe.mover(
                new Ubicacion(1, 2, new Posicion(5, 7))
        );

        heroe.restarEnergia(25);
        heroe.sumarCofreAbierto();

        // Crear inventario con objetos de prueba.
        heroe.getInventario().agregar(
                new ObjetoPrueba("POCION")
        );

        heroe.getInventario().agregar(
                new ObjetoPrueba("LLAVE")
        );

        heroe.getInventario().agregar(
                new ObjetoPrueba("POCION")
        );

        // Mapear.
        HeroeDTO datos = mapeador.mapear(heroe);

        // Verificar ubicación.
        assertEquals(1, datos.ubicacion.tablero);
        assertEquals(2, datos.ubicacion.piso);
        assertEquals(5, datos.ubicacion.posicion.fila);
        assertEquals(7, datos.ubicacion.posicion.columna);

        // Verificar energía.
        assertEquals(75, datos.energia);
        assertEquals(100, datos.energiaMaxima);

        // Verificar cofres.
        assertEquals(1, datos.cofresAbiertos);

        // Verificar cantidades.
        assertEquals(2, datos.inventario.size());

        assertEquals("POCION", datos.inventario.get(0).id);
        assertEquals(2, datos.inventario.get(0).cantidad);

        assertEquals("LLAVE", datos.inventario.get(1).id);
        assertEquals(1, datos.inventario.get(1).cantidad);

        // Todavía no hay efectos activos.
        assertTrue(datos.efectosActivos.isEmpty());
    }

    @Test
    void mapearRechazaHeroeNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> mapeador.mapear(null)
        );
    }

    @Test
    void mapearRechazaEfectosActivosNoSoportados() {

        // Esta prueba queda pendiente hasta incorporar
        // una implementación concreta de Efecto.
    }

    /**
     * Objeto mínimo utilizado exclusivamente por las pruebas.
     */
    private static class ObjetoPrueba extends Objeto {

        ObjetoPrueba(String id) {
            super(id);
        }

        @Override
        public void usar(Heroe heroe, Agenda agenda) {
            // No se necesita comportamiento para esta prueba.
        }
    }
}
