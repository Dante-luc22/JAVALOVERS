package ar.uba.fi.cb100.tp2.modelo.objetos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Collections;
import java.util.Map;

/**
 * Inventario del heroe. Se utilizaron tablas hash id -> cantidad, con un objeto
 * representante por id, se uso LinkedHashMap porque el robo al azar tiene
 * que dar lo mismo con la misma semilla.
 */
public class Inventario {

    private final LinkedHashMap<String, Integer> cantidades = new LinkedHashMap<>();
    private final LinkedHashMap<String, Objeto> objetos = new LinkedHashMap<>();

    public Inventario() {}

    /**
     * Metodo para agregar un objeto al inventario,
     *
     * @param objeto a agregar en el inventario.
     * @throws IllegalArgumentException si el objeto es nulo.
     */
    public void agregar(Objeto objeto) {
        if (objeto == null) {
            throw new IllegalArgumentException("El objeto no puede ser nulo.");
        }
        objetos.put(objeto.getId(), objeto);
        cantidades.put(objeto.getId(), cantidades.getOrDefault(objeto.getId(), 0) + 1);
    }

    /**
     * Restaura una entrada del inventario desde una partida guardada.
     *
     * A diferencia de agregar(), este método establece directamente
     * la cantidad original del objeto.
     *
     * Se conserva el orden de inserción mediante LinkedHashMap.
     *
     * @param objeto objeto concreto que se incorpora
     * @param cantidad cantidad que tenía al guardar la partida
     * @throws IllegalArgumentException si el objeto es nulo,
     *         la cantidad no es positiva o el ID ya existe
     */
    public void restaurar(Objeto objeto, int cantidad) {

        if (objeto == null) {
            throw new IllegalArgumentException(
                    "El objeto no puede ser nulo."
            );
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad a restaurar debe ser positiva."
            );
        }
        String id = objeto.getId();
        validarId(id);
        if (this.objetos.containsKey(id)
                || this.cantidades.containsKey(id)) {
            throw new IllegalArgumentException(
                    "El objeto " + id + " ya existe en el inventario."
            );
        }

        this.objetos.put(id, objeto);
        this.cantidades.put(id, cantidad);
    }


    /**
     * Metodo para saber la cantidad de un objeto en el inventario
     *
     * @param id del objeto a buscar
     * @return la cantidad de ese objeto
     */
    public int cantidad(String id){
        validarId(id);
        return cantidades.getOrDefault(id, 0);
    }

    /**
     * Metodo para validar si el objeto esta en el inventario.
     *
     * @param id del objeto a buscar
     * @return true si contiene al menos uno
     */
    public boolean contiene(String id){
        validarId(id);
        return objetos.containsKey(id);
    }

    /**
     * Metodo para saber si el inventario esta vacio.
     *
     * @return true si esta vacio
     */
    public boolean estaVacio() {
        return cantidades.isEmpty();
    }

    /**
     * Recibe un id de un objeto a sacar del inventario
     *
     * @param id del objeto a quitar
     * @return el objeto quitado
     * @throws IllegalArgumentException si el id no esta en el inventario.
     */
    public Objeto quitar(String id){
        validarId(id);
        if (!contiene(id)) {
            throw new IllegalArgumentException("No hay " + id + " en el inventario.");
        }
        Objeto actual = objetos.get(id);
        int nueva = cantidades.get(id) - 1;
        if (nueva == 0) {
            cantidades.remove(id);
            objetos.remove(id);
        } else {
            cantidades.put(id, nueva);
        }
        return actual;
    }

    /**
     * Metodo para que un heroe use el objeto
     *
     * @param id     del objeto a usar.
     * @param heroe  que usa el objeto
     * @param agenda la agenda, para efectos que vencen más adelante.
     * @throws IllegalArgumentException si el id buscado no existe o si
     *                                  el heroe es nulo
     */
    public void usar(String id, Heroe heroe, Agenda agenda) {
        validarId(id);
        if (!contiene(id)) {
            throw new IllegalArgumentException("El id buscado no existe.");
        }
        if (heroe == null) {
            throw new IllegalArgumentException("El heroe no puede ser nulo.");
        }
        Objeto objeto = quitar(id);
        objeto.usar(heroe, agenda);
    }

    /**
     * Metodo para quitar un objeto al azar del inventario
     *
     * @param azar para elegir el objeto al azar
     * @return el objeto robado o null si el inventario esta vacio.
     * @throws IllegalArgumentException si el azar es nulo.
     */
    public Objeto quitarUnoAlAzar(Random azar){
        if (azar == null) {
            throw new IllegalArgumentException("El objeto random a sacar no puede ser nulo.");
        }
        if (estaVacio()) {
            return null;
        }
        ArrayList<String> ids = new ArrayList<>(cantidades.keySet());
        return quitar(ids.get(azar.nextInt(ids.size())));
    }

    /**
     * Metodo para validar el id del objeto.
     *
     * @param id a validar.
     * @throws IllegalArgumentException si el id es nulo o esta vacio.
     */
    private void validarId(String id){
        if (id == null){
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        if (id.isBlank()) {
            throw new IllegalArgumentException("El id no puede ser vacio.");
        }
    }

    /**
     * Devuelve una copia de las cantidades del inventario,
     * conservando el orden de inserción de los identificadores.
     *
     * El mapa devuelto no permite modificaciones.
     *
     * @return identificadores y cantidades de los objetos
     */
    public Map<String, Integer> getCantidades() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(this.cantidades)
        );
    }

}
