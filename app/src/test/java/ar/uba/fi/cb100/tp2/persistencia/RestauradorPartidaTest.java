package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.partida.AzarPersistible;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.*;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestauradorPartidaTest {

    private final RestauradorPartida restaurador =
            new RestauradorPartida(
                    new RestauradorMazmorra(),
                    new RestauradorHeroe(),
                    datos -> new Agenda()
            );

    /**
     * Comprueba que la restauración de una partida completa
     * conserve los atributos del héroe y su inventario.
     */
    @Test
    void restaurarPartidaConservaInventarioDelHeroe() {

        PartidaGuardadaDTO datos = crearPartidaDTO();

        Partida partida = restaurador.restaurar(datos);

        Heroe heroe = partida.getHeroe();

        // Atributos del héroe.
        assertEquals(100, heroe.getEnergia());
        assertEquals(100, heroe.getEnergiaMaxima());
        assertEquals(2, heroe.getCofresAbiertos());

        // Ubicación del héroe.
        assertEquals(
                new Posicion(2, 3),
                heroe.getUbicacion().posicion()
        );

        // Objetos y cantidades.
        var cantidades = heroe.getInventario().getCantidades();

        assertEquals(
                2,
                cantidades.get("BOTAS_IGNIFUGAS")
        );

        assertEquals(
                3,
                cantidades.get("ZAPATOS_DE_AGUA")
        );

        // Orden original del inventario.
        assertEquals(
                List.of("BOTAS_IGNIFUGAS", "ZAPATOS_DE_AGUA"),
                new ArrayList<>(cantidades.keySet())
        );
    }

    /**
     * Comprueba que también se conserven los demás datos
     * generales de la partida durante la restauración.
     */
    @Test
    void restaurarPartidaConservaEstadoGeneral() {

        PartidaGuardadaDTO datos = crearPartidaDTO();

        Partida partida = restaurador.restaurar(datos);

        assertEquals(15, partida.getPasos());
        assertEquals(Dificultad.FACIL, partida.getDificultad());
        assertEquals(12345L, partida.getSemilla());
        assertEquals(datos.estadoAzar, partida.getAzar().getEstadoInterno());
        assertEquals(1, partida.getCostos().get(Terreno.PISO));
        assertEquals(3, partida.getCostos().get(Terreno.AGUA));
        assertEquals(5, partida.getCostos().get(Terreno.LAVA));

        assertNotNull(partida.getMazmorra());
        assertNotNull(partida.getAgenda());
    }

    /**
     * Construye una partida DTO completa para las pruebas.
     */
    private PartidaGuardadaDTO crearPartidaDTO() {

        PartidaGuardadaDTO datos = new PartidaGuardadaDTO();

        datos.version = 1;
        datos.pasos = 15;
        datos.dificultad = "FACIL";
        datos.semilla = 12345L;

        AzarPersistible azarOriginal =
                new AzarPersistible(12345L);

        datos.estadoAzar = azarOriginal.getEstadoInterno();

        // Costos de movimiento.
        datos.costos = new LinkedHashMap<>();
        datos.costos.put("PISO", 1);
        datos.costos.put("AGUA", 3);
        datos.costos.put("LAVA", 5);

        // Héroe.
        datos.heroe = crearHeroeDTO();

        // Mazmorra.
        datos.mazmorra = crearMazmorraDTO();

        // Agenda vacía para esta prueba.
        datos.agenda = new AgendaDTO();
        datos.agenda.pendientes = new ArrayList<>();

        return datos;
    }

    /**
     * Construye los datos del héroe con dos tipos de objetos.
     */
    private HeroeDTO crearHeroeDTO() {

        HeroeDTO heroe = new HeroeDTO();

        heroe.ubicacion = new UbicacionDTO();
        heroe.ubicacion.tablero = 1;
        heroe.ubicacion.piso = 1;
        heroe.ubicacion.posicion = posicion(2, 3);

        heroe.energia = 100;
        heroe.energiaMaxima = 100;
        heroe.cofresAbiertos = 2;

        heroe.inventario = new ArrayList<>();

        heroe.inventario.add(
                crearObjeto("BOTAS_IGNIFUGAS", 2)
        );

        heroe.inventario.add(
                crearObjeto("ZAPATOS_DE_AGUA", 3)
        );

        heroe.efectosActivos = new ArrayList<>();

        return heroe;
    }

    /**
     * Construye una mazmorra sencilla con un único plano.
     */
    private MazmorraDTO crearMazmorraDTO() {

        MazmorraDTO mazmorra = new MazmorraDTO();

        mazmorra.pisosPorTablero = 1;
        mazmorra.planos = new ArrayList<>();
        mazmorra.conexiones = new ArrayList<>();

        PlanoDTO plano = new PlanoDTO();

        TableroDTO tablero = new TableroDTO();

        tablero.filas = 4;
        tablero.columnas = 4;
        tablero.celdas = new ArrayList<>();

        for (int fila = 0; fila < 4; fila++) {

            tablero.celdas.add(
                    new ArrayList<>(
                            List.of("PISO", "PISO", "PISO", "PISO")
                    )
            );
        }

        plano.tablero = tablero;
        plano.entrada = posicion(2, 2);
        plano.salida = null;
        plano.entidades = new ArrayList<>();

        mazmorra.planos.add(plano);

        return mazmorra;
    }

    /**
     * Construye un objeto del inventario DTO.
     */
    private ObjetoInventarioDTO crearObjeto(
            String id,
            int cantidad
    ) {

        ObjetoInventarioDTO objeto = new ObjetoInventarioDTO();

        objeto.id = id;
        objeto.cantidad = cantidad;

        return objeto;
    }

    /**
     * Construye una posición DTO.
     */
    private PosicionDTO posicion(int fila, int columna) {

        PosicionDTO posicion = new PosicionDTO();

        posicion.fila = fila;
        posicion.columna = columna;

        return posicion;
    }
}