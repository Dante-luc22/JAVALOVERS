package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Clase que guarda una fabrica por cada tipo de monstruo y crea uno al azar.
 * Agregar un tipo al juego es un registrar mas.
 */
public class CatalogoDeMonstruos{

    private final LinkedHashMap<String, FabricaDeMonstruo> fabricasDeMonstruos =  new LinkedHashMap<>();

    /**
     * Constructor vacio
     */
    public CatalogoDeMonstruos(){
    }

    /**
     * Metodo para registrar un monstruo en el catalogo;
     *
     * @param nombre  nombre del tipo de monstruo;
     * @param fabrica la que sabe crear ese tipo
     * @throws IllegalArgumentException si el nombre es nulo o vacio;
     *                                  si la fabrica es nula;
     *                                  si el nombre ya esta registrado;
     */
    public void registrar(String nombre, FabricaDeMonstruo fabrica){
        if (nombre == null || nombre.isBlank()){
            throw new IllegalArgumentException(
                    "El nombre no puede ser nulo ni un campo vacio.");
        }
        if (fabrica == null){
            throw new IllegalArgumentException(
                    "La fabrica dada no puede ser nula.");
        }
        if (estaRegistrado(nombre)){
            throw new IllegalArgumentException(
                    "El nombre ya se encuentra registrado.");
        }
        fabricasDeMonstruos.put(nombre, fabrica);
    }

    /**
     * Metodo que crea un monstruo al azar en una posicion;
     *
     * @param p    posicion donde se crea.
     * @param azar utilizado para un indice al azar.
     * @return devuelve el monstruo creado.
     * @throws IllegalArgumentException si la posicion es nula;
     *                                  si random es nulo;
     * @throws IllegalStateException si el catalogo esta vacio;
     */
    public Monstruo crearAlAzar(Posicion p, Random azar){
        if (p == null){
            throw new IllegalArgumentException(
                    "La posicion no puede ser nula.");
        }
        if (azar == null){
            throw new IllegalArgumentException(
                    "El azar no puede ser nulo.");
        }
        if (fabricasDeMonstruos.isEmpty()){
            throw new IllegalStateException(
                    "El catálogo no tiene tipos de monstruo registrados.");
        }
        ArrayList<FabricaDeMonstruo> fabricas = new ArrayList<>(fabricasDeMonstruos.values());
        int indice = azar.nextInt(fabricas.size());
        FabricaDeMonstruo fabricaElegida = fabricas.get(indice);
        return fabricaElegida.crear(p);
    }

    /**
     * Devuelve la cantidad de tipos de monstruo que hay.
     *
     * @return cantidad de tipos
     */
    public int cantidadDeTipos() {
        return fabricasDeMonstruos.size();
    }

    /**
     * Metodo para decir si un tipo de monstruo ya esta registrado.
     *
     * @param nombre nombre a buscar.
     * @return True si ya esta registrado.
     * @throws IllegalArgumentException si el nombre es nulo o vacio;
     */
    public boolean estaRegistrado(String nombre){
        if (nombre == null || nombre.isBlank()){
            throw new IllegalArgumentException(
                    "El nombre no puede ser nulo ni un campo vacio.");
        }

        return fabricasDeMonstruos.containsKey(nombre);
    }
}
