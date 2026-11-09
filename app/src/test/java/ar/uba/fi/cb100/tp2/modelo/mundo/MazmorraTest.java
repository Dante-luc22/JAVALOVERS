package ar.uba.fi.cb100.tp2.modelo.mundo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

class MazmorraTest {

    private static final Posicion ENTRADA = new Posicion(2, 2);
    private static final Posicion SALIDA = new Posicion(4, 4);

    private Plano planoSinSalida() {
        return new Plano(new Tablero(6, 6), ENTRADA, null);
    }

    private Plano planoConSalida() {
        return new Plano(new Tablero(6, 6), ENTRADA, SALIDA);
    }

    private List<Plano> seisPlanosConSalidaEnElQuinto() {
        List<Plano> planos = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            planos.add(i == 4 ? planoConSalida() : planoSinSalida());
        }
        return planos;
    }

    @Test
    void conUnSoloPlanoLaUbicacionUnoUnoLoDevuelve() {
        Plano plano = planoConSalida();
        Mazmorra mazmorra = new Mazmorra(List.of(plano), 1);

        assertSame(plano, mazmorra.getPlano(new Ubicacion(1, 1, ENTRADA)));
    }

    @Test
    void elTableroDosPisoTresEsElSextoPlanoConTresPisosPorTablero() {
        List<Plano> planos = seisPlanosConSalidaEnElQuinto();
        Mazmorra mazmorra = new Mazmorra(planos, 3);

        assertSame(planos.get(5), mazmorra.getPlano(new Ubicacion(2, 3, ENTRADA)));
    }

    @Test
    void laEntradaEsElTableroUnoPisoUnoConLaEntradaDelPrimerPlano() {
        Mazmorra mazmorra = new Mazmorra(seisPlanosConSalidaEnElQuinto(), 3);

        assertEquals(new Ubicacion(1, 1, ENTRADA), mazmorra.getEntrada());
    }

    @Test
    void laSalidaTraeElTableroYElPisoDelPlanoQueLaTiene() {
        // el plano de la lista en la posición 4 es el tablero 2, piso 2
        Mazmorra mazmorra = new Mazmorra(seisPlanosConSalidaEnElQuinto(), 3);

        assertEquals(new Ubicacion(2, 2, SALIDA), mazmorra.getSalida());
    }

    @Test
    void sinNingunaSalidaPedirlaTiraError() {
        Mazmorra mazmorra = new Mazmorra(List.of(planoSinSalida()), 1);

        assertThrows(IllegalStateException.class, mazmorra::getSalida);
    }

    @Test
    void unaUbicacionQueNoExisteTiraError() {
        Mazmorra mazmorra = new Mazmorra(seisPlanosConSalidaEnElQuinto(), 3);

        assertThrows(IllegalArgumentException.class, () -> mazmorra.getPlano(new Ubicacion(3, 1, ENTRADA)));
        assertThrows(IllegalArgumentException.class, () -> mazmorra.getPlano(new Ubicacion(1, 4, ENTRADA)));
        assertThrows(IllegalArgumentException.class, () -> mazmorra.getPlano(new Ubicacion(0, 1, ENTRADA)));
        assertThrows(IllegalArgumentException.class, () -> mazmorra.getPlano(new Ubicacion(1, 0, ENTRADA)));
    }

    @Test
    void noSePuedeCrearUnaMazmorraSinPlanosOConPisosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Mazmorra(List.of(), 1));
        assertThrows(IllegalArgumentException.class, () -> new Mazmorra(List.of(planoSinSalida()), 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Mazmorra(List.of(planoSinSalida(), planoSinSalida()), 3));
    }

    @Test
    void cambiarLaListaOriginalNoAfectaALaMazmorra() {
        List<Plano> planos = new ArrayList<>(List.of(planoConSalida()));
        Mazmorra mazmorra = new Mazmorra(planos, 1);

        planos.clear();

        assertEquals(new Ubicacion(1, 1, SALIDA), mazmorra.getSalida());
    }
}
