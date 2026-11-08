package ar.uba.fi.cb100.tp2.modelo.partida;

/**
 * Lo que configuracion.json dice de una dificultad: cuántos tableros y pisos
 * tiene el mundo, el tamaño de cada plano y cuántos cofres y monstruos hay.
 * Los nombres coinciden con las claves del JSON para que Gson lo lea directo.
 */

public record ParametrosDeDificultad(int tableros, int pisos, int filas, int columnas,
                                     int cofres, int monstruos) {
    
}
