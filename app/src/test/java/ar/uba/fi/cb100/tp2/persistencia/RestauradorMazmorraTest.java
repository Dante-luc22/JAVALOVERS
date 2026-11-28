
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.MazmorraDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PlanoDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.TableroDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PosicionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.UbicacionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.ConexionDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EntidadDTO;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de reconstrucción de mazmorras desde un DTO serializado.
 */
class RestauradorMazmorraTest {

    private final RestauradorMazmorra restaurador =
            new RestauradorMazmorra();

    /**
     * Comprueba que se conserven las dimensiones, terrenos,
     * entradas y salidas después del recorrido por JSON.
     */
    @Test
    void restaurarConservaTablerosYTerrenos() {

        MazmorraDTO datos = crearMazmorraDTO();

        Mazmorra restaurada = restaurarDesdeJson(datos);

        Plano primerPlano = restaurada.getPlano(new Ubicacion(1, 1, new Posicion(1, 1)));

        Plano segundoPlano = restaurada.getPlano(new Ubicacion(1, 2, new Posicion(1, 1)));

        // Dimensiones del primer tablero.
        assertEquals(4, primerPlano.getTablero().getFilas());
        assertEquals(4, primerPlano.getTablero().getColumnas());

        // Terrenos del primer piso.
        assertEquals(Terreno.AGUA, primerPlano.getTablero().getTerreno(new Posicion(2, 3)));

        assertEquals(Terreno.LAVA, primerPlano.getTablero().getTerreno(new Posicion(3, 2)));

        // El segundo piso tiene una distribución diferente.
        assertEquals(
                Terreno.LAVA,
                segundoPlano.getTablero().getTerreno(new Posicion(2, 3)));

        assertEquals(Terreno.AGUA, segundoPlano.getTablero().getTerreno(new Posicion(3, 2)));

        // Posiciones de entrada.
        assertEquals(
                new Posicion(2, 2),
                primerPlano.getEntrada()
        );

        assertEquals(
                new Posicion(2, 2),
                segundoPlano.getEntrada()
        );

        // El primer plano no tiene salida.
        assertNull(primerPlano.getSalida());

        // La salida está en el segundo plano.
        assertEquals(
                new Posicion(3, 3),
                restaurada.getSalida().posicion()
        );
    }

    /**
     * Comprueba que las conexiones se reconstruyan en los dos sentidos
     * y que no se dupliquen en la lista de conexiones.
     */
    @Test
    void restaurarConservaConexionesBidireccionales() {

        MazmorraDTO datos = crearMazmorraDTO();

        Mazmorra restaurada = restaurarDesdeJson(datos);

        Ubicacion origen = ubicacion(1, 1, 2, 3);
        Ubicacion destino = ubicacion(1, 2, 3, 2);

        // Recorrido desde el origen hacia el destino.
        assertEquals(
                Optional.of(destino),
                restaurada.destinoDesde(origen)
        );

        // Recorrido inverso.
        assertEquals(
                Optional.of(origen),
                restaurada.destinoDesde(destino)
        );

        // La conexión se conserva una sola vez.
        assertEquals(1, restaurada.getConexiones().size());

        var conexion = restaurada.getConexiones().get(0);

        assertEquals(origen, conexion.origen());
        assertEquals(destino, conexion.destino());
    }

    /**
     * Comprueba que los planos mantengan su orden original.
     */
    @Test
    void restaurarConservaOrdenDeLosPlanos() {

        MazmorraDTO datos = crearMazmorraDTO();

        Mazmorra restaurada = restaurarDesdeJson(datos);

        Plano pisoUno = restaurada.getPlano(new Ubicacion(1, 1, new Posicion(1, 1)));

        Plano pisoDos = restaurada.getPlano(new Ubicacion(1, 2, new Posicion(1, 1)));

        assertEquals(Terreno.AGUA, pisoUno.getTablero().getTerreno(new Posicion(2, 3)));

        assertEquals(Terreno.LAVA, pisoDos.getTablero().getTerreno(new Posicion(2, 3)));
    }

    /**
     * Comprueba que los monstruos que necesitan azar no puedan
     * restaurarse cuando no se proporciona un generador aleatorio.
     */
    @Test
    void restaurarRechazaMonstruosSinAzar() {

        for (String tipo : List.of("Errante", "Ladron")) {

            MazmorraDTO datos = crearMazmorraDTO();

            EntidadDTO entidad = new EntidadDTO();
            entidad.tipo = tipo;
            entidad.posicion = posicion(2, 2);

            entidad.datos = new JsonObject();
            entidad.datos.addProperty("filaInicial", 2);
            entidad.datos.addProperty("columnaInicial", 2);
            entidad.datos.addProperty("danio", 1);

            datos.planos.get(0).entidades.add(entidad);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> restaurador.restaurar(datos),
                    "Se esperaba un error al restaurar " + tipo + " sin azar."
            );
        }
    }

    /**
     * Simula el paso de los datos por JSON y luego reconstruye
     * los objetos reales del modelo.
     */
    private Mazmorra restaurarDesdeJson(MazmorraDTO datos) {

        Gson gson = new Gson();

        String json = gson.toJson(datos);

        MazmorraDTO recuperado = gson.fromJson(
                json,
                MazmorraDTO.class
        );

        return restaurador.restaurar(recuperado);
    }

    /**
     * Construye una mazmorra de prueba con dos pisos y una conexión.
     */
    private MazmorraDTO crearMazmorraDTO() {

        MazmorraDTO mazmorra = new MazmorraDTO();

        mazmorra.pisosPorTablero = 2;

        // Primer piso.
        mazmorra.planos.add(crearPlano(false));

        // Segundo piso.
        mazmorra.planos.add(crearPlano(true));

        // Conexión entre los dos pisos.
        ConexionDTO conexion = new ConexionDTO();

        conexion.origen = ubicacionDTO(1, 1, 2, 3);
        conexion.destino = ubicacionDTO(1, 2, 3, 2);

        mazmorra.conexiones = new ArrayList<>();
        mazmorra.conexiones.add(conexion);

        return mazmorra;
    }

    /**
     * Crea un plano de 4x4.
     *
     * El parámetro segundoPiso permite distinguir los terrenos
     * y verificar que no se intercambie el orden de los planos.
     */
    private PlanoDTO crearPlano(boolean segundoPiso) {

        PlanoDTO plano = new PlanoDTO();

        TableroDTO tablero = new TableroDTO();

        tablero.filas = 4;
        tablero.columnas = 4;

        tablero.celdas = new ArrayList<>();

        tablero.celdas.add(
                new ArrayList<>(List.of("PARED", "PARED", "PARED", "PARED")));

        if (segundoPiso) {

            tablero.celdas.add(
                    new ArrayList<>(List.of("PARED", "PISO", "LAVA", "PARED")));

            tablero.celdas.add(
                    new ArrayList<>(List.of("PARED", "AGUA", "PISO", "PARED")));

        } else {

            tablero.celdas.add(
                    new ArrayList<>(List.of("PARED", "PISO", "AGUA", "PARED")));

            tablero.celdas.add(
                    new ArrayList<>(List.of("PARED", "LAVA", "PISO", "PARED")));
        }

        tablero.celdas.add(
                new ArrayList<>(List.of("PARED", "PARED", "PARED", "PARED")));

        plano.tablero = tablero;

        plano.entrada = posicion(2, 2);

        plano.salida = segundoPiso
                ? posicion(3, 3)
                : null;

        plano.entidades = new ArrayList<>();

        return plano;
    }

    private PosicionDTO posicion(int fila, int columna) {

        PosicionDTO posicion = new PosicionDTO();

        posicion.fila = fila;
        posicion.columna = columna;

        return posicion;
    }

    private UbicacionDTO ubicacionDTO(
            int tablero,
            int piso,
            int fila,
            int columna) {

        UbicacionDTO ubicacion = new UbicacionDTO();

        ubicacion.tablero = tablero;
        ubicacion.piso = piso;
        ubicacion.posicion = posicion(fila, columna);

        return ubicacion;
    }

    private Ubicacion ubicacion(
            int tablero,
            int piso,
            int fila,
            int columna) {

        return new Ubicacion(tablero, piso, new Posicion(fila, columna));
    }
}