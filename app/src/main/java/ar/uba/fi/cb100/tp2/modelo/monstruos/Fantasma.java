package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Clase Fantasma, se mueve en línea recta hacia el heroe, atravesando paredes,
 * pero avanza solo cada 2 pasos. Al alcanzar al heroe realiza lo mismo que
 * el Errante.
 */
public class Fantasma extends Monstruo{

    /**
     * Si en este paso descansa. Se invierte en cada paso;
     * arranca en true, así que el primer paso descansa.
     */
    private boolean descansaEsteTurno = true;

    /**
     * Constructor de fantasma, pide su posicion inicial y el danio
     * que realiza.
     *
     * @param posicionInicial del fantasma.
     * @param danio           que realiza.
     */
    public Fantasma(Posicion posicionInicial, int danio) {
        super(posicionInicial, danio);
    }


    /**
     * Método para calcular la distancia en línea recta, |Δfila| + |Δcolumna|
     * sin tener en cuenta las paredes.
     *
     * @param a celda
     * @param b celda
     * @return el absoluto de la distancia entre ellas
     */
    private int distancia(Posicion a, Posicion b) {
        // distancia entre las posiciones
        return Math.abs(a.fila() - b.fila()) + Math.abs(a.columna() - b.columna());
    }


    /**
     * Metodo para decidir el movimiento del fantasma.
     *
     * @param plano lugar del plano donde se va a mover;
     * @param heroe posicion del heroe;
     * @return la mejor posicion para moverse.
     */
    @Override
    protected Posicion decidirMovimiento(Plano plano, Posicion heroe){
        boolean descansa = this.descansaEsteTurno;
        this.descansaEsteTurno = !this.descansaEsteTurno;

        // si este paso descansa: actualiza el campo y se queda quieto
        if (descansa){
            return getPosicion();
        }

        //por defecto, se queda donde está
        Posicion mejor = getPosicion();

        // probar las 4 direcciones
        for(Direccion direccion : Direccion.values()){
            //calcula la candidata
            Posicion candidata = getPosicion().mover(direccion);

            // descarta las que caen fuera del tablero (las paredes no)
            if (!plano.getTablero().estaDentro(candidata)){
                continue;
            }

            // se queda con la candidata si esta mas cerca del heroe
            if (distancia(candidata, heroe) < distancia(mejor, heroe)){
                mejor = candidata;
            }
        }

        // devuelve la elegida
        return mejor;
    }

    /**
     * Metodo que dice que hace el fantasma al alcanzar al heroe.
     *
     * @param heroe  heroe que alcanza
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    protected void alAlcanzarAlHeroe(Heroe heroe, Agenda agenda){
        atacarYVolver(heroe);
    }
}
