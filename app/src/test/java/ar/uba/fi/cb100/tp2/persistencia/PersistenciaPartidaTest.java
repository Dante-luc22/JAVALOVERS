
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.AgendaDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba integrada de escritura, lectura y reconstrucción
 * de una partida mediante JSON.
 *
 * Alcance actual:
 * - Mazmorra sin entidades.
 * - Héroe sin objetos ni efectos activos.
 * - Agenda vacía.
 *
 * Los componentes pendientes se incorporarán en pruebas posteriores.
 */
class PersistenciaPartidaTest {

    @TempDir
    Path carpetaTemporal;

    @Test
    void guardarLeerYRestaurarConservaElEstadoDeLaPartida()
            throws Exception {

        // 1. Crear una partida real de prueba.
        Partida original = crearPartida();

        // 2. Crear los mapeadores.
        MapeadorPartida mapeador = new MapeadorPartida(
                new MapeadorHeroe(),
                new MapeadorMazmorra(),
                agenda -> {

                    // Adaptador provisional:
                    // todavía no representa la agenda real.
                    AgendaDTO dto = new AgendaDTO();
                    dto.paso = 7;
                    dto.pendientes = new ArrayList<>();

                    return dto;
                }
        );

        // 3. Convertir la partida a DTO.
        PartidaGuardadaDTO datos = mapeador.mapear(original);

        // 4. Escribir el JSON.
        Path archivo = carpetaTemporal.resolve("partida.json");

        EscritorPartida escritor = new EscritorPartida();
        escritor.guardar(datos, archivo);

        assertTrue(
                java.nio.file.Files.exists(archivo),
                "El archivo JSON debería haberse creado."
        );

        assertTrue(
                java.nio.file.Files.size(archivo) > 0,
                "El archivo JSON no debería estar vacío."
        );

        // 5. Leer y validar el JSON.
        LectorPartida lector = new LectorPartida();
        PartidaGuardadaDTO recuperado = lector.leer(archivo);

        // 6. Reconstruir los componentes de la partida.
        RestauradorPartida restaurador = new RestauradorPartida(
                new RestauradorMazmorra(),
                new RestauradorHeroe(),

                // Adaptador provisional de agenda vacía.
                agendaDTO -> new Agenda()
        );

        Partida reconstruida = restaurador.restaurar(recuperado);

        // 7. Comparar el estado original y el recuperado.

        // Pasos.
        assertEquals(original.getPasos(), reconstruida.getPasos());

        // Ubicación del héroe.
        assertEquals(
                original.getHeroe().getUbicacion(),
                reconstruida.getHeroe().getUbicacion()
        );

        // Energía actual y máxima.
        assertEquals(
                original.getHeroe().getEnergia(),
                reconstruida.getHeroe().getEnergia()
        );

        assertEquals(
                original.getHeroe().getEnergiaMaxima(),
                reconstruida.getHeroe().getEnergiaMaxima()
        );

        // Cofres abiertos.
        assertEquals(
                original.getHeroe().getCofresAbiertos(),
                reconstruida.getHeroe().getCofresAbiertos()
        );

        // Costos.
        assertEquals(
                original.getCostos(),
                reconstruida.getCostos()
        );

        // Cantidad de planos.
        assertEquals(
                original.getMazmorra().getPlanos().size(),
                reconstruida.getMazmorra().getPlanos().size()
        );

        // Conexiones.
        assertEquals(
                original.getMazmorra().getConexiones(),
                reconstruida.getMazmorra().getConexiones()
        );

        // Posición global de la salida.
        assertEquals(
                original.getMazmorra().getSalida(),
                reconstruida.getMazmorra().getSalida()
        );
    }

    /**
     * Crea una partida de prueba con el héroe ubicado
     * en el segundo piso.
     */
    private Partida crearPartida() {

        Plano primerPiso = crearPlano(false);
        Plano segundoPiso = crearPlano(true);

        Mazmorra mazmorra = new Mazmorra(
                List.of(primerPiso, segundoPiso),
                2
        );

        // Conectar los dos pisos.
        mazmorra.conectar(
                ubicacion(1, 1, 2, 3),
                ubicacion(1, 2, 3, 2)
        );

        // Héroe ubicado en el segundo piso.
        Heroe heroe = new Heroe(
                ubicacion(1, 2, 2, 2),
                100
        );

        heroe.restarEnergia(25);
        heroe.sumarCofreAbierto();

        Map<Terreno, Integer> costos =
                new EnumMap<>(Terreno.class);

        costos.put(Terreno.PISO, 1);
        costos.put(Terreno.AGUA, 3);
        costos.put(Terreno.LAVA, 8);

        return new Partida(
                mazmorra,
                heroe,
                costos,
                7,
                new Agenda()
        );
    }

    /**
     * Crea un plano de prueba sin entidades.
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
