package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.util.ArrayDeque;
import java.util.Queue;
import ar.uba.fi.cb100.material.ValidacionesUtiles;

/**
 * Valida un plano en el formato JSON del Anexo B.3 con las once reglas de B.4.
 */
public class ValidadorPlano {

    private static final String SIMBOLOS_VALIDOS = "#.~^CMES";

    // arriba, abajo, izquierda, derecha
    private static final int[][] MOVIMIENTOS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    // Datos leídos del JSON. Las posiciones quedan en base 0.
    private int filas;
    private int columnas;
    private int cofres;
    private int monstruos;
    private boolean conSalida;
    private Posicion entrada;
    private Posicion salida; // null si el plano no tiene salida
    private char[][] plano;

    /**
     * Si el plano es válido no hace nada. Si alguna regla falla, lanza
     * IllegalArgumentException con un mensaje que empieza por la regla
     * (por ejemplo "R5: ...").
     */
    public void validar(String json) {
        leer(json);
        validarDimensiones();
        validarSimbolos();
        validarBorde();
        validarEntradaYSalida();

        int[][] distancias = calcularDistancias();
        validarConexo(distancias);
        validarBifurcaciones();
        validarProporcion();
        validarCantidades();
        validarSalidaMasLejana(distancias);
        validarAguaYLava();
        validarMonstruosLejosDeLaEntrada();
    }

    /**
     * BFS con cola desde la entrada. Devuelve la distancia en pasos hasta cada
     * celda, o -1 si no se llega a ella.
     */
    private int[][] calcularDistancias() {
        int[][] distancias = new int[filas][columnas];
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                distancias[fila][columna] = -1;
            }
        }

        Queue<Posicion> cola = new ArrayDeque<>();
        distancias[entrada.fila()][entrada.columna()] = 0;
        cola.add(entrada);

        while (!cola.isEmpty()) {
            Posicion actual = cola.remove();
            int distanciaActual = distancias[actual.fila()][actual.columna()];

            for (int[] movimiento : MOVIMIENTOS) {
                int fila = actual.fila() + movimiento[0];
                int columna = actual.columna() + movimiento[1];

                if (esTransitable(fila, columna) && distancias[fila][columna] == -1) {
                    distancias[fila][columna] = distanciaActual + 1;
                    cola.add(new Posicion(fila, columna));
                }
            }
        }

        return distancias;
    }

    /**
     * R5: desde la E se llega a toda celda transitable.
     */
    private void validarConexo(int[][] distancias) {
        int alcanzadas = 0;
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                if (distancias[fila][columna] != -1) {
                    alcanzadas++;
                }
            }
        }

        int transitables = contarTransitables();
        ValidacionesUtiles.validarFalso(alcanzadas != transitables,
                "R5: desde la entrada se alcanzan " + alcanzadas
                + " de " + transitables + " celdas transitables");
    }

    /**
     * R6: al menos el 10 % de las celdas transitables tienen 3 o más vecinos
     * transitables.
     */
    private void validarBifurcaciones() {
        int conBifurcacion = 0;

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                if (esTransitable(fila, columna) && contarVecinosTransitables(fila, columna) >= 3) {
                    conBifurcacion++;
                }
            }
        }

        int transitables = contarTransitables();
        ValidacionesUtiles.validarFalso(conBifurcacion * 100 < transitables * 10,
                "R6: sólo " + conBifurcacion + " de " + transitables
                + " celdas transitables tienen 3 o más vecinos, y tiene que ser al menos el 10 %");
    }

    /**
     * R9: si hay S, su distancia a la E es la máxima del plano (se admiten empates).
     */
    private void validarSalidaMasLejana(int[][] distancias) {
        if (!conSalida) {
            return;
        }

        int maxima = 0;
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                maxima = Math.max(maxima, distancias[fila][columna]);
            }
        }

        int distanciaDeLaSalida = distancias[salida.fila()][salida.columna()];
        ValidacionesUtiles.validarFalso(distanciaDeLaSalida != maxima,
                "R9: la salida está a distancia " + distanciaDeLaSalida
                + " y la celda más lejana está a " + maxima);
    }

    private boolean esTransitable(int fila, int columna) {
        boolean estaDentro = fila >= 0 && fila < filas && columna >= 0 && columna < columnas;
        return estaDentro && plano[fila][columna] != '#';
    }

    private int contarVecinosTransitables(int fila, int columna) {
        int vecinos = 0;
        for (int[] movimiento : MOVIMIENTOS) {
            if (esTransitable(fila + movimiento[0], columna + movimiento[1])) {
                vecinos++;
            }
        }
        return vecinos;
    }

    /**
     * R1: el plano tiene exactamente filas cadenas de exactamente columnas
     * caracteres, y ambas medidas son al menos 5.
     */
    private void validarDimensiones() {
        ValidacionesUtiles.validarFalso(filas < 5 || columnas < 5,
                "R1: filas y columnas deben ser al menos 5");

        ValidacionesUtiles.validarFalso(plano.length != filas,
                "R1: el plano tiene " + plano.length + " filas y se esperaban " + filas);

        for (int fila = 0; fila < filas; fila++) {
            ValidacionesUtiles.validarFalso(plano[fila].length != columnas,
                    "R1: la fila " + (fila + 1) + " tiene "
                    + plano[fila].length + " columnas y se esperaban " + columnas);
        }
    }

    /**
     * R2: sólo aparecen los ocho símbolos de B.3.
     */
    private void validarSimbolos() {
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                char simbolo = plano[fila][columna];

                ValidacionesUtiles.validarFalso(SIMBOLOS_VALIDOS.indexOf(simbolo) == -1,
                        "R2: símbolo inválido '" + simbolo
                        + "' en (" + (fila + 1) + ", " + (columna + 1) + ")");
            }
        }
    }

    /**
     * R3: la primera y la última fila, y la primera y la última columna, son
     * todas pared.
     */
    private void validarBorde() {
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                boolean esBorde = fila == 0 || fila == filas - 1
                        || columna == 0 || columna == columnas - 1;

                ValidacionesUtiles.validarFalso(esBorde && plano[fila][columna] != '#',
                        "R3: el borde debe ser pared y ("
                        + (fila + 1) + ", " + (columna + 1) + ") no lo es");
            }
        }
    }

    /**
     * R4: exactamente una E; exactamente una S si conSalida, ninguna si no.
     * Los campos entrada y salida coinciden con ellas.
     */
    private void validarEntradaYSalida() {
        int cantidadDeE = contar('E');
        ValidacionesUtiles.validarFalso(cantidadDeE != 1,
                "R4: debe haber exactamente una E y hay " + cantidadDeE);

        int cantidadDeS = contar('S');
        int esperadasDeS = conSalida ? 1 : 0;
        ValidacionesUtiles.validarFalso(cantidadDeS != esperadasDeS,
                "R4: con conSalida " + conSalida + " se esperaban "
                + esperadasDeS + " S y hay " + cantidadDeS);

        ValidacionesUtiles.validarFalso(!hay(entrada, 'E'),
                "R4: el campo entrada no coincide con la E del plano");

        if (conSalida) {
            ValidacionesUtiles.validarFalso(salida == null || !hay(salida, 'S'),
                    "R4: el campo salida no coincide con la S del plano");
        } else {
            ValidacionesUtiles.validarFalso(salida != null,
                    "R4: conSalida es false pero el campo salida no es null");
        }
    }

    /**
     * R7: entre el 30 % y el 60 % de las celdas del plano son transitables.
     */
    private void validarProporcion() {
        int transitables = contarTransitables();
        int total = filas * columnas;

        // transitables / total entre 30 % y 60 %, multiplicando para no usar decimales
        ValidacionesUtiles.validarFalso(transitables * 100 < total * 30 || transitables * 100 > total * 60,
                "R7: hay " + transitables + " celdas transitables de "
                + total + ", y tienen que ser entre el 30 % y el 60 %");
    }

    /**
     * R8: tantas C como cofres y tantas M como monstruos.
     */
    private void validarCantidades() {
        int cantidadDeC = contar('C');
        ValidacionesUtiles.validarFalso(cantidadDeC != cofres,
                "R8: hay " + cantidadDeC + " C y se esperaban " + cofres);

        int cantidadDeM = contar('M');
        ValidacionesUtiles.validarFalso(cantidadDeM != monstruos,
                "R8: hay " + cantidadDeM + " M y se esperaban " + monstruos);
    }

    /**
     * R10: entre agua y lava, como mucho el 25 % de las transitables.
     */
    private void validarAguaYLava() {
        int aguaYLava = contar('~') + contar('^');
        int transitables = contarTransitables();

        ValidacionesUtiles.validarFalso(aguaYLava * 100 > transitables * 25,
                "R10: hay " + aguaYLava + " celdas de agua y lava de "
                + transitables + " transitables, y el máximo es el 25 %");
    }

    /**
     * R11: toda M está a distancia Manhattan mayor o igual a 3 de la E.
     */
    private void validarMonstruosLejosDeLaEntrada() {
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                if (plano[fila][columna] == 'M') {
                    int distancia = Math.abs(fila - entrada.fila()) + Math.abs(columna - entrada.columna());

                    ValidacionesUtiles.validarFalso(distancia < 3,
                            "R11: el monstruo en (" + (fila + 1) + ", "
                            + (columna + 1) + ") está a distancia " + distancia
                            + " de la entrada, y el mínimo es 3");
                }
            }
        }
    }

    /**
     * Cuenta cuántas veces aparece el símbolo en el plano.
     */
    private int contar(char simbolo) {
        int cantidad = 0;

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                if (plano[fila][columna] == simbolo) {
                    cantidad++;
                }
            }
        }

        return cantidad;
    }

    /**
     * Cuenta las celdas transitables: todo lo que no es pared.
     */
    private int contarTransitables() {
        int cantidad = 0;

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                if (plano[fila][columna] != '#') {
                    cantidad++;
                }
            }
        }

        return cantidad;
    }

    /**
     * Indica si en la posición está el símbolo. Si la posición queda fuera
     * del plano, devuelve false.
     */
    private boolean hay(Posicion posicion, char simbolo) {
        int fila = posicion.fila();
        int columna = posicion.columna();
        boolean estaDentro = fila >= 0 && fila < filas && columna >= 0 && columna < columnas;

        return estaDentro && plano[fila][columna] == simbolo;
    }

    /**
     * Lee el JSON y guarda sus datos en los atributos.
     */
    private void leer(String json) {
        try {
            JsonObject raiz = JsonParser.parseString(json).getAsJsonObject();

            JsonObject parametros = obtener(raiz, "parametros").getAsJsonObject();
            filas = obtener(parametros, "filas").getAsInt();
            columnas = obtener(parametros, "columnas").getAsInt();
            cofres = obtener(parametros, "cofres").getAsInt();
            monstruos = obtener(parametros, "monstruos").getAsInt();
            conSalida = obtener(parametros, "conSalida").getAsBoolean();

            entrada = leerPosicion(obtener(raiz, "entrada"));

            JsonElement salidaJson = raiz.get("salida");
            if (salidaJson == null || salidaJson.isJsonNull()) {
                salida = null;
            } else {
                salida = leerPosicion(salidaJson);
            }

            JsonArray filasDelPlano = obtener(raiz, "plano").getAsJsonArray();
            plano = new char[filasDelPlano.size()][];
            for (int fila = 0; fila < filasDelPlano.size(); fila++) {
                plano[fila] = filasDelPlano.get(fila).getAsString().toCharArray();
            }
        } catch (JsonParseException | IllegalStateException | UnsupportedOperationException e) {
            throw new IllegalArgumentException("El JSON del plano tiene un formato inválido", e);
        }
    }

    /**
     * Devuelve el campo pedido, o lanza una excepción si no está.
     */
    private JsonElement obtener(JsonObject objeto, String nombre) {
        JsonElement campo = objeto.get(nombre);
        ValidacionesUtiles.validarFalso(campo == null || campo.isJsonNull(),
                "Falta el campo: " + nombre);
        return campo;
    }

    /**
     * Convierte {"fila": f, "columna": c} (base 1) en una Posicion (base 0).
     */
    private Posicion leerPosicion(JsonElement elemento) {
        JsonObject coordenadas = elemento.getAsJsonObject();
        int fila = obtener(coordenadas, "fila").getAsInt();
        int columna = obtener(coordenadas, "columna").getAsInt();
        return new Posicion(fila - 1, columna - 1);
    }
}
