
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

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
