package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.partida.AzarPersistible;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;
import ar.uba.fi.cb100.tp2.persistencia.RestauradorMazmorra;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.*;

import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestauradorMazmorraAzarTest {

    @Test
    void restaurarErranteConservaElGeneradorAleatorioCompartido() {

        AzarPersistible azar = new AzarPersistible(12345L);

        Entidad entidad = restaurarEntidad("Errante", azar);

        assertInstanceOf(Errante.class, entidad);

        Errante errante = (Errante) entidad;

        assertSame(
                azar,
                errante.getAzar(),
                "El Errante debe recibir la misma instancia de azar."
        );
    }

    @Test
    void restaurarLadronConservaElGeneradorAleatorioCompartido() {

        AzarPersistible azar = new AzarPersistible(67890L);

        Entidad entidad = restaurarEntidad("Ladron", azar);

        assertInstanceOf(Ladron.class, entidad);

        Ladron ladron = (Ladron) entidad;

        assertSame(
                azar,
                ladron.getAzar(),
                "El Ladron debe recibir la misma instancia de azar."
        );
    }

    private Entidad restaurarEntidad(
            String tipo,
            AzarPersistible azar
    ) {

        MazmorraDTO datos = crearMazmorraDTO(tipo);

        RestauradorMazmorra restaurador =
                new RestauradorMazmorra();

        Mazmorra mazmorra = restaurador.restaurar(datos, azar);

        Plano plano = mazmorra.getPlano(
                new Ubicacion(1, 1, new Posicion(1, 1))
        );

        assertEquals(1, plano.getEntidades().size());

        return plano.getEntidades().get(0);
    }

    private MazmorraDTO crearMazmorraDTO(String tipo) {

        MazmorraDTO mazmorra = new MazmorraDTO();

        mazmorra.pisosPorTablero = 1;
        mazmorra.planos = new ArrayList<>();
        mazmorra.conexiones = new ArrayList<>();

        PlanoDTO plano = new PlanoDTO();

        // Tablero de prueba de 4 x 4.
        TableroDTO tablero = new TableroDTO();

        tablero.filas = 4;
        tablero.columnas = 4;
        tablero.celdas = new ArrayList<>();

        for (int fila = 0; fila < 4; fila++) {
            tablero.celdas.add(new ArrayList<>(List.of("PISO", "PISO", "PISO", "PISO")));
        }

        plano.tablero = tablero;
        plano.entrada = posicion(1, 1);
        plano.salida = null;
        plano.entidades = new ArrayList<>();

        // Entidad que se va a restaurar.
        EntidadDTO entidad = new EntidadDTO();

        entidad.tipo = tipo;
        entidad.posicion = posicion(2, 3);

        entidad.datos = new JsonObject();
        entidad.datos.addProperty("filaInicial", 2);
        entidad.datos.addProperty("columnaInicial", 2);
        entidad.datos.addProperty("danio", 1);

        plano.entidades.add(entidad);
        mazmorra.planos.add(plano);

        return mazmorra;
    }

    private PosicionDTO posicion(int fila, int columna) {

        PosicionDTO posicion = new PosicionDTO();

        posicion.fila = fila;
        posicion.columna = columna;

        return posicion;
    }
}