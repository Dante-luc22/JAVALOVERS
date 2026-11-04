package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.List;
import java.util.Random;

/**
 * Clase de monstruo errante, se mueve al azar por el mapa.
 */
public class Errante extends Monstruo {

    private final Random azar;

    /**
     * Constructor de la clase Errante con su posicion y danio inicial.
     *
     * @param posicionInicial desde donde empieza el errante
     * @param danio           danio que hace el errante
     * @param azar            generador de azar compartido de la partida, creado con la semilla
     */
    public Errante(Posicion posicionInicial, int danio, Random azar) {
        super(posicionInicial, danio);
        if (azar == null) {
            throw new IllegalArgumentException("azar no puede ser nulo");
        }
        this.azar = azar;
    }


    /**
     * Metodo para decidir el movimiento del errante.
     * Elige al azar una vecina transitable; si no hay ninguna, se queda donde está
     *
     * @param plano lugar del plano donde se va a mover;
     * @param heroe posicion del heroe;
     * @return posicion a la que se mueve
     */
    @Override
    protected Posicion decidirMovimiento(Plano plano, Posicion heroe) {
        List<Posicion> vecinas = plano.getTablero().vecinasTransitables(getPosicion());
        if (vecinas.isEmpty()) {
            return getPosicion();
        } else {
            int indice = azar.nextInt(vecinas.size());
            return vecinas.get(indice);
        }
    }

    /**
     * Metodo que dice que hace el errante al alcanzar al heroe
     *
     * @param heroe  heroe que alcanza
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    protected void alAlcanzarAlHeroe(Heroe heroe, Agenda agenda) {
        atacarYVolver(heroe);
    }

}
