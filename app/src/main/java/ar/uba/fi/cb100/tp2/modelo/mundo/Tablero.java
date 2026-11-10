package ar.uba.fi.cb100.tp2.modelo.mundo;

import java.util.ArrayList;
import java.util.List;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Una grilla de filas x columnas donde cada celda tiene un terreno.
 * Las posiciones van de 1 a n, igual que en el JSON del generador;
 * la resta de 1 para el arreglo se hace solo dentro de esta clase.
 */


public class Tablero {
    
    private final int filas;
    private final int columnas;
    private final Terreno[][] celdas;

     /**
     * Crea un tablero con todas las celdas en pared.
     * @param filas    cantidad de filas, incluido el borde
     * @param columnas cantidad de columnas, incluido el borde
     * @throws IllegalArgumentException si filas o columnas no son positivas
     */

    public Tablero(int filas, int columnas) {
        if (filas < 1 || columnas < 1) {
            throw new IllegalArgumentException("Filas y columnas tienen que ser positivas");
        }
        this.filas = filas;
        this.columnas = columnas;
        this.celdas = new Terreno[filas][columnas];
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                this.celdas[i][j] = Terreno.PARED;
            }
        }
    }

    /**
     * @param posicion de 1 a n
     * @return si la posición existe en el tablero
     */
    public boolean estaDentro(Posicion posicion) {

        return posicion.fila() >= 1 && posicion.fila() <= this.filas
                && posicion.columna() >= 1 && posicion.columna() <= this.columnas;
    }

     /** 
     * @param posicion de 1 a n
     * @return el terreno de esa celda
     * @throws IllegalArgumentException si la posición está fuera del tablero
     */
    public Terreno getTerreno(Posicion posicion) {
        validar(posicion);
        return this.celdas[posicion.fila() - 1][posicion.columna() - 1];
    }

    /**
     * @param posicion de 1 a n
     * @param terreno  el terreno a poner
     * @throws IllegalArgumentException si la posición está fuera del tablero
     */
    public void setTerreno(Posicion posicion, Terreno terreno) {
        validar(posicion);
        this.celdas[posicion.fila() - 1][posicion.columna() - 1] = terreno;
    }

    /**
     * @param posicion de 1 a n
     * @return si se puede pisar: existe y no es pared
     */
    public boolean esTransitable(Posicion posicion) {
        return estaDentro(posicion) && getTerreno(posicion) != Terreno.PARED;
    }

    /**
     * Las vecinas de arriba, abajo, izquierda y derecha que se pueden pisar.
     * La usan el errante, el BFS del validador y Dijkstra.
     * @param posicion de 1 a n
     * @return las vecinas transitables, en el orden de Direccion
     */
    public List<Posicion> vecinasTransitables(Posicion posicion) {
        validar(posicion);
        List<Posicion> vecinas = new ArrayList<>();
        
        for (Direccion direccion : Direccion.values()) {
            Posicion vecina = posicion.mover(direccion);
            if (esTransitable(vecina)) {
                vecinas.add(vecina);
            }
        }
        return vecinas;
    }


  /**
     * @param posicion la posición a verificar
     * @throws IllegalArgumentException si está fuera del tablero
     */
    private void validar(Posicion posicion) {
        if (!estaDentro(posicion)) {
            throw new IllegalArgumentException("Posición fuera del tablero: " + posicion);
        }
    }

        /**
     * @return cantidad de filas, incluido el borde
     */
    public int getFilas() {
        return this.filas;
    }

    /**
     * @return cantidad de columnas, incluido el borde
     */
    public int getColumnas() {
        return this.columnas;
    }

}
