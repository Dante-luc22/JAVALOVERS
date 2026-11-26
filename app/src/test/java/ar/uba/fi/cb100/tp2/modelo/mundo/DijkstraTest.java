package ar.uba.fi.cb100.tp2.modelo.mundo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Tests de Dijkstra: el camino de costo mínimo con el costo de cada terreno.
 *
 * El caso principal es el que pide el enunciado: un camino con menos pasos que
 * cuesta más que otro más largo.
 * <pre>
 *   col   2 3 4 5
 *   fila 2  . . . .
 *   fila 3  O ~ ~ D      O = origen, D = destino, ~ = agua
 * </pre>
 * Por el agua son 3 pasos y cuesta 3 + 3 + 1 = 7; por arriba son 5 pasos y
 * cuesta 5. Con los costos de la configuración (piso 1, agua 3, lava 8), el
 * camino corto es el caro.
 */
class DijkstraTest {

    private static final Map<Terreno, Integer> COSTOS = costos(1, 3, 8);

    private static final Posicion ORIGEN = new Posicion(3, 2);
    private static final Posicion DESTINO = new Posicion(3, 5);

    // =================== AYUDAS =================== //

    private static Map<Terreno, Integer> costos(int piso, int agua, int lava) {
        Map<Terreno, Integer> costos = new EnumMap<>(Terreno.class);
        costos.put(Terreno.PISO, piso);
        costos.put(Terreno.AGUA, agua);
        costos.put(Terreno.LAVA, lava);
        return costos;
    }

    /** El dibujo de la documentación de la clase, con el terreno dado en el medio de la fila 3. */
    private static Tablero conTerrenoEnElMedio(Terreno medio) {
        Tablero tablero = new Tablero(5, 7);
        for (int columna = 2; columna <= 5; columna++) {
            tablero.setTerreno(new Posicion(2, columna), Terreno.PISO);
        }
        tablero.setTerreno(new Posicion(3, 2), Terreno.PISO);
        tablero.setTerreno(new Posicion(3, 3), medio);
        tablero.setTerreno(new Posicion(3, 4), medio);
        tablero.setTerreno(new Posicion(3, 5), Terreno.PISO);
        return tablero;
    }

    /** Un pasillo de piso en la fila 3, de la columna 2 a la 5. */
    private static Tablero pasillo() {
        Tablero tablero = new Tablero(5, 7);
        for (int columna = 2; columna <= 5; columna++) {
            tablero.setTerreno(new Posicion(3, columna), Terreno.PISO);
        }
        return tablero;
    }

    // =================== EL CAMINO CON MENOS PASOS CUESTA MAS =================== //

    @Test
    void elCaminoConMenosPasosPuedeCostarMasYDijkstraRodea() {
        Tablero tablero = conTerrenoEnElMedio(Terreno.AGUA);

        List<Posicion> camino = Dijkstra.caminoMinimo(tablero, COSTOS, ORIGEN, DESTINO);

        // por arriba: 5 pasos, 6 celdas contando el origen
        assertEquals(List.of(
                new Posicion(3, 2), new Posicion(2, 2), new Posicion(2, 3),
                new Posicion(2, 4), new Posicion(2, 5), new Posicion(3, 5)), camino);
        assertEquals(5, Dijkstra.costoMinimo(tablero, COSTOS, ORIGEN, DESTINO));
    }

    @Test
    void elCaminoPorElAguaTieneMenosPasosPeroCuestaMas() {
        Tablero tablero = conTerrenoEnElMedio(Terreno.AGUA);

        int pasosPorElAgua = 3;
        int costoPorElAgua = 3 + 3 + 1;
        int pasosPorArriba = 5;
        int costoPorArriba = Dijkstra.costoMinimo(tablero, COSTOS, ORIGEN, DESTINO);

        assertTrue(pasosPorElAgua < pasosPorArriba);
        assertTrue(costoPorElAgua > costoPorArriba);
    }

    @Test
    void conAguaBarataElCaminoCortoGana() {
        Tablero tablero = conTerrenoEnElMedio(Terreno.AGUA);

        // con agua a 1 es igual que el piso: lo mejor son los 3 pasos directos
        List<Posicion> camino = Dijkstra.caminoMinimo(tablero, costos(1, 1, 8), ORIGEN, DESTINO);

        assertEquals(4, camino.size());
        assertEquals(new Posicion(3, 3), camino.get(1));
    }

    @Test
    void conLavaElRodeoGanaAunConMuchosPasos() {
        Tablero tablero = conTerrenoEnElMedio(Terreno.LAVA);

        List<Posicion> camino = Dijkstra.caminoMinimo(tablero, COSTOS, ORIGEN, DESTINO);

        assertEquals(6, camino.size());
        assertEquals(5, Dijkstra.costoMinimo(tablero, COSTOS, ORIGEN, DESTINO));
    }

    @Test
    void siElRodeoNoExisteSeCruzaElAgua() {
        Tablero tablero = conTerrenoEnElMedio(Terreno.AGUA);
        // se tapa el rodeo de arriba
        for (int columna = 2; columna <= 5; columna++) {
            tablero.setTerreno(new Posicion(2, columna), Terreno.PARED);
        }

        assertEquals(7, Dijkstra.costoMinimo(tablero, COSTOS, ORIGEN, DESTINO));
        assertEquals(4, Dijkstra.caminoMinimo(tablero, COSTOS, ORIGEN, DESTINO).size());
    }

    // =================== COSTOS HACIA EL DESTINO =================== //

    @Test
    void conTodoPisoElCostoEsLaCantidadDePasos() {
        Map<Posicion, Integer> costos = Dijkstra.costosHacia(pasillo(), COSTOS, DESTINO);

        assertEquals(0, costos.get(new Posicion(3, 5)));
        assertEquals(1, costos.get(new Posicion(3, 4)));
        assertEquals(2, costos.get(new Posicion(3, 3)));
        assertEquals(3, costos.get(new Posicion(3, 2)));
    }

    @Test
    void elDestinoCuestaCeroYSuCostoSePagaAlEntrar() {
        Tablero tablero = pasillo();
        tablero.setTerreno(DESTINO, Terreno.AGUA);

        Map<Posicion, Integer> costos = Dijkstra.costosHacia(tablero, COSTOS, DESTINO);

        assertEquals(0, costos.get(DESTINO));
        // la vecina paga el agua al entrar al destino
        assertEquals(3, costos.get(new Posicion(3, 4)));
    }

    @Test
    void lasParedesNoAparecen() {
        Map<Posicion, Integer> costos = Dijkstra.costosHacia(pasillo(), COSTOS, DESTINO);

        assertFalse(costos.containsKey(new Posicion(2, 3)));
        assertFalse(costos.containsKey(new Posicion(1, 1)));
        assertEquals(4, costos.size());
    }

    @Test
    void lasCeldasSeparadasPorUnaParedNoAparecen() {
        Tablero tablero = pasillo();
        tablero.setTerreno(new Posicion(3, 3), Terreno.PARED);

        Map<Posicion, Integer> costos = Dijkstra.costosHacia(tablero, COSTOS, DESTINO);

        assertFalse(costos.containsKey(new Posicion(3, 2)));
        assertTrue(costos.containsKey(new Posicion(3, 4)));
    }

    // =================== CASOS DE BORDE =================== //

    @Test
    void sinCaminoDevuelveVacioYCostoMenosUno() {
        Tablero tablero = pasillo();
        tablero.setTerreno(new Posicion(3, 3), Terreno.PARED);

        assertTrue(Dijkstra.caminoMinimo(tablero, COSTOS, ORIGEN, DESTINO).isEmpty());
        assertEquals(-1, Dijkstra.costoMinimo(tablero, COSTOS, ORIGEN, DESTINO));
    }

    @Test
    void siElOrigenEsElDestinoElCaminoEsEsaCeldaYCuestaCero() {
        List<Posicion> camino = Dijkstra.caminoMinimo(pasillo(), COSTOS, DESTINO, DESTINO);

        assertEquals(List.of(DESTINO), camino);
        assertEquals(0, Dijkstra.costoMinimo(pasillo(), COSTOS, DESTINO, DESTINO));
    }

    @Test
    void unOrigenEnUnaParedNoTieneCamino() {
        assertTrue(Dijkstra.caminoMinimo(pasillo(), COSTOS, new Posicion(2, 3), DESTINO).isEmpty());
        assertEquals(-1, Dijkstra.costoMinimo(pasillo(), COSTOS, new Posicion(2, 3), DESTINO));
    }

    @Test
    void conCaminosIgualDeBaratosSiempreEligeElMismo() {
        // un cuadrado de piso: de una esquina a la opuesta hay dos caminos de igual costo
        Tablero tablero = new Tablero(5, 5);
        for (int fila = 2; fila <= 3; fila++) {
            for (int columna = 2; columna <= 3; columna++) {
                tablero.setTerreno(new Posicion(fila, columna), Terreno.PISO);
            }
        }
        Posicion esquina = new Posicion(2, 2);
        Posicion opuesta = new Posicion(3, 3);

        List<Posicion> primera = Dijkstra.caminoMinimo(tablero, COSTOS, esquina, opuesta);
        List<Posicion> segunda = Dijkstra.caminoMinimo(tablero, COSTOS, esquina, opuesta);

        assertEquals(3, primera.size());
        assertEquals(primera, segunda);
    }

    // =================== VALIDACIONES =================== //

    @Test
    void rechazaArgumentosNulos() {
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.costosHacia(null, COSTOS, DESTINO));
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.costosHacia(pasillo(), null, DESTINO));
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.costosHacia(pasillo(), COSTOS, null));
        assertThrows(IllegalArgumentException.class,
                () -> Dijkstra.caminoMinimo(pasillo(), COSTOS, null, DESTINO));
        assertThrows(IllegalArgumentException.class,
                () -> Dijkstra.costoMinimo(pasillo(), COSTOS, null, DESTINO));
    }

    @Test
    void rechazaUnDestinoQueEsPared() {
        assertThrows(IllegalArgumentException.class,
                () -> Dijkstra.costosHacia(pasillo(), COSTOS, new Posicion(2, 3)));
    }

    @Test
    void rechazaUnDestinoFueraDelTablero() {
        assertThrows(IllegalArgumentException.class,
                () -> Dijkstra.costosHacia(pasillo(), COSTOS, new Posicion(99, 99)));
    }

    @Test
    void rechazaCostosIncompletos() {
        Map<Terreno, Integer> sinLava = new EnumMap<>(Terreno.class);
        sinLava.put(Terreno.PISO, 1);
        sinLava.put(Terreno.AGUA, 3);

        assertThrows(IllegalArgumentException.class, () -> Dijkstra.costosHacia(pasillo(), sinLava, DESTINO));
    }

    @Test
    void rechazaCostosQueNoSonPositivos() {
        assertThrows(IllegalArgumentException.class,
                () -> Dijkstra.costosHacia(pasillo(), costos(0, 3, 8), DESTINO));
        assertThrows(IllegalArgumentException.class,
                () -> Dijkstra.costosHacia(pasillo(), costos(1, -3, 8), DESTINO));
    }

    // =================== CONTRA UN CALCULO INDEPENDIENTE =================== //

    /**
     * Un plano de 25 × 40 con terrenos al azar. Se compara con otro cálculo, más
     * lento y más simple: se repite "mejorar cada celda con la de su vecina"
     * hasta que ya no cambia nada. Si Dijkstra falla en cualquier celda, se nota.
     */
    @Test
    void coincideConUnCalculoPorFuerzaBrutaEnUnPlanoDe25Por40() {
        Random azar = new Random(42);
        Tablero tablero = new Tablero(27, 42);
        Terreno[] posibles = {Terreno.PISO, Terreno.PISO, Terreno.PISO, Terreno.AGUA, Terreno.LAVA, Terreno.PARED};
        for (int fila = 2; fila <= 26; fila++) {
            for (int columna = 2; columna <= 41; columna++) {
                tablero.setTerreno(new Posicion(fila, columna), posibles[azar.nextInt(posibles.length)]);
            }
        }
        Posicion destino = new Posicion(14, 20);
        tablero.setTerreno(destino, Terreno.PISO);

        Map<Posicion, Integer> esperado = fuerzaBruta(tablero, COSTOS, destino);
        Map<Posicion, Integer> obtenido = Dijkstra.costosHacia(tablero, COSTOS, destino);

        assertEquals(esperado, obtenido);
    }

    @Test
    void elCaminoDevueltoCuestaLoQueDiceElCostoMinimo() {
        Random azar = new Random(7);
        Tablero tablero = new Tablero(27, 42);
        Terreno[] posibles = {Terreno.PISO, Terreno.PISO, Terreno.AGUA, Terreno.LAVA};
        for (int fila = 2; fila <= 26; fila++) {
            for (int columna = 2; columna <= 41; columna++) {
                tablero.setTerreno(new Posicion(fila, columna), posibles[azar.nextInt(posibles.length)]);
            }
        }
        Posicion origen = new Posicion(2, 2);
        Posicion destino = new Posicion(26, 41);

        List<Posicion> camino = Dijkstra.caminoMinimo(tablero, COSTOS, origen, destino);

        assertEquals(origen, camino.get(0));
        assertEquals(destino, camino.get(camino.size() - 1));
        int suma = 0;
        for (int i = 1; i < camino.size(); i++) {
            Posicion anterior = camino.get(i - 1);
            Posicion actual = camino.get(i);
            assertEquals(1, Math.abs(anterior.fila() - actual.fila())
                    + Math.abs(anterior.columna() - actual.columna()));
            suma += COSTOS.get(tablero.getTerreno(actual));
        }
        assertEquals(Dijkstra.costoMinimo(tablero, COSTOS, origen, destino), suma);
    }

    /** El costo hasta el destino repitiendo "mejorar con las vecinas" hasta que nada cambia. */
    private static Map<Posicion, Integer> fuerzaBruta(
            Tablero tablero, Map<Terreno, Integer> costos, Posicion destino) {
        Map<Posicion, Integer> mejores = new HashMap<>();
        mejores.put(destino, 0);
        boolean cambio = true;
        while (cambio) {
            cambio = false;
            for (int fila = 1; fila <= tablero.getFilas(); fila++) {
                for (int columna = 1; columna <= tablero.getColumnas(); columna++) {
                    Posicion celda = new Posicion(fila, columna);
                    if (!tablero.esTransitable(celda)) {
                        continue;
                    }
                    for (Posicion vecina : tablero.vecinasTransitables(celda)) {
                        Integer desdeLaVecina = mejores.get(vecina);
                        if (desdeLaVecina == null) {
                            continue;
                        }
                        int total = desdeLaVecina + costos.get(tablero.getTerreno(vecina));
                        Integer actual = mejores.get(celda);
                        if (actual == null || total < actual) {
                            mejores.put(celda, total);
                            cambio = true;
                        }
                    }
                }
            }
        }
        return mejores;
    }
}
