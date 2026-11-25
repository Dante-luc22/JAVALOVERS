package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.efectos.AguaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.efectos.LavaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.efectos.Envenenado;

import com.google.gson.JsonObject;


class RestauradorHeroeTest {

    private final RestauradorHeroe restaurador =
            new RestauradorHeroe();

    /**
     * Comprueba que se reconstruyan los objetos soportados,
     * conservando sus cantidades y el orden de inserción.
     */
    @Test
    void restaurarConservaObjetosCantidadesYOrden() {

        HeroeDTO datos = crearHeroeDTO();

        agregarObjeto(datos, "BOTAS_IGNIFUGAS", 2);
        agregarObjeto(datos, "ZAPATOS_DE_AGUA", 3);

        Heroe heroe = restaurador.restaurar(datos);

        Map<String, Integer> cantidades =
                heroe.getInventario().getCantidades();

        assertEquals(
                2,
                cantidades.get("BOTAS_IGNIFUGAS")
        );

        assertEquals(
                3,
                cantidades.get("ZAPATOS_DE_AGUA")
        );

        assertEquals(
                List.of("BOTAS_IGNIFUGAS", "ZAPATOS_DE_AGUA"),
                new ArrayList<>(cantidades.keySet())
        );
    }

    /**
     * Comprueba que se pueda reconstruir un inventario vacío.
     */
    @Test
    void restaurarConservaInventarioVacio() {

        HeroeDTO datos = crearHeroeDTO();

        Heroe heroe = restaurador.restaurar(datos);

        assertTrue(
                heroe.getInventario().getCantidades().isEmpty()
        );
    }

    /**
     * Comprueba que se rechacen identificadores desconocidos.
     */
    @Test
    void restaurarRechazaObjetosDesconocidos() {

        HeroeDTO datos = crearHeroeDTO();

        agregarObjeto(datos, "OBJETO_INEXISTENTE", 1);

        assertThrows(
                IllegalStateException.class,
                () -> restaurador.restaurar(datos)
        );
    }

    /**
     * Comprueba que se rechacen cantidades iguales a cero.
     */
    @Test
    void restaurarRechazaCantidadCero() {

        HeroeDTO datos = crearHeroeDTO();

        agregarObjeto(datos, "BOTAS_IGNIFUGAS", 0);

        assertThrows(
                IllegalStateException.class,
                () -> restaurador.restaurar(datos)
        );
    }

    /**
     * Comprueba que se rechacen cantidades negativas.
     */
    @Test
    void restaurarRechazaCantidadesNegativas() {

        HeroeDTO datos = crearHeroeDTO();

        agregarObjeto(datos, "ZAPATOS_DE_AGUA", -2);

        assertThrows(
                IllegalStateException.class,
                () -> restaurador.restaurar(datos)
        );
    }
    /**
     * Comprueba que se reconstruyan los tres tipos de efectos,
     * conservando exactamente sus pasos restantes.
     */
    @Test
    void restaurarConservaTiposYDuracionDeLosEfectos() {

        HeroeDTO datos = crearHeroeDTO();

        agregarEfecto(datos, "AguaComoPiso", 7);
        agregarEfecto(datos, "LavaComoPiso", 12);
        agregarEfecto(datos, "Envenenado", 5);

        Heroe heroe = restaurador.restaurar(datos);

        List<Efecto> efectos = heroe.getEfectosActivos();

        assertEquals(3, efectos.size());

        // Comprobar los tipos reconstruidos.
        assertTrue(efectos.get(0) instanceof AguaComoPiso);
        assertTrue(efectos.get(1) instanceof LavaComoPiso);
        assertTrue(efectos.get(2) instanceof Envenenado);

        // Comprobar las duraciones originales restantes.
        assertEquals(7, efectos.get(0).getPasosRestantes());
        assertEquals(12, efectos.get(1).getPasosRestantes());
        assertEquals(5, efectos.get(2).getPasosRestantes());

        // Comprobar que los efectos siguen funcionando.
        efectos.get(0).pasarUnPaso();
        efectos.get(1).pasarUnPaso();
        efectos.get(2).pasarUnPaso();

        assertEquals(6, efectos.get(0).getPasosRestantes());
        assertEquals(11, efectos.get(1).getPasosRestantes());
        assertEquals(4, efectos.get(2).getPasosRestantes());
    }

    /**
     * Comprueba que no se reconstruyan efectos con duración cero.
     */
    @Test
    void restaurarRechazaEfectosConDuracionCero() {

        HeroeDTO datos = crearHeroeDTO();

        agregarEfecto(datos, "Envenenado", 0);

        assertThrows(
                IllegalStateException.class,
                () -> restaurador.restaurar(datos)
        );
    }

    /**
     * Comprueba que se rechacen tipos de efectos desconocidos.
     */
    @Test
    void restaurarRechazaEfectosDesconocidos() {

        HeroeDTO datos = crearHeroeDTO();

        agregarEfecto(datos, "EfectoInexistente", 10);

        assertThrows(
                IllegalStateException.class,
                () -> restaurador.restaurar(datos)
        );
    }

    /**
     * Agrega un efecto a la representación DTO del héroe.
     */
    private void agregarEfecto(
            HeroeDTO heroe,
            String tipo,
            int pasosRestantes
    ) {

        EfectoDTO efecto = new EfectoDTO();

        efecto.tipo = tipo;
        efecto.datos = new JsonObject();

        efecto.datos.addProperty(
                "pasosRestantes",
                pasosRestantes
        );

        heroe.efectosActivos.add(efecto);
    }

    /**
     * Crea un héroe DTO válido, sin objetos ni efectos.
     */
    private HeroeDTO crearHeroeDTO() {

        HeroeDTO datos = new HeroeDTO();

        datos.ubicacion = new UbicacionDTO();
        datos.ubicacion.tablero = 1;
        datos.ubicacion.piso = 1;

        datos.ubicacion.posicion = new PosicionDTO();
        datos.ubicacion.posicion.fila = 2;
        datos.ubicacion.posicion.columna = 3;

        datos.energia = 100;
        datos.energiaMaxima = 100;
        datos.cofresAbiertos = 0;

        datos.inventario = new ArrayList<>();
        datos.efectosActivos = new ArrayList<>();

        return datos;
    }

    /**
     * Agrega una entrada al inventario DTO.
     */
    private void agregarObjeto(
            HeroeDTO heroe,
            String id,
            int cantidad
    ) {

        ObjetoInventarioDTO objeto = new ObjetoInventarioDTO();

        objeto.id = id;
        objeto.cantidad = cantidad;

        heroe.inventario.add(objeto);
    }
}