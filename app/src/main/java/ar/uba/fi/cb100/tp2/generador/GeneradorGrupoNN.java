package ar.uba.fi.cb100.tp2.generador;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Random;

/**
 * Genera laberintos mediante backtracking con una pila explícita.
 */
public class GeneradorGrupoNN implements GeneradorDeLaberinto {

    @Override
    public String generar(String parametrosJson) {
        JsonObject json = leerObjetoJson(parametrosJson);
        Parametros parametros = leerParametros(json);

        Random azar = new Random(parametros.semilla());
        char[][] grilla = construirGrilla(parametros, azar);

        Celda entrada = elegirEntrada(grilla, azar);
        grilla[entrada.fila()][entrada.columna()] = 'E';

        throw new UnsupportedOperationException(
                "Entrada ubicada; falta completar el plano y generar el JSON"
        );
    }
    /**
     * Coordenadas internas, con índices desde cero.
     */
    private record Celda(int fila, int columna) {
    }
    /**
     * Parámetros del contrato de generación.
     */
    private record Parametros(
            int version,
            String dificultad,
            int filas,
            int columnas,
            long semilla,
            int cofres,
            int monstruos,
            boolean conSalida
    ) {
    }

    /**
     * <hr><hr>
     * <h1>Etapa 1: Manejo de JSON y lectura de parametros iniciales.</h1>
     * <hr><hr>
     * Lee y valida los parámetros recibidos en formato JSON antes de crear
     * el objeto Parametros. Comprueba la estructura, los campos obligatorios,
     * sus tipos y las restricciones necesarias para generar el mapa.
     *
     * <hr>
     * <ul>
     *   <li>{@code leerObjetoJson} → Convierte el texto en un objeto JSON
     *       y rechaza entradas vacías, mal formadas o de otro tipo.</li>
     *   <li>{@code obtenerCampo} → Obtiene un campo obligatorio y verifica
     *       que exista y no sea nulo.</li>
     *   <li>{@code leerParametros} → Valida la versión, dificultad, dimensiones,
     *       cantidades y espacio disponible; luego crea los parámetros.</li>
     *   <li>{@code leerEntero} → Lee un número entero dentro del rango de int.</li>
     *   <li>{@code leerLong} → Lee un número entero dentro del rango de long.</li>
     *   <li>{@code leerTexto} → Lee un campo y comprueba que sea texto.</li>
     *   <li>{@code leerBooleano} → Lee un campo y comprueba que sea booleano.</li>
     * </ul>
     * <hr>
     * Si alguna validación falla, se lanza {@code IllegalArgumentException}
     * con un mensaje que describe el problema.
     */
    private JsonObject leerObjetoJson(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "Los parámetros JSON no pueden estar vacíos"
            );
        }

        try {
            JsonElement elemento = JsonParser.parseString(texto);

            if (!elemento.isJsonObject()) {
                throw new IllegalArgumentException(
                        "Los parámetros deben ser un objeto JSON"
                );
            }

            return elemento.getAsJsonObject();
        } catch (JsonParseException e) {
            throw new IllegalArgumentException(
                    "El JSON de parámetros es inválido", e
            );
        }
    }

    private JsonElement obtenerCampo(JsonObject json, String nombre) {
        if (!json.has(nombre) || json.get(nombre).isJsonNull()) {
            throw new IllegalArgumentException(
                    "Falta el parámetro obligatorio: " + nombre
            );
        }

        return json.get(nombre);
    }

    private Parametros leerParametros(JsonObject json) {
        int version = leerEntero(json, "version");
        String dificultad = leerTexto(json, "dificultad");
        int filas = leerEntero(json, "filas");
        int columnas = leerEntero(json, "columnas");
        long semilla = leerLong(json, "semilla");
        int cofres = leerEntero(json, "cofres");
        int monstruos = leerEntero(json, "monstruos");
        boolean conSalida = leerBooleano(json, "conSalida");

        if (version != 1) {
            throw new IllegalArgumentException(
                    "La versión del formato debe ser 1"
            );
        }

        if (!dificultad.equals("FACIL")
                && !dificultad.equals("MEDIO")
                && !dificultad.equals("DIFICIL")) {
            throw new IllegalArgumentException(
                    "La dificultad debe ser FACIL, MEDIO o DIFICIL"
            );
        }

        if (filas < 5 || columnas < 5) {
            throw new IllegalArgumentException(
                    "Las filas y columnas deben ser al menos 5"
            );
        }

        if (cofres < 0 || monstruos < 0) {
            throw new IllegalArgumentException(
                    "Las cantidades de cofres y monstruos no pueden ser negativas"
            );
        }

        long celdasInteriores = (long) (filas - 2) * (columnas - 2);
        int posicionesReservadas = conSalida ? 2 : 1;
        long entidadesSolicitadas = (long) cofres + monstruos;

        if (entidadesSolicitadas > celdasInteriores - posicionesReservadas) {
            throw new IllegalArgumentException(
                    "No hay espacio interior suficiente para cofres y monstruos"
            );
        }

        return new Parametros(
                version,
                dificultad,
                filas,
                columnas,
                semilla,
                cofres,
                monstruos,
                conSalida
        );
    }

    private int leerEntero(JsonObject json, String nombre) {
        JsonElement campo = obtenerCampo(json, nombre);

        if (!campo.isJsonPrimitive()
                || !campo.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un número entero"
            );
        }

        try {
            return campo.getAsBigDecimal().intValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un entero válido",
                    e
            );
        }
    }

    private long leerLong(JsonObject json, String nombre) {
        JsonElement campo = obtenerCampo(json, nombre);

        if (!campo.isJsonPrimitive()
                || !campo.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un número entero"
            );
        }

        try {
            return campo.getAsBigDecimal().longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un long válido",
                    e
            );
        }
    }

    private String leerTexto(JsonObject json, String nombre) {
        JsonElement campo = obtenerCampo(json, nombre);

        if (!campo.isJsonPrimitive()
                || !campo.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser texto"
            );
        }

        return campo.getAsString();
    }

    private boolean leerBooleano(JsonObject json, String nombre) {
        JsonElement campo = obtenerCampo(json, nombre);

        if (!campo.isJsonPrimitive()
                || !campo.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombre + " debe ser un booleano"
            );
        }

        return campo.getAsBoolean();
    }

    /**
     * <hr><hr>
     * <h1>Etapa 2: Construcción de la geometría del laberinto.</h1>
     * <hr><hr>
     * Genera la estructura del mapa a partir de una grilla de paredes.
     * Excava caminos conectados, agrega bifurcaciones y comprueba que
     * la proporción de celdas transitables cumpla los límites establecidos.
     *
     * <hr>
     * <ul>
     *   <li>{@code crearGrilla} → Crea una grilla con todas sus celdas
     *       inicializadas como paredes.</li>
     *   <li>{@code buscarVecinosSinExcavar} → Busca paredes a dos celdas
     *       de distancia en las cuatro direcciones, sin incluir el borde.</li>
     *   <li>{@code excavar} → Abre caminos conectados desde (1, 1),
     *       eligiendo destinos al azar y usando backtracking con una pila.</li>
     *   <li>{@code contarVecinosTransitables} → Cuenta los vecinos inmediatos
     *       que no son paredes, sin considerar diagonales.</li>
     *   <li>{@code tieneBifurcacionesSuficientes} → Verifica que al menos
     *       el 10 % de las celdas transitables interiores tenga tres o más
     *       vecinos transitables.</li>
     *   <li>{@code contarTransitables} → Cuenta todas las celdas
     *       de la grilla que no son paredes.</li>
     *   <li>{@code agregarBifurcaciones} → Abre paredes interiores al azar
     *       para alcanzar el mínimo de bifurcaciones, sin superar el 60 %
     *       de celdas transitables. Indica si logró el objetivo.</li>
     *   <li>{@code tieneProporcionValida} → Comprueba que entre el 30 %
     *       y el 60 % del total de celdas sea transitable.</li>
     *   <li>{@code construirGrilla} → Coordina la generación y validación
     *       del mapa, con un máximo de 100 intentos.</li>
     * </ul>
     * <hr>
     * Si no se consigue una grilla válida tras los 100 intentos,
     * se lanza {@code IllegalStateException}.
     */
    char[][] crearGrilla(int filas, int columnas) {
        char[][] grilla = new char[filas][columnas];

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                grilla[fila][columna] = '#';
            }
        }

        return grilla;
    }

    private List<Celda> buscarVecinosSinExcavar(char[][] grilla, Celda actual) {

        List<Celda> vecinos = new ArrayList<>();

        int[][] desplazamientos = {
                {-2, 0},  // Arriba
                {2, 0},   // Abajo
                {0, -2},  // Izquierda
                {0, 2}    // Derecha
        };

        for (int[] desplazamiento : desplazamientos) {
            int fila = actual.fila() + desplazamiento[0];
            int columna = actual.columna() + desplazamiento[1];

            boolean esInterior =
                    fila > 0
                            && fila < grilla.length - 1
                            && columna > 0
                            && columna < grilla[0].length - 1;

            if (esInterior && grilla[fila][columna] == '#') {
                vecinos.add(new Celda(fila, columna));
            }
        }

        return vecinos;
    }

    void excavar(char[][] grilla, Random azar) {

        ArrayDeque<Celda> pila = new ArrayDeque<>();

        Celda inicio = new Celda(1, 1);
        grilla[inicio.fila()][inicio.columna()] = '.';
        pila.push(inicio);

        while (!pila.isEmpty()) {
            Celda actual = pila.peek();

            List<Celda> vecinos = buscarVecinosSinExcavar(grilla, actual);

            if (vecinos.isEmpty()) {
                pila.pop();
            } else {
                Celda destino = vecinos.get(azar.nextInt(vecinos.size()));

                int filaIntermedia = (actual.fila() + destino.fila()) / 2;
                int columnaIntermedia =
                        (actual.columna() + destino.columna()) / 2;

                grilla[filaIntermedia][columnaIntermedia] = '.';
                grilla[destino.fila()][destino.columna()] = '.';

                pila.push(destino);
            }
        }
    }

    private int contarVecinosTransitables(char[][] grilla, int fila, int columna) {
        int cantidad = 0;

        int[][] desplazamientos = {
                {-1, 0},
                {1, 0},
                {0, -1},
                {0, 1}
        };

        for (int[] desplazamiento : desplazamientos) {
            int filaVecina = fila + desplazamiento[0];
            int columnaVecina = columna + desplazamiento[1];

            boolean estaDentro =
                    filaVecina >= 0
                            && filaVecina < grilla.length
                            && columnaVecina >= 0
                            && columnaVecina < grilla[0].length;

            if (estaDentro && grilla[filaVecina][columnaVecina] != '#') {
                cantidad++;
            }
        }

        return cantidad;
    }

    private boolean tieneBifurcacionesSuficientes(char[][] grilla) {
        int transitables = 0;
        int bifurcaciones = 0;

        for (int fila = 1; fila < grilla.length - 1; fila++) {
            for (int columna = 1; columna < grilla[fila].length - 1; columna++) {
                if (grilla[fila][columna] != '#') {
                    transitables++;

                    if (contarVecinosTransitables(grilla, fila, columna) >= 3) {
                        bifurcaciones++;
                    }
                }
            }
        }

        return transitables > 0 && bifurcaciones * 10L >= transitables;
    }

    private int contarTransitables(char[][] grilla) {
        int cantidad = 0;

        for (char[] fila : grilla) {
            for (char celda : fila) {
                if (celda != '#') {
                    cantidad++;
                }
            }
        }

        return cantidad;
    }

    boolean agregarBifurcaciones(char[][] grilla, Random azar) {
        int transitables = contarTransitables(grilla);
        long totalCeldas = (long) grilla.length * grilla[0].length;

        while (!tieneBifurcacionesSuficientes(grilla)) {
            // Abrir una pared suma una celda transitable.
            if ((transitables + 1L) * 100 > totalCeldas * 60) {
                return false;
            }

            List<Celda> candidatas = new ArrayList<>();

            for (int fila = 1; fila < grilla.length - 1; fila++) {
                for (int columna = 1;
                     columna < grilla[fila].length - 1;
                     columna++) {

                    if (grilla[fila][columna] == '#'
                            && contarVecinosTransitables(
                            grilla, fila, columna
                    ) >= 2) {
                        candidatas.add(new Celda(fila, columna));
                    }
                }
            }

            if (candidatas.isEmpty()) {
                return false;
            }

            Celda elegida = candidatas.get(
                    azar.nextInt(candidatas.size())
            );

            grilla[elegida.fila()][elegida.columna()] = '.';
            transitables++;
        }

        return true;
    }

    private boolean tieneProporcionValida(char[][] grilla) {
        long transitables = contarTransitables(grilla);
        long totalCeldas = (long) grilla.length * grilla[0].length;

        return transitables * 100 >= totalCeldas * 30
                && transitables * 100 <= totalCeldas * 60;
    }

    private char[][] construirGrilla(Parametros parametros, Random azar) {
        final int maxIntentos = 100;

        for (int intento = 0; intento < maxIntentos; intento++) {
            char[][] grilla = crearGrilla(
                    parametros.filas(),
                    parametros.columnas()
            );

            excavar(grilla, azar);

            boolean pudoAgregarBifurcaciones =
                    agregarBifurcaciones(grilla, azar);

            if (pudoAgregarBifurcaciones && tieneProporcionValida(grilla)) {
                return grilla;
            }
        }

        throw new IllegalStateException(
                "No se pudo construir una grilla válida tras "
                        + maxIntentos + " intentos"
        );
    }

    /**
     * Elige al azar una celda de piso para la entrada del heroe.
     */
    private Celda elegirEntrada(char[][] grilla, Random azar) {
        List<Celda> pisos = new ArrayList<>();

        for (int fila = 1; fila < grilla.length - 1; fila++) {
            for (int columna = 1;
                 columna < grilla[fila].length - 1;
                 columna++) {

                if (grilla[fila][columna] == '.') {
                    pisos.add(new Celda(fila, columna));
                }
            }
        }

        if (pisos.isEmpty()) {
            throw new IllegalStateException(
                    "No hay celdas de piso para ubicar la entrada"
            );
        }

        int indiceElegido = azar.nextInt(pisos.size());
        return pisos.get(indiceElegido);
    }

}