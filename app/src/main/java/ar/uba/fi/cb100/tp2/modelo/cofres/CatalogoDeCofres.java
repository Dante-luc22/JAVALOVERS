package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import ar.uba.fi.cb100.material.ValidacionesUtiles;

/**
 * Clase que guarda una fabrica por cada tipo de cofre y crea uno al azar.
 * Agregar un tipo al juego es un registrar mas.
 */
public class CatalogoDeCofres {

    private final LinkedHashMap<String, FabricaDeCofre> fabricasDeCofres = new LinkedHashMap<>();

    /**
     * Constructor vacio
     */
    public CatalogoDeCofres() {
    }


    /**
     * Metodo para registrar un cofre en el catalogo.
     *
     * @param nombre  nombre del tipo de cofre.
     * @param fabrica la que sabe crear ese tipo.
     * @throws IllegalArgumentException si el nombre es nulo o vacio;
     *                                  si la fabrica es nula;
     *                                  si el nombre ya esta registrado;
     */
    public void registrar(String nombre, FabricaDeCofre fabrica) {
        ValidacionesUtiles.validarDistintoDeNull(nombre, "nombre");
        ValidacionesUtiles.validarFalso(nombre.isBlank(),
                "El nombre no puede ser nulo ni un campo vacio");
        ValidacionesUtiles.validarDistintoDeNull(fabrica, "fabrica");
        ValidacionesUtiles.validarFalso(estaRegistrado(nombre),
                "El nombre ya se encuentra registrado.");
        fabricasDeCofres.put(nombre, fabrica);
    }


    /**
     * Metodo que crea un cofre al azar en una posicion.
     *
     * @param p    posicion donde se crea.
     * @param azar utilizado para un indice al azar.
     * @return devuelve el cofre creado.
     * @throws IllegalArgumentException si la posicion es nula;
     *                                  si random es nulo;
     * @throws IllegalStateException si el catalogo esta vacio;
     */
    public Cofre crearAlAzar(Posicion p, Random azar) {
        ValidacionesUtiles.validarDistintoDeNull(p, "posicion");
        ValidacionesUtiles.validarDistintoDeNull(azar, "azar");
        ValidacionesUtiles.validarEstadoFalso(fabricasDeCofres.isEmpty(),
                "El catalogo no tiene tipos de cofre registrados.");
        ArrayList<FabricaDeCofre> fabricas = new ArrayList<>(fabricasDeCofres.values());
        int indice = azar.nextInt(fabricas.size());
        FabricaDeCofre fabricaElegida = fabricas.get(indice);
        return fabricaElegida.crear(p);
    }


    /**
     * Devuelve la cantidad de tipos de cofres que hay.
     *
     * @return cantidad de tipos.
     */
    public int cantidadDeTipos(){
        return fabricasDeCofres.size();
    }


    /**
     * Metodo para decir si un tipo de cofre ya esta registrado.
     *
     * @param nombre nombre a buscar
     * @return True si ya esta registrado.
     * @throws IllegalArgumentException si el nombre es nulo o vacio.
     */
    public boolean estaRegistrado(String nombre){
        ValidacionesUtiles.validarDistintoDeNull(nombre, "nombre");
        ValidacionesUtiles.validarFalso(nombre.isBlank(),
                "El nombre no puede ser nulo ni vacio.");
        return fabricasDeCofres.containsKey(nombre);
    }
}
