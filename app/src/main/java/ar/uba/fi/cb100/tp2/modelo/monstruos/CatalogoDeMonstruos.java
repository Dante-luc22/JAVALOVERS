package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.LinkedHashMap;
import java.util.Random;

public class CatalogoDeMonstruos implements FabricaDeMonstruo{

    private CatalogoDeMonstruos(){}

    private final LinkedHashMap<String, FabricaDeMonstruo> fabricasDeMonstruos (){}

    public void registrar(String nombre, FabricaDeMonstruo fabrica){
        if (nombre == null || nombre.isBlank()){
            throw new IllegalArgumentException(
                    "El nombre no puede ser nulo ni un campo vacio.");
        }
        if (fabrica == null){
            throw new IllegalArgumentException(
                    "La fabrica dada no puede ser nula.");
        }

    }

    public FabricaDeMonstruo crearAlAzar(Posicion p, Random azar){
        if (p == null || azar == null){
            throw new IllegalArgumentException(
                    "La posicion y el azar no pueden ser nulos.");
        }
    }

    public void cantidadDeTipos() {}

    public boolean estaRegistrado(String nombre){}

    @Override
    public Monstruo crear(Posicion p) {
        return null;
    }
}
