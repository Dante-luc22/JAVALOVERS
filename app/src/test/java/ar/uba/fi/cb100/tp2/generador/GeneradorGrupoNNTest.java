package ar.uba.fi.cb100.tp2.generador;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ar.uba.fi.cb100.tp2.construccion.ValidadorPlano;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorGrupoNNTest {

    /**
     * Entrada válida nueva para cada prueba.
     */
    private JsonObject parametrosValidos() {
        return JsonParser.parseString("""
                {
                    "version": 1,
                    "dificultad": "FACIL",
                    "filas": 25,
                    "columnas": 40,
                    "semilla": 42,
                    "cofres": 12,
                    "monstruos": 4,
                    "conSalida": true
                }
                """).getAsJsonObject();
    }

    /**
     * Generador de 20 semillas para cada dificultad.
     */

    static List<Arguments> dificultadesYSemillas() {
        List<Arguments> casos = new ArrayList<>();
        String[] dificultades = {"FACIL", "MEDIO", "DIFICIL"};

        for (String dificultad : dificultades) {
            for (int semilla = 1; semilla <= 20; semilla++) {
                casos.add(Arguments.of(dificultad, semilla));
            }
        }

        return casos;
    }

    @Test
    void rechazaTextoVacio() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar("")
        );
    }

    @Test
    void rechazaJsonQueNoEsObjeto() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar("[]")
        );
    }

    @Test
    void rechazaFaltaDeFilas() {
        JsonObject json = parametrosValidos();
        json.remove("filas");

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void rechazaFilasMenoresACinco() {
        JsonObject json = parametrosValidos();
        json.addProperty("filas", 3);

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void rechazaFilasConParteFraccionaria() {
        JsonObject json = parametrosValidos();
        json.addProperty("filas", 25.5);

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void rechazaCofresNegativos() {
        JsonObject json = parametrosValidos();
        json.addProperty("cofres", -1);

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void rechazaMasCofresQueCeldas() {
        JsonObject json = parametrosValidos();
        json.addProperty("cofres", 1000);

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void rechazaConSalidaComoTexto() {
        JsonObject json = parametrosValidos();
        json.addProperty("conSalida", "true");

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(json.toString())
        );
    }

    @Test
    void creaGrillaConDimensionesIndicadasYTodasParedes() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        char[][] grilla = generador.crearGrilla(5, 7);

        assertEquals(5, grilla.length, "Debe tener 5 filas");

        for (char[] fila : grilla) {
            assertEquals(7, fila.length, "Cada fila debe tener 7 columnas");

            for (char celda : fila) {
                assertEquals('#', celda, "Todas las celdas deben ser paredes");
            }
        }
    }

    @Test
    void excavarAbreElInicioYConservaLosBordes() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();
        char[][] grilla = generador.crearGrilla(15, 20);

        generador.excavar(grilla, new Random(42));

        assertEquals('.', grilla[1][1]);

        for (int columna = 0; columna < grilla[0].length; columna++) {
            assertEquals('#', grilla[0][columna]);
            assertEquals('#', grilla[grilla.length - 1][columna]);
        }

        for (char[] chars : grilla) {
            assertEquals('#', chars[0]);
            assertEquals('#', chars[chars.length - 1]);
        }
    }

    @Test
    void excavarConLaMismaSemillaProduceLaMismaGrilla() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();
        char[][] primera = generador.crearGrilla(15, 20);
        char[][] segunda = generador.crearGrilla(15, 20);

        generador.excavar(primera, new Random(42));
        generador.excavar(segunda, new Random(42));

        for (int fila = 0; fila < primera.length; fila++) {
            assertArrayEquals(
                    primera[fila],
                    segunda[fila],
                    "Las grillas difieren en la fila " + fila
            );
        }
    }

    @Test
    void agregaBifurcacionesSinSuperarElLimiteTransitable() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        char[][] grilla = {
                "#######".toCharArray(),
                "#.....#".toCharArray(),
                "#####.#".toCharArray(),
                "#.....#".toCharArray(),
                "#.#####".toCharArray(),
                "#.....#".toCharArray(),
                "#######".toCharArray()
        };

        boolean pudoAjustar = generador.agregarBifurcaciones(
                grilla, new Random(42)
        );

        assertTrue(pudoAjustar, "Debe poder ajustar este laberinto");

        int transitables = 0;
        int bifurcaciones = 0;

        for (int fila = 1; fila < grilla.length - 1; fila++) {
            for (int columna = 1;
                 columna < grilla[fila].length - 1;
                 columna++) {

                if (grilla[fila][columna] == '#') {
                    continue;
                }

                transitables++;
                int vecinos = 0;

                if (grilla[fila - 1][columna] != '#') {
                    vecinos++;
                }
                if (grilla[fila + 1][columna] != '#') {
                    vecinos++;
                }
                if (grilla[fila][columna - 1] != '#') {
                    vecinos++;
                }
                if (grilla[fila][columna + 1] != '#') {
                    vecinos++;
                }

                if (vecinos >= 3) {
                    bifurcaciones++;
                }
            }
        }

        long totalCeldas = (long) grilla.length * grilla[0].length;

        assertTrue(
                bifurcaciones * 10L >= transitables,
                "Al menos el 10 % debe tener tres o más vecinos"
        );

        assertTrue(
                transitables * 100L <= totalCeldas * 60,
                "No debe superar el 60 % de celdas transitables"
        );
    }

    @Test
    void encuentraLaCeldaMasLejanaPorElCaminoDisponible() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        char[][] grilla = {
                "#######".toCharArray(),
                "#E....#".toCharArray(),
                "#####.#".toCharArray(),
                "#.....#".toCharArray(),
                "#######".toCharArray()
        };

        GeneradorGrupoNN.Celda entrada =
                new GeneradorGrupoNN.Celda(1, 1);

        GeneradorGrupoNN.Celda resultado =
                generador.buscarCeldaMasLejana(grilla, entrada);

        assertEquals(
                new GeneradorGrupoNN.Celda(3, 1),
                resultado,
                "Debe encontrar el extremo del corredor"
        );
    }

    @Test
    void colocaMonstruosEnCantidadExactaYLejosDeLaEntrada() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        char[][] grilla = {
                "#######".toCharArray(),
                "#E....#".toCharArray(),
                "#.....#".toCharArray(),
                "#....S#".toCharArray(),
                "#######".toCharArray()
        };

        GeneradorGrupoNN.Celda entrada = new GeneradorGrupoNN.Celda(1, 1);

        generador.colocarMonstruos(grilla, entrada, 4, new Random(42));

        int monstruos = 0;

        for (int fila = 0; fila < grilla.length; fila++) {
            for (int columna = 0; columna < grilla[fila].length; columna++) {

                if (grilla[fila][columna] == 'M') {
                    monstruos++;

                    int distancia = Math.abs(fila - entrada.fila()) + Math.abs(columna - entrada.columna());

                    assertTrue(
                            distancia >= 3,
                            "Monstruo demasiado cerca de la entrada en "
                                    + fila + ", " + columna
                    );

                    assertTrue(
                            fila > 0 && fila < grilla.length - 1
                                    && columna > 0
                                    && columna < grilla[fila].length - 1,
                            "No debe haber monstruos en el borde"
                    );
                }
            }
        }

        assertEquals(4, monstruos);
        assertEquals('E', grilla[1][1]);
        assertEquals('S', grilla[3][5]);
    }

    @Test
    void rechazaMonstruosCuandoNoHayPosicionesPermitidas() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        char[][] grilla = {
                "#####".toCharArray(),
                "#E.##".toCharArray(),
                "#####".toCharArray(),
                "#####".toCharArray(),
                "#####".toCharArray()
        };

        GeneradorGrupoNN.Celda entrada = new GeneradorGrupoNN.Celda(1, 1);

        assertThrows(
                IllegalStateException.class,
                () -> generador.colocarMonstruos(
                        grilla, entrada, 1, new Random(42)
                )
        );

        assertEquals('.', grilla[1][2]);
        assertEquals('E', grilla[1][1]);
    }

    @Test
    void colocaCofresSinReemplazarCeldasOcupadas() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        char[][] grilla = {
                "#######".toCharArray(),
                "#E.M..#".toCharArray(),
                "#.#...#".toCharArray(),
                "#....S#".toCharArray(),
                "#######".toCharArray()
        };

        char[][] original = new char[grilla.length][];

        for (int fila = 0; fila < grilla.length; fila++) {
            original[fila] = grilla[fila].clone();
        }

        generador.colocarCofres(grilla, 4, new Random(42));

        int cofres = 0;

        for (int fila = 0; fila < grilla.length; fila++) {
            for (int columna = 0; columna < grilla[fila].length; columna++) {

                if (grilla[fila][columna] == 'C') {
                    cofres++;

                    assertEquals(
                            '.',
                            original[fila][columna],
                            "Cada cofre debe ocupar un piso que estaba libre"
                    );
                }

                if (original[fila][columna] != '.') {
                    assertEquals(
                            original[fila][columna],
                            grilla[fila][columna],
                            "No debe modificar celdas ocupadas ni paredes"
                    );
                }
            }
        }

        assertEquals(4, cofres);
        assertEquals('E', grilla[1][1]);
        assertEquals('M', grilla[1][3]);
        assertEquals('S', grilla[3][5]);
    }

    @Test
    void rechazaCofresSinEspacioSinModificarLaGrilla() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        char[][] grilla = {
                "#####".toCharArray(),
                "#E.M#".toCharArray(),
                "###S#".toCharArray(),
                "#####".toCharArray(),
                "#####".toCharArray()
        };

        char[][] original = new char[grilla.length][];

        for (int fila = 0; fila < grilla.length; fila++) {
            original[fila] = grilla[fila].clone();
        }

        assertThrows(
                IllegalStateException.class,
                () -> generador.colocarCofres(
                        grilla, 2, new Random(42)
                )
        );

        for (int fila = 0; fila < grilla.length; fila++) {
            assertArrayEquals(original[fila], grilla[fila]);
        }

        assertEquals('E', grilla[1][1]);
        assertEquals('.', grilla[1][2]);
        assertEquals('M', grilla[1][3]);
        assertEquals('S', grilla[2][3]);
    }

    @ParameterizedTest
    @CsvSource({"FACIL, 1, 0", "MEDIO, 2, 1", "DIFICIL, 2, 2"})
    void colocaTerrenosSegunDificultadSinReemplazarOcupados(String dificultad, int aguaEsperada, int lavaEsperada) {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        char[][] grilla = {
                "#######".toCharArray(),
                "#E....#".toCharArray(),
                "#..M..#".toCharArray(),
                "#..C..#".toCharArray(),
                "#....S#".toCharArray(),
                "#######".toCharArray()
        };

        char[][] original = new char[grilla.length][];

        for (int fila = 0; fila < grilla.length; fila++) {
            original[fila] = grilla[fila].clone();
        }

        generador.colocarAguaYLava(grilla, dificultad, new Random(42));

        int agua = 0;
        int lava = 0;
        int transitables = 0;

        for (int fila = 0; fila < grilla.length; fila++) {
            for (int columna = 0; columna < grilla[fila].length; columna++) {

                char celda = grilla[fila][columna];

                if (celda != '#') {
                    transitables++;
                }

                if (celda == '~') {
                    agua++;
                }

                if (celda == '^') {
                    lava++;
                }

                if (celda == '~' || celda == '^') {
                    assertEquals(
                            '.',
                            original[fila][columna],
                            "El terreno especial debe ocupar un piso libre"
                    );
                }

                if (original[fila][columna] != '.') {
                    assertEquals(
                            original[fila][columna],
                            celda,
                            "Debe conservar paredes, entrada, salida y entidades"
                    );
                }
            }
        }

        assertEquals(aguaEsperada, agua);
        assertEquals(lavaEsperada, lava);
        assertEquals(20, transitables);

        assertTrue(
                (agua + lava) * 100L <= transitables * 25L,
                "Agua y lava juntas no deben superar el 25 %"
        );
    }

    @Test
    void noColocaTerrenosSiNoHayPisosLibres() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        char[][] grilla = {
                "#######".toCharArray(),
                "#EMCCC#".toCharArray(),
                "#CCCCC#".toCharArray(),
                "#CCCCC#".toCharArray(),
                "#CCCCS#".toCharArray(),
                "#######".toCharArray()
        };

        char[][] original = new char[grilla.length][];

        for (int fila = 0; fila < grilla.length; fila++) {
            original[fila] = grilla[fila].clone();
        }

        generador.colocarAguaYLava(grilla, "DIFICIL", new Random(42));

        for (int fila = 0; fila < grilla.length; fila++) {
            assertArrayEquals(original[fila], grilla[fila]);
        }
    }

    @Test
    void generaJsonConDimensionesEntidadesYCoordenadasCorrectas() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();
        JsonObject parametros = parametrosValidos();

        String resultado = generador.generar(parametros.toString());
        JsonObject json = JsonParser.parseString(resultado).getAsJsonObject();

        assertEquals(1, json.get("version").getAsInt());
        assertEquals("JAVALOVERS", json.get("generador").getAsString());
        assertEquals(parametros, json.getAsJsonObject("parametros"));

        JsonArray plano = json.getAsJsonArray("plano");
        assertEquals(25, plano.size());

        int entradas = 0;
        int salidas = 0;
        int cofres = 0;
        int monstruos = 0;

        for (JsonElement elemento : plano) {
            String fila = elemento.getAsString();
            assertEquals(40, fila.length());

            for (char simbolo : fila.toCharArray()) {
                assertTrue(
                        "#.~^CMES".indexOf(simbolo) >= 0,
                        "Símbolo desconocido: " + simbolo
                );

                switch (simbolo) {
                    case 'E' -> entradas++;
                    case 'S' -> salidas++;
                    case 'C' -> cofres++;
                    case 'M' -> monstruos++;
                    default -> {
                    }
                }
            }
        }

        assertEquals(1, entradas);
        assertEquals(1, salidas);
        assertEquals(12, cofres);
        assertEquals(4, monstruos);

        JsonObject entrada = json.getAsJsonObject("entrada");
        int filaEntrada = entrada.get("fila").getAsInt() - 1;
        int columnaEntrada = entrada.get("columna").getAsInt() - 1;

        assertEquals(
                'E',
                plano.get(filaEntrada).getAsString().charAt(columnaEntrada)
        );

        JsonObject salida = json.getAsJsonObject("salida");
        int filaSalida = salida.get("fila").getAsInt() - 1;
        int columnaSalida = salida.get("columna").getAsInt() - 1;

        assertEquals(
                'S',
                plano.get(filaSalida).getAsString().charAt(columnaSalida)
        );
    }

    @Test
    void generarConLosMismosParametrosProduceElMismoResultado() {
        String parametros = parametrosValidos().toString();

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        String primero = generador.generar(parametros);
        String segundo = generador.generar(parametros);
        String otraInstancia = new GeneradorGrupoNN().generar(parametros);

        assertEquals(primero, segundo);
        assertEquals(primero, otraInstancia);
    }

    @Test
    void generaPlanoSinSalidaCuandoNoSeSolicita() {
        JsonObject parametros = parametrosValidos();
        parametros.addProperty("conSalida", false);

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        JsonObject resultado = JsonParser.parseString(
                generador.generar(parametros.toString())
        ).getAsJsonObject();

        assertTrue(resultado.has("salida"));
        assertTrue(resultado.get("salida").isJsonNull());

        for (JsonElement fila : resultado.getAsJsonArray("plano")) {
            assertEquals(-1, fila.getAsString().indexOf('S'), "No debe haber una salida en la grilla");
        }
    }

    @ParameterizedTest(name = "{0}, semilla {1}")
    @MethodSource("dificultadesYSemillas")
    void generaPlanoConDimensionesYCantidadesSolicitadas(String dificultad, int semilla) {
        boolean esFacil = dificultad.equals("FACIL");
        int filas = esFacil ? 25 : 15;
        int columnas = esFacil ? 40 : 20;
        int cofres = esFacil ? 12 : 2;
        int monstruos = esFacil ? 4 : 2;

        JsonObject parametros = parametrosValidos();
        parametros.addProperty("dificultad", dificultad);
        parametros.addProperty("semilla", semilla);
        parametros.addProperty("filas", filas);
        parametros.addProperty("columnas", columnas);
        parametros.addProperty("cofres", cofres);
        parametros.addProperty("monstruos", monstruos);

        String resultado = new GeneradorGrupoNN().generar(parametros.toString());
        JsonArray plano = JsonParser.parseString(resultado)
                .getAsJsonObject()
                .getAsJsonArray("plano");

        assertEquals(filas, plano.size(), "Cantidad de filas incorrecta");

        int cofresEncontrados = 0;
        int monstruosEncontrados = 0;

        for (JsonElement elemento : plano) {
            String fila = elemento.getAsString();
            assertEquals(columnas, fila.length(), "Largo de fila incorrecto");

            for (char celda : fila.toCharArray()) {
                if (celda == 'C') {
                    cofresEncontrados++;
                } else if (celda == 'M') {
                    monstruosEncontrados++;
                }
            }
        }

        assertEquals(cofres, cofresEncontrados, "Cantidad de cofres incorrecta");
        assertEquals(monstruos, monstruosEncontrados, "Cantidad de monstruos incorrecta");
    }

    @Test
    void dosSemillasDistintasPuedenGenerarPlanosDistintos() {
        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        JsonObject parametros = parametrosValidos();
        parametros.addProperty("semilla", 1);
        String resultadoUno = generador.generar(parametros.toString());

        parametros.addProperty("semilla", 2);
        String resultadoDos = generador.generar(parametros.toString());

        JsonArray planoUno = JsonParser.parseString(resultadoUno)
                .getAsJsonObject().getAsJsonArray("plano");
        JsonArray planoDos = JsonParser.parseString(resultadoDos)
                .getAsJsonObject().getAsJsonArray("plano");

        assertNotEquals(planoUno, planoDos);
    }

    @Test
    void generaPlanoMinimoConMonstruoAunqueLaPrimeraEntradaNoSirva() {
        JsonObject parametros = parametrosValidos();
        parametros.addProperty("filas", 5);
        parametros.addProperty("columnas", 5);
        parametros.addProperty("semilla", 1);
        parametros.addProperty("cofres", 0);
        parametros.addProperty("monstruos", 1);
        parametros.addProperty("conSalida", false);

        String resultado = new GeneradorGrupoNN().generar(parametros.toString());
        JsonObject json = JsonParser.parseString(resultado).getAsJsonObject();

        // El JSON usa coordenadas desde 1; las filas del plano, desde 0.
        JsonObject entrada = json.getAsJsonObject("entrada");
        int filaEntrada = entrada.get("fila").getAsInt() - 1;
        int columnaEntrada = entrada.get("columna").getAsInt() - 1;

        JsonArray plano = json.getAsJsonArray("plano");
        int monstruos = 0;

        for (int fila = 0; fila < plano.size(); fila++) {
            String celdas = plano.get(fila).getAsString();

            for (int columna = 0; columna < celdas.length(); columna++) {
                if (celdas.charAt(columna) == 'M') {
                    monstruos++;
                    int distancia = Math.abs(fila - filaEntrada)
                            + Math.abs(columna - columnaEntrada);
                    assertTrue(distancia >= 3);
                }
            }
        }

        assertEquals(1, monstruos);
    }

    @Test
    void reintentaCuandoLaPrimeraGrillaNoTieneEspacioParaLosCofres() {
        JsonObject parametros = parametrosValidos();
        parametros.addProperty("filas", 5);
        parametros.addProperty("columnas", 5);
        parametros.addProperty("semilla", 3);
        parametros.addProperty("cofres", 8);
        parametros.addProperty("monstruos", 0);
        parametros.addProperty("conSalida", false);

        String resultado = new GeneradorGrupoNN().generar(parametros.toString());
        JsonArray plano = JsonParser.parseString(resultado)
                .getAsJsonObject()
                .getAsJsonArray("plano");

        int cofres = 0;
        for (JsonElement fila : plano) {
            for (char celda : fila.getAsString().toCharArray()) {
                if (celda == 'C') {
                    cofres++;
                }
            }
        }

        assertEquals(8, cofres);
    }

    @Test
    void rechazaEntidadesQueSuperanElMaximoTransitable() {
        JsonObject parametros = parametrosValidos();
        parametros.addProperty("filas", 10);
        parametros.addProperty("columnas", 10);
        parametros.addProperty("cofres", 59);
        parametros.addProperty("monstruos", 0);
        parametros.addProperty("conSalida", true);

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(
                IllegalArgumentException.class,
                () -> generador.generar(parametros.toString())
        );
    }

    @Test
    void rechazaJsonConClaveEntreComillasSimples() {
        String json = parametrosValidos().toString()
                .replace("\"filas\"", "'filas'");

        GeneradorGrupoNN generador = new GeneradorGrupoNN();

        assertThrows(IllegalArgumentException.class, () -> generador.generar(json));
    }

    @ParameterizedTest(name = "{0}, semilla {1}")
    @MethodSource("dificultadesYSemillas")
    void generaPlanosAceptadosPorElValidador(
            String dificultad,
            int semilla
    ) {
        boolean esFacil = dificultad.equals("FACIL");

        JsonObject parametros = parametrosValidos();
        parametros.addProperty("dificultad", dificultad);
        parametros.addProperty("semilla", semilla);
        parametros.addProperty("filas", esFacil ? 25 : 15);
        parametros.addProperty("columnas", esFacil ? 40 : 20);
        parametros.addProperty("cofres", esFacil ? 12 : 2);
        parametros.addProperty("monstruos", esFacil ? 4 : 2);

        GeneradorGrupoNN generador = new GeneradorGrupoNN();
        String resultado = generador.generar(parametros.toString());

        ValidadorPlano validador = new ValidadorPlano();

        assertDoesNotThrow(
                () -> validador.validar(resultado),
                "El validador rechazó el plano de "
                        + dificultad + " con semilla " + semilla
        );
    }

}