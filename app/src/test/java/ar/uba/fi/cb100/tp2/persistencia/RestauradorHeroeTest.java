package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.efectos.TipoDeEfecto;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.HeroeDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.ObjetoInventarioDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EfectoDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PosicionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.UbicacionDTO;

import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        agregarObjeto(datos, "PICO", 1);

        Heroe heroe = restaurador.restaurar(datos);

        Map<String, Integer> cantidades =
                heroe.getInventario().getCantidades();

        assertEquals(2, cantidades.get("BOTAS_IGNIFUGAS"));
        assertEquals(3, cantidades.get("ZAPATOS_DE_AGUA"));
        assertEquals(1, cantidades.get("PICO"));

        assertEquals(
                List.of("BOTAS_IGNIFUGAS", "ZAPATOS_DE_AGUA", "PICO"),
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

        assertTrue(heroe.getInventario().getCantidades().isEmpty());
        assertEquals(0, heroe.getEscudos());
    }

    @Test
    void restaurarConservaCantidadDeEscudos() {
        HeroeDTO datos = crearHeroeDTO();
        datos.escudos = 3;

        Heroe heroe = restaurador.restaurar(datos);

        assertEquals(3, heroe.getEscudos());
    }

    @Test
    void restaurarRechazaEscudosNegativos() {
        HeroeDTO datos = crearHeroeDTO();
        datos.escudos = -1;

        assertThrows(
                IllegalArgumentException.class,
                () -> restaurador.restaurar(datos)
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
     * Comprueba que se reconstruyan correctamente los tres tipos
     * de efectos actualmente soportados.
     */
    @Test
    void restaurarConservaTiposDeEfectos() {

        HeroeDTO datos = crearHeroeDTO();

        agregarEfecto(datos, "AGUA_COMO_PISO");
        agregarEfecto(datos, "LAVA_COMO_PISO");
        agregarEfecto(datos, "ENVENENADO");

        Heroe heroe = restaurador.restaurar(datos);

        List<Efecto> efectos = heroe.getEfectosActivos();

        assertEquals(3, efectos.size());

        assertEquals(
                TipoDeEfecto.AGUA_COMO_PISO,
                efectos.get(0).getTipo()
        );

        assertEquals(
                TipoDeEfecto.LAVA_COMO_PISO,
                efectos.get(1).getTipo()
        );

        assertEquals(
                TipoDeEfecto.ENVENENADO,
                efectos.get(2).getTipo()
        );
    }

    /**
     * Comprueba que se rechacen tipos de efectos desconocidos.
     */
    @Test
    void restaurarRechazaEfectosDesconocidos() {

        HeroeDTO datos = crearHeroeDTO();

        agregarEfecto(datos, "EFECTO_INEXISTENTE");

        assertThrows(
                IllegalStateException.class,
                () -> restaurador.restaurar(datos)
        );
    }

    /**
     * Agrega un efecto a la representación DTO del héroe.
     *
     * Los efectos ya no guardan su duración:
     * esta se representa mediante eventos de la agenda.
     *
     * @param heroe héroe DTO
     * @param tipo identificador del efecto
     */
    private void agregarEfecto(
            HeroeDTO heroe,
            String tipo
    ) {

        EfectoDTO efecto = new EfectoDTO();

        efecto.tipo = tipo;
        efecto.datos = new JsonObject();

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