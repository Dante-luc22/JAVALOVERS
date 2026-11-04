package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

public class CatalogoDeMonstruos{

    private final LinkedHashMap<String, FabricaDeMonstruo> fabricasDeMonstruos =  new LinkedHashMap<>();

    public CatalogoDeMonstruos(){
    }

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

    public int cantidadDeTipos() {
        return fabricasDeMonstruos.size();
    }

    public boolean estaRegistrado(String nombre){
        if (nombre == null || nombre.isBlank()){
            throw new IllegalArgumentException(
                    "El nombre no puede ser nulo ni un campo vacio.");
        }

        return fabricasDeMonstruos.containsKey(nombre);
    }
}
