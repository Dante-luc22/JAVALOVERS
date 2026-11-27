package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.objetos.Inventario;
import ar.uba.fi.cb100.tp2.modelo.objetos.BotasIgnifugas;
import ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.efectos.AguaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.efectos.LavaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.efectos.Envenenado;
import ar.uba.fi.cb100.tp2.modelo.efectos.TipoDeEfecto;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.HeroeDTO;

import com.google.gson.Gson;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MapeadorRestauradorHeroeTest {

    private final MapeadorHeroe mapeador =
            new MapeadorHeroe();

    private final RestauradorHeroe restaurador =
            new RestauradorHeroe();

    /**
     * Comprueba el recorrido completo:
     *
     * Modelo -> DTO -> JSON -> DTO -> Modelo.
     *
     * Conserva los atributos del héroe, los objetos del inventario
     * y los tipos de los efectos activos.
     */
    @Test
    void recorridoCompletoConservaEstadoDelHeroe() {

        // 1. Crear el héroe original.
        Heroe original = crearHeroe();

        // 2. Convertir el héroe a DTO.
        HeroeDTO datos = mapeador.mapear(original);

        // 3. Serializar el DTO a JSON.
        Gson gson = new Gson();

        String json = gson.toJson(datos);

        // 4. Recuperar el DTO desde el JSON.
        HeroeDTO recuperado = gson.fromJson(
                json,
                HeroeDTO.class
        );

        // 5. Reconstruir el héroe.
        Heroe restaurado = restaurador.restaurar(recuperado);

        // Comprobar atributos generales.
        assertEquals(
                original.getEnergia(),
                restaurado.getEnergia()
        );

        assertEquals(
                original.getEnergiaMaxima(),
                restaurado.getEnergiaMaxima()
        );

        assertEquals(
                original.getCofresAbiertos(),
                restaurado.getCofresAbiertos()
        );

        assertEquals(original.getUbicacion(), restaurado.getUbicacion());
        assertEquals(original.getEscudos(), restaurado.getEscudos());

        // Comprobar cantidades y orden del inventario.
        assertEquals(original.getInventario().getCantidades(), restaurado.getInventario().getCantidades());

        assertEquals(
                new ArrayList<>(
                        original.getInventario()
                                .getCantidades()
                                .keySet()
                ),
                new ArrayList<>(
                        restaurado.getInventario()
                                .getCantidades()
                                .keySet()
                )
        );

        // Comprobar efectos activos.
        List<Efecto> efectosOriginales =
                original.getEfectosActivos();

        List<Efecto> efectosRestaurados =
                restaurado.getEfectosActivos();

        assertEquals(
                efectosOriginales.size(),
                efectosRestaurados.size()
        );

        for (int i = 0; i < efectosOriginales.size(); i++) {

            assertEquals(
                    efectosOriginales.get(i).getTipo(),
                    efectosRestaurados.get(i).getTipo()
            );
        }
    }

    /**
     * Comprueba que los efectos continúen funcionando correctamente
     * después de completar el recorrido por JSON.
     *
     * La duración ya no pertenece a los efectos:
     * se administra mediante los eventos de la agenda.
     */
    @Test
    void efectosConservanComportamientoDespuesDeRestaurar() {

        Heroe original = crearHeroe();

        HeroeDTO datos = mapeador.mapear(original);

        Gson gson = new Gson();

        String json = gson.toJson(datos);

        HeroeDTO recuperado = gson.fromJson(
                json,
                HeroeDTO.class
        );

        Heroe restaurado = restaurador.restaurar(recuperado);

        List<Efecto> efectos = restaurado.getEfectosActivos();

        assertEquals(3, efectos.size());

        // AguaComoPiso:
        // el terreno agua cuesta lo mismo que el piso.
        assertEquals(
                1,
                efectos.get(0).ajustarCosto(
                        Terreno.AGUA,
                        5,
                        1
                )
        );

        // LavaComoPiso:
        // el terreno lava cuesta lo mismo que el piso.
        assertEquals(
                1,
                efectos.get(1).ajustarCosto(
                        Terreno.LAVA,
                        6,
                        1
                )
        );

        // Envenenado:
        // aumenta en uno el costo de cualquier terreno.
        assertEquals(
                6,
                efectos.get(2).ajustarCosto(
                        Terreno.PISO,
                        5,
                        1
                )
        );
    }

    /**
     * Construye un héroe de prueba con inventario y efectos activos.
     */
    private Heroe crearHeroe() {

        Inventario inventario = new Inventario();

        inventario.restaurar(
                new BotasIgnifugas(),
                2
        );

        inventario.restaurar(
                new ZapatosDeAgua(),
                3
        );

        List<Efecto> efectos = List.of(
                new AguaComoPiso(),
                new LavaComoPiso(),
                new Envenenado()
        );

        Heroe heroe = new Heroe(
                new Ubicacion(
                        1,
                        2,
                        new Posicion(3, 4)
                ),
                80,
                100,
                4,
                inventario,
                efectos
        );

        heroe.agregarEscudo();
        heroe.agregarEscudo();

        return heroe;}
}