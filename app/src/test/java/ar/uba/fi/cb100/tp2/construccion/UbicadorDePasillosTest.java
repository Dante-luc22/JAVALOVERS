package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UbicadorDePasillosTest {

    private final UbicadorDePasillos ubicador = new UbicadorDePasillos();

    @ParameterizedTest
    @CsvSource({
            "ARRIBA,    2, 4, 1, 4",
            "ABAJO,     4, 4, 5, 4",
            "IZQUIERDA, 3, 2, 3, 1",
            "DERECHA,   3, 6, 3, 7"
    })
    void encuentraUnExtremoEnCadaLadoSinAbrirLaPared(
            Direccion lado, int filaInterior, int columnaInterior,
            int filaBorde, int columnaBorde) {

        Plano plano = crearPlano(new Posicion(filaInterior, columnaInterior));
        Posicion extremo = new Posicion(filaBorde, columnaBorde);

        assertEquals(Optional.of(extremo), ubicador.buscarExtremo(plano, lado));
        assertEquals(Terreno.PARED, plano.getTablero().getTerreno(extremo));
    }

    @Test
    void descartaUnExtremoQueYaFueAbierto() {
        Plano plano = crearPlano(
                new Posicion(2, 6), new Posicion(2, 7), new Posicion(3, 6)
        );

        assertEquals(Optional.of(new Posicion(3, 7)),
                ubicador.buscarExtremo(plano, Direccion.DERECHA));
    }

    @Test
    void unaAberturaSobreElBordeNoHaceUtilUnaEsquina() {
        Plano plano = crearPlano(new Posicion(1, 6));

        assertTrue(ubicador.buscarExtremo(plano, Direccion.DERECHA).isEmpty());
    }

    private Plano crearPlano(Posicion... pisos) {
        Tablero tablero = new Tablero(5, 7);
        for (Posicion piso : pisos) {
            tablero.setTerreno(piso, Terreno.PISO);
        }
        return new Plano(tablero, pisos[0], null);
    }
}