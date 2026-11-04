package ar.uba.fi.cb100.tp2.modelo.partida;

public class Configuracion {
    private int ancho;
    private int alto;
    private int pasos;
    private String directorioSalida;

    //-----CONSTRUCTOR-----
    public Configuracion() {
    }
    public Configuracion(int ancho, int alto, int pasos, String directorioSalida) {
        this.ancho = ancho;
        this.alto = alto;
        this.pasos = pasos;
        this.directorioSalida = directorioSalida;
    }
    //-----GETTERS SIMPLES-----
    public int getAncho() {
        return ancho;}

    public int getAlto() {
        return alto;}

    public int getPasos() {
        return pasos;}

    public String getDirectorioSalida() {
        return directorioSalida;}
}