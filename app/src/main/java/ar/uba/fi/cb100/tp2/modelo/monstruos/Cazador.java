package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * Clase Cazador, se mueve una celda por paso, por el camino mas corto hacia el heroe.
 * Calculado por BFS(hito 2)
 */
public class Cazador extends Monstruo{

    /**
     * Constructor del cazador que hereda la posicion inicial y el danio
     *
     * @param posicionInicial del cazador.
     * @param danio           que realiza el cazador.
     */
    public Cazador(Posicion posicionInicial, int danio) {
        super(posicionInicial, danio);
    }

    /**
     * Método que Recorre el plano con BFS desde el destino y calcula
     * a cuántos pasos está cada celda alcanzable
     *
     * @param plano   el plano a recorrer
     * @param destino destino donde se quiere llegar
     * @return un mapa celda → distancia al destino;
     *         las celdas inalcanzables y las paredes no aparecen.
     */
    protected HashMap<Posicion, Integer> calcularDistancias(Plano plano, Posicion destino){
        // Creo el mapa vacío
        HashMap<Posicion, Integer> distancias = new HashMap<>();
        // Creo la cola vacía
        ArrayDeque<Posicion> cola = new ArrayDeque<>();

        //el destino está a distancia 0
        distancias.put(destino, 0);
        cola.add(destino);

        // mientras la cola no este vacia
        while(!cola.isEmpty()){
            //saca el primero
            Posicion actual = cola.poll();

            // recorre sus vecinas
            for (Posicion vecina : plano.getTablero().vecinasTransitables(actual)) {
                // si la vecina no fue visitada
                if (!distancias.containsKey(vecina)) {
                    // su distancia actual es la del actual + 1
                    distancias.put(vecina, distancias.get(actual) + 1);

                    // encolo
                    cola.add(vecina);
                }

            }
        }
        return distancias;
    }


    /**
     * Metodo para decidir el movimiento del cazador en el plano,
     * busca la mejor celda para alcanzar al heroe.
     *
     * @param plano lugar del plano donde se va a mover;
     * @param heroe posicion del heroe;
     * @return la mejor celda a la cual moverse
     */
    @Override
    protected Posicion decidirMovimiento(Plano plano, Posicion heroe){
        // calculo las distancias al heroe
        HashMap<Posicion, Integer> distancias = calcularDistancias(plano, heroe);

        // por defecto, se queda quieto
        Posicion mejor = getPosicion();

        // recorro las vecinas
        for (Posicion vecina : plano.getTablero().vecinasTransitables(getPosicion())) {
            // si el heroe no puede llegar a esa vecina, la saltea
            if (!distancias.containsKey(vecina)) {
                continue;
            }

            // si mejor no tiene distancia, cualquier vecina con distancia es mejor
            if (!distancias.containsKey(mejor)){
                mejor = vecina;
            }
            // si no, quedarse con la vecina solo si esta mas cerca
            else if (distancias.get(vecina) < distancias.get(mejor)) {
                mejor = vecina;
            }
        }
        return mejor;
    }


    /**
     * Metodo para indicar que sucede al alcanzar al heroe.
     *
     * @param heroe  heroe que alcanza
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    protected void alAlcanzarAlHeroe(Heroe heroe, Agenda agenda){
        atacarYVolver(heroe);
    }
}
