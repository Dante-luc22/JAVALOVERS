package ar.uba.fi.cb100.tp2.modelo.heroe;

import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

public class Heroe {

    private Ubicacion ubicacion;
    private int energia;
    private int cofresAbiertos;

    /**
     * Constructor de la clase heroe, recibe su ubicacionInicial y
     * su energiaInicial, los cofres abiertos empiezan en 0;
     *
     * @param ubicacionInicial ubicacion donde empieza el heroe;
     * @param energiaInicial   energia con la que empieza el heroe;
     * @throws IllegalArgumentException si la energia inicial no es mayor a cero.
     * Y si se recibe una ubicacion nula.
     */
    public Heroe(Ubicacion ubicacionInicial, int energiaInicial) {
        if (energiaInicial <= 0) {
            throw new IllegalArgumentException(
                    "La energia inicial debe ser mayor a cero, se recibio: " + energiaInicial);
        }
        if (ubicacionInicial == null) {
            throw new IllegalArgumentException(
                    "La ubicacion del personaje no puede ser nula.");
        }
        this.ubicacion = ubicacionInicial;
        this.energia = energiaInicial;
        this.cofresAbiertos = 0;
    }


    /**
     * Metodo para mover al heroe al destino dado
     *
     * @param destino destino de ubicacion.
     * @throws IllegalArgumentException si la ubicacion es nula
     */
    public void mover(Ubicacion destino) {
        if (destino == null) {
            throw new IllegalArgumentException(
                    "La ubicacion donde mover el personaje no puede ser nula.");
        }

        this.ubicacion = destino;
    }


    /**
     * Metodo para restar la cantidad de energia dada
     *
     * @param cantidad energia a restar.
     * @throws IllegalArgumentException si la cantidad dada es negativa
     */
    public void restarEnergia(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException(
                    "La energia a restar no puede ser negativa, se recibio: " + cantidad);
        }
        this.energia -= cantidad;
        if (energia < 0) {
            this.energia = 0;
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
}
