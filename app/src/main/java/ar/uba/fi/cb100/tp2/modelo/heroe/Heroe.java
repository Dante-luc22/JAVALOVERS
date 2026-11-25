package ar.uba.fi.cb100.tp2.modelo.heroe;

import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.objetos.Inventario;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

import java.util.ArrayList;
import java.util.List;

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
        this.energiaMaxima = energiaInicial;
        this.cofresAbiertos = 0;
        this.inventario = new Inventario();
    }


    /**
     * Constructor para reconstruir un héroe desde una partida guardada.
     *
     * A diferencia del constructor inicial, conserva todos los valores
     * que tenía el héroe al momento de guardar la partida.
     *
     * @param ubicacion ubicación actual del héroe
     * @param energia energía disponible
     * @param energiaMaxima energía máxima
     * @param cofresAbiertos cantidad de cofres abiertos
     * @param inventario inventario previamente reconstruido
     * @param efectosActivos efectos que todavía están vigentes
     */
    public Heroe(
            Ubicacion ubicacion,
            int energia,
            int energiaMaxima,
            int cofresAbiertos,
            Inventario inventario,
            List<Efecto> efectosActivos) {

        if (ubicacion == null) {
            throw new IllegalArgumentException(
                    "La ubicación del héroe no puede ser nula.");
        }

        if (energiaMaxima <= 0) {
            throw new IllegalArgumentException(
                    "La energía máxima debe ser mayor que cero.");
        }

        if (energia < 0 || energia > energiaMaxima) {
            throw new IllegalArgumentException(
                    "La energía actual debe estar entre cero y la energía máxima.");
        }

        if (cofresAbiertos < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de cofres abiertos no puede ser negativa.");
        }

        if (inventario == null) {
            throw new IllegalArgumentException(
                    "El inventario no puede ser nulo.");
        }

        if (efectosActivos == null) {
            throw new IllegalArgumentException(
                    "La lista de efectos activos no puede ser nula.");
        }

        for (Efecto efecto : efectosActivos) {
            if (efecto == null) {
                throw new IllegalArgumentException(
                        "La lista de efectos no puede contener valores nulos."
                );
            }
        }

        this.ubicacion = ubicacion;
        this.energia = energia;
        this.energiaMaxima = energiaMaxima;
        this.cofresAbiertos = cofresAbiertos;
        this.inventario = inventario;

        // Se conserva la lista interna, sin compartir la colección recibida.
        this.efectosActivos.addAll(efectosActivos);
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
     * Metodo para sumar la cantidad de energia dada.
     *
     * @param cantidad energia a sumar
     * @throws IllegalArgumentException si la cantidad dada es negativa.
     */
    public void sumarEnergia(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException(
                    "La energia a sumar no puede ser negativa, se recibio: " + cantidad);
        }
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
        if  (efecto == null) {
            throw new IllegalArgumentException("El efecto no puede ser nulo.");
        }
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
