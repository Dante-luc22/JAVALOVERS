package ar.uba.fi.cb100.tp2.modelo.heroe;

import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.objetos.Inventario;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import java.util.ArrayList;
import ar.uba.fi.cb100.material.ValidacionesUtiles;

public class Heroe {

    private Ubicacion ubicacion;
    private int energia;
    private int cofresAbiertos;
    private final int energiaMaxima;
    private final Inventario inventario;
    private final ArrayList<Efecto> efectosActivos = new ArrayList<>();

    /**
     * Constructor de la clase heroe, recibe su ubicacionInicial y
     * su energiaInicial, los cofres abiertos empiezan en 0 y empieza
     * con un inventario vacio.
     *
     * @param ubicacionInicial ubicacion donde empieza el heroe;
     * @param energiaInicial   energia con la que empieza el heroe;
     * @throws IllegalArgumentException si la energia inicial no es mayor a cero.
     * Y si se recibe una ubicacion nula.
     */
    public Heroe(Ubicacion ubicacionInicial, int energiaInicial) {
        ValidacionesUtiles.esMayorQueCero(energiaInicial, "energiaInicial");
        ValidacionesUtiles.validarDistintoDeNull(ubicacionInicial, "ubicacionInicial");
        this.ubicacion = ubicacionInicial;
        this.energia = energiaInicial;
        this.energiaMaxima = energiaInicial;
        this.cofresAbiertos = 0;
        this.inventario = new Inventario();
    }


    /**
     * Metodo para mover al heroe al destino dado
     *
     * @param destino destino de ubicacion.
     * @throws IllegalArgumentException si la ubicacion es nula
     */
    public void mover(Ubicacion destino) {
        ValidacionesUtiles.validarDistintoDeNull(destino, "destino");

        this.ubicacion = destino;
    }


    /**
     * Metodo para restar la cantidad de energia dada
     *
     * @param cantidad energia a restar.
     * @throws IllegalArgumentException si la cantidad dada es negativa
     */
    public void restarEnergia(int cantidad) {
        ValidacionesUtiles.validarFalso(cantidad < 0,
                "La energia a restar no puede ser negativa, se recibio: " + cantidad);
        this.energia -= cantidad;
        if (energia < 0) {
            this.energia = 0;
        }
    }

    /**
     * Metodo para sumar la cantidad de energia dada.
     *
     * @param cantidad energia a sumar
     * @throws IllegalArgumentException si la cantidad dada es negativa.
     */
    public void sumarEnergia(int cantidad) {
        ValidacionesUtiles.validarFalso(cantidad < 0,
                "La energia a sumar no puede ser negativa, se recibio: " + cantidad);
        this.energia += cantidad;
        if (energia > energiaMaxima) {
            this.energia = energiaMaxima;
        }
    }

    /**
     * Metodo para sumar 1 a la cantidad de cofres abiertos
     */
    public void sumarCofreAbierto(){
        this.cofresAbiertos++;
    }

    /**
     * Metodo para verificar si el heroe esta sin energia
     *
     * @return true si el heroe esta sin energia
     */
    public boolean estaSinEnergia(){
        return this.energia == 0;
    }


    /**
     * Metodo para agregar un efecto al heroe.
     *
     * @param efecto a agregar
     * @throws IllegalArgumentException si el efeto es nulo.
     */
    public void agregarEfecto(Efecto efecto) {
        ValidacionesUtiles.validarDistintoDeNull(efecto, "efecto");
        this.efectosActivos.add(efecto);
    }

    /**
     * Sirve para ajustar el costo del terreno
     *
     * @param terreno      donde se mueve el heroe
     * @param costoBase    del terrero.
     * @param costoDelPiso el costo que le saca el piso al heroe
     * @return el costo ajustado
     */
    public int ajustarCosto(Terreno terreno, int costoBase, int costoDelPiso) {
        int costo = costoBase;
        for (Efecto efecto : this.efectosActivos) {
            costo = efecto.ajustarCosto(terreno, costo, costoDelPiso);
        }
        return costo;
    }

    /**
     * Metodo para indicar que el heroe camino con el efecto
     * activo.
     */
    public void pasarUnPaso() {
        for (Efecto ef : this.efectosActivos) {
            ef.pasarUnPaso();
        }
        this.efectosActivos.removeIf(Efecto::termino);
    }



    // =================== GETTERS SIMPLES =================== //

    /**
     * Getter que devuelve la ubicacion actual del heroe
     */
    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    /**
     * Getter que devuelve la energia actual del heroe
     */
    public int getEnergia() {
        return energia;
    }

    /**
     * Getter que devuelve la cantidad de cofres abiertos
     */
    public int getCofresAbiertos() {
        return cofresAbiertos;
    }

    /**
     * Getter que devuelve la energia maxima del heroe.
     */
    public int getEnergiaMaxima() {
        return energiaMaxima;
    }

    /**
     * Getter que devuelve el inventario del heroe.
     */
    public Inventario getInventario() {
        return inventario;
    }

    /**
     * Getter que devuelve una copia de la lista de
     * efectos activos.
     */
    public ArrayList<Efecto> getEfectosActivos() {
        return new ArrayList<>(this.efectosActivos);
    }
}
