package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.objetos.Inventario;
import ar.uba.fi.cb100.tp2.modelo.objetos.BotasIgnifugas;
import ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.efectos.AguaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.efectos.LavaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.efectos.Envenenado;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.mundo.*;
import ar.uba.fi.cb100.tp2.modelo.partida.AzarPersistible;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.AgendaDTO;


import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO;

import com.google.gson.Gson;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapeadorRestauradorPartidaTest {

    /**
     * Comprueba el recorrido completo de persistencia:
     *
     * Partida -> DTO -> JSON -> DTO -> Partida.
     *
     * Verifica que se conserven los atributos del héroe,
     * su inventario, sus efectos activos, la mazmorra,
     * los costos y el estado del generador aleatorio.
     */
    @Test
    void recorridoCompletoConservaEstadoDeLaPartida() {

        // 1. Crear la partida original.
        Partida original = crearPartida();

        MapeadorPartida mapeador = new MapeadorPartida(
                new MapeadorHeroe(),
                new MapeadorMazmorra(),
                new MapeadorAgenda()::mapear
        );

        // 3. Convertir la partida en DTO.
        PartidaGuardadaDTO datos = mapeador.mapear(original);

        // 4. Serializar el DTO a JSON.
        Gson gson = new Gson();

        String json = gson.toJson(datos);

        // 5. Recuperar el DTO desde JSON.
        PartidaGuardadaDTO recuperado = gson.fromJson(
                json,
                PartidaGuardadaDTO.class
        );

        // 6. Crear el restaurador.
        RestauradorPartida restaurador = new RestauradorPartida(
                new RestauradorMazmorra(),
                new RestauradorHeroe(),
                new RestauradorAgenda()::restaurar
        );

        // 7. Reconstruir la partida.
        Partida restaurada = restaurador.restaurar(recuperado);

        // Comparar estado general de la partida.
        assertEquals(original.getPasos(), restaurada.getPasos());

        assertEquals(
                original.getAzar().getEstadoInterno(),
                restaurada.getAzar().getEstadoInterno()
        );

        assertEquals(
                original.getCostos(),
                restaurada.getCostos()
        );

        // Comparar atributos del héroe.
        Heroe heroeOriginal = original.getHeroe();
        Heroe heroeRestaurado = restaurada.getHeroe();

        assertEquals(
                heroeOriginal.getEnergia(),
                heroeRestaurado.getEnergia()
        );

        assertEquals(
                heroeOriginal.getEnergiaMaxima(),
                heroeRestaurado.getEnergiaMaxima()
        );

        assertEquals(
                heroeOriginal.getCofresAbiertos(),
                heroeRestaurado.getCofresAbiertos()
        );

        assertEquals(
                heroeOriginal.getUbicacion(),
                heroeRestaurado.getUbicacion()
        );

        // Comparar inventarios.
        Map<String, Integer> inventarioOriginal =
                heroeOriginal.getInventario().getCantidades();

        Map<String, Integer> inventarioRestaurado =
                heroeRestaurado.getInventario().getCantidades();

        assertEquals(
                inventarioOriginal,
                inventarioRestaurado
        );

        assertEquals(
                new ArrayList<>(inventarioOriginal.keySet()),
                new ArrayList<>(inventarioRestaurado.keySet())
        );

        // Comparar efectos activos.
        List<Efecto> efectosOriginales =
                heroeOriginal.getEfectosActivos();

        List<Efecto> efectosRestaurados =
                heroeRestaurado.getEfectosActivos();

        assertEquals(
                efectosOriginales.size(),
                efectosRestaurados.size()
        );

        for (int i = 0; i < efectosOriginales.size(); i++) {

            assertEquals(
                    efectosOriginales.get(i).getClass(),
                    efectosRestaurados.get(i).getClass()
            );

            assertEquals(
                    efectosOriginales.get(i).getPasosRestantes(),
                    efectosRestaurados.get(i).getPasosRestantes()
            );
        }

        // Comprobar que la mazmorra también fue reconstruida.
        assertNotNull(restaurada.getMazmorra());

        assertEquals(
                1,
                restaurada.getMazmorra().getPlanos().size()
        );

        // Comprobar que la agenda conserve su estado.
        assertEquals(
                original.getAgenda().getPasoActual(),
                restaurada.getAgenda().getPasoActual()
        );

        assertTrue(
                restaurada.getAgenda().getPendientes().isEmpty()
        );
    }

    /**
     * Comprueba que los efectos continúen funcionando después
     * de restaurar la partida completa.
     */
    @Test
    void efectosContinuanFuncionandoDespuesDeRestaurarPartida() {

        Partida original = crearPartida();

        Partida restaurada = recorrerJSON(original);

        Heroe heroe = restaurada.getHeroe();

        // Duraciones inmediatamente después de cargar.
        assertEquals(
                List.of(7, 12, 5),
                duraciones(heroe)
        );

        // Simular un paso de movimiento.
        heroe.pasarUnPaso();

        // Las duraciones deben disminuir exactamente una unidad.
        assertEquals(
                List.of(6, 11, 4),
                duraciones(heroe)
        );
    }

    /**
     * Realiza el recorrido completo de guardado y restauración.
     */
    private Partida recorrerJSON(Partida partida) {

        MapeadorPartida mapeador = new MapeadorPartida(
                new MapeadorHeroe(),
                new MapeadorMazmorra(),
                new MapeadorAgenda()::mapear
        );

        PartidaGuardadaDTO datos = mapeador.mapear(partida);

        Gson gson = new Gson();

        String json = gson.toJson(datos);

        PartidaGuardadaDTO recuperado = gson.fromJson(
                json,
                PartidaGuardadaDTO.class
        );

        RestauradorPartida restaurador = new RestauradorPartida(
                new RestauradorMazmorra(),
                new RestauradorHeroe(),
                new RestauradorAgenda()::restaurar
        );

        return restaurador.restaurar(recuperado);
    }

    /**
     * Devuelve las duraciones de los efectos activos respetando
     * su orden.
     */
    private List<Integer> duraciones(Heroe heroe) {

        return heroe.getEfectosActivos()
                .stream()
                .map(Efecto::getPasosRestantes)
                .toList();
    }

    /**
     * Construye una partida completa de prueba.
     */
    private Partida crearPartida() {

        // Tablero y mazmorra.
        Tablero tablero = new Tablero(3, 3);

        Posicion centro = new Posicion(2, 2);

        tablero.setTerreno(centro, Terreno.PISO);

        Plano plano = new Plano(
                tablero,
                centro,
                new Posicion(2, 2)
        );

        Mazmorra mazmorra = new Mazmorra(
                List.of(plano),
                1
        );

        // Inventario.
        Inventario inventario = new Inventario();

        inventario.restaurar(
                new BotasIgnifugas(),
                2
        );

        inventario.restaurar(
                new ZapatosDeAgua(),
                3
        );

        // Efectos activos.
        List<Efecto> efectos = List.of(
                new AguaComoPiso(7),
                new LavaComoPiso(12),
                new Envenenado(5)
        );

        // Héroe.
        Heroe heroe = new Heroe(
                new Ubicacion(1, 1, centro),
                75,
                100,
                1,
                inventario,
                efectos
        );

        // Costos de movimiento.
        Map<Terreno, Integer> costos =
                new EnumMap<>(Terreno.class);

        costos.put(Terreno.PISO, 1);
        costos.put(Terreno.AGUA, 3);
        costos.put(Terreno.LAVA, 8);

        // Generador aleatorio persistible.
        AzarPersistible azar = new AzarPersistible(12345L);

        Agenda agenda = new Agenda();
        agenda.restaurar(7, List.of());

        return new Partida(
                mazmorra,
                heroe,
                costos,
                7,
                agenda,
                azar
        );
    }
}