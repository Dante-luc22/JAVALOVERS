
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;


import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeBotasIgnifugas;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePocion;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePuntos;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeTrampaDePinchos;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeVeneno;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeZapatosDeAgua;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.MazmorraDTO;

import com.google.gson.Gson;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprueba que una mazmorra real pueda convertirse a DTO,
 * pasar por JSON y reconstruirse sin perder su estructura.
 */
class MapeadorMazmorraTest {

    private final MapeadorMazmorra mapeador =
            new MapeadorMazmorra();

    private final RestauradorMazmorra restaurador =
            new RestauradorMazmorra();

    private final Gson gson = new Gson();

    @Test
    void idaYVueltaConservaLaMazmorraCompleta() {

        // 1. Crear una mazmorra real.
        Mazmorra original = crearMazmorra();

        // 2. Convertir el modelo a DTO.
        MazmorraDTO dto = mapeador.mapear(original);

        assertEquals(2, dto.pisosPorTablero);
        assertEquals(2, dto.planos.size());
        assertEquals(1, dto.conexiones.size());

        // 3. Serializar a JSON.
        String json = gson.toJson(dto);

        assertNotNull(json);
        assertFalse(json.isBlank());

        // 4. Leer nuevamente el JSON.
        MazmorraDTO dtoRecuperado = gson.fromJson(
                json,
                MazmorraDTO.class
        );

        assertNotNull(dtoRecuperado);

        // 5. Reconstruir la mazmorra.
        Mazmorra reconstruida =
                restaurador.restaurar(dtoRecuperado);

        // 6. Comparar información general.
        assertEquals(
                original.getPisosPorTablero(),
                reconstruida.getPisosPorTablero()
        );

        assertEquals(
                original.getPlanos().size(),
                reconstruida.getPlanos().size()
        );

        // 7. Comparar todos los planos y sus terrenos.
        List<Plano> planosOriginales = original.getPlanos();
        List<Plano> planosRecuperados = reconstruida.getPlanos();

        for (int i = 0; i < planosOriginales.size(); i++) {

            Plano antes = planosOriginales.get(i);
            Plano despues = planosRecuperados.get(i);

            assertEquals(
                    antes.getEntrada(),
                    despues.getEntrada()
            );

            assertEquals(
                    antes.getSalida(),
                    despues.getSalida()
            );

            assertEquals(
                    antes.getTablero().getFilas(),
                    despues.getTablero().getFilas()
            );

            assertEquals(
                    antes.getTablero().getColumnas(),
                    despues.getTablero().getColumnas()
            );

            for (int fila = 1;
                 fila <= antes.getTablero().getFilas();
                 fila++) {

                for (int columna = 1;
                     columna <= antes.getTablero().getColumnas();
                     columna++) {

                    Posicion posicion =
                            new Posicion(fila, columna);

                    assertEquals(
                            antes.getTablero().getTerreno(posicion),
                            despues.getTablero().getTerreno(posicion),
                            "Terreno diferente en plano " + (i + 1)
                                    + ", fila " + fila
                                    + ", columna " + columna
                    );
                }
            }
        }

        // 8. Comparar las conexiones.
        assertEquals(
                original.getConexiones(),
                reconstruida.getConexiones()
        );

        // 9. Comprobar que las conexiones funcionan en ambos sentidos.
        Ubicacion origen = ubicacion(1, 1, 2, 3);
        Ubicacion destino = ubicacion(1, 2, 3, 2);

        assertEquals(
                Optional.of(destino),
                reconstruida.destinoDesde(origen)
        );

        assertEquals(
                Optional.of(origen),
                reconstruida.destinoDesde(destino)
        );

        // 10. Comprobar que la salida sigue estando en el segundo piso.
        assertEquals(
                new Ubicacion(1, 2, new Posicion(3, 3)),
                reconstruida.getSalida()
        );
    }


    @Test
    void idaYVueltaConservaLosSeisTiposDeCofres() {

        // 1. Crear un tablero con espacio para los seis cofres.
        Tablero tablero = new Tablero(8, 8);

        List<Posicion> posiciones = List.of(
                new Posicion(2, 2),
                new Posicion(3, 2),
                new Posicion(4, 2),
                new Posicion(5, 2),
                new Posicion(6, 2),
                new Posicion(7, 2)
        );

        // Todas las posiciones utilizadas son transitables.
        tablero.setTerreno(new Posicion(1, 1), Terreno.PISO);

        for (Posicion posicion : posiciones) {
            tablero.setTerreno(posicion, Terreno.PISO);
        }

        Plano plano = new Plano(
                tablero,
                new Posicion(1, 1),
                null
        );

        // 2. Agregar los seis tipos de cofres.
        plano.agregarEntidad(
                new CofreDeBotasIgnifugas(posiciones.get(0))
        );

        plano.agregarEntidad(
                new CofreDePocion(posiciones.get(1), 35)
        );

        plano.agregarEntidad(
                new CofreDePuntos(posiciones.get(2))
        );

        plano.agregarEntidad(
                new CofreDeTrampaDePinchos(posiciones.get(3), 12)
        );

        plano.agregarEntidad(
                new CofreDeVeneno(posiciones.get(4))
        );

        plano.agregarEntidad(
                new CofreDeZapatosDeAgua(posiciones.get(5))
        );

        Mazmorra original = new Mazmorra(
                List.of(plano),
                1
        );

        // 3. Convertir la mazmorra a DTO.
        MazmorraDTO dto = mapeador.mapear(original);

        assertEquals(6, dto.planos.get(0).entidades.size());

        // 4. Serializar y recuperar desde JSON.
        String json = gson.toJson(dto);

        MazmorraDTO dtoRecuperado = gson.fromJson(
                json,
                MazmorraDTO.class
        );

        // 5. Reconstruir la mazmorra.
        Mazmorra reconstruida = restaurador.restaurar(dtoRecuperado);

        List<Entidad> entidades =
                reconstruida.getPlanos().get(0).getEntidades();

        // 6. Verificar cantidad y orden.
        assertEquals(6, entidades.size());

        assertEquals(
                CofreDeBotasIgnifugas.class,
                entidades.get(0).getClass()
        );

        assertEquals(
                CofreDePocion.class,
                entidades.get(1).getClass()
        );

        assertEquals(
                CofreDePuntos.class,
                entidades.get(2).getClass()
        );

        assertEquals(
                CofreDeTrampaDePinchos.class,
                entidades.get(3).getClass()
        );

        assertEquals(
                CofreDeVeneno.class,
                entidades.get(4).getClass()
        );

        assertEquals(
                CofreDeZapatosDeAgua.class,
                entidades.get(5).getClass()
        );

        // 7. Verificar que se conservaron las posiciones.
        for (int i = 0; i < posiciones.size(); i++) {
            assertEquals(
                    posiciones.get(i),
                    entidades.get(i).getPosicion()
            );
        }

        // 8. Verificar los atributos específicos.
        CofreDePocion pocion =
                (CofreDePocion) entidades.get(1);

        assertEquals(35, pocion.getCantidad());

        CofreDeTrampaDePinchos trampa =
                (CofreDeTrampaDePinchos) entidades.get(3);

        assertEquals(12, trampa.getDanio());

        // 9. Los cofres reconstruidos deben estar cerrados.
        for (Entidad entidad : entidades) {
            assertFalse(entidad.debeSalirDelPlano());
        }
    }


    /**
     * Construye una mazmorra de prueba con dos pisos conectados.
     */
    private Mazmorra crearMazmorra() {

        Plano primerPiso = crearPlano(false);
        Plano segundoPiso = crearPlano(true);

        Mazmorra mazmorra = new Mazmorra(
                List.of(primerPiso, segundoPiso),
                2
        );

        mazmorra.conectar(
                ubicacion(1, 1, 2, 3),
                ubicacion(1, 2, 3, 2)
        );

        return mazmorra;
    }

    /**
     * Construye un plano con terrenos diferenciados para la prueba.
     */
    private Plano crearPlano(boolean segundoPiso) {

        Tablero tablero = new Tablero(4, 4);

        tablero.setTerreno(
                new Posicion(2, 2),
                Terreno.PISO
        );

        tablero.setTerreno(
                new Posicion(2, 3),
                segundoPiso ? Terreno.LAVA : Terreno.AGUA
        );

        tablero.setTerreno(
                new Posicion(3, 2),
                segundoPiso ? Terreno.AGUA : Terreno.LAVA
        );

        tablero.setTerreno(
                new Posicion(3, 3),
                Terreno.PISO
        );

        Posicion entrada = new Posicion(2, 2);

        Posicion salida = segundoPiso
                ? new Posicion(3, 3)
                : null;

        return new Plano(tablero, entrada, salida);
    }

    private Ubicacion ubicacion(
            int tablero,
            int piso,
            int fila,
            int columna) {

        return new Ubicacion(
                tablero,
                piso,
                new Posicion(fila, columna)
        );
    }
}
