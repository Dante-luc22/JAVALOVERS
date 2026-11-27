package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.objetos.Objeto;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.Random;

/**
 * Clase ladron, un ladron se mueve igual que un errante y al alcanzar
 * al heroe le roba un objeto al azar del inventario, si no tenia nada
 * en el inventario, lo ataca y vuelve.
 */
public class Ladron extends Errante{

    /**
     * Constructor del ladron, recibe la posicion inicial y el
     * danio que hace.
     *
     * @param posicionInicial del ladron
     * @param danio           que realiza el ladron
     * @param azar            generador de azar compartido de la partida,
     *                        creado con la semilla
     */
    public Ladron(Posicion posicionInicial, int danio, Random azar){
        super(posicionInicial, danio, azar);
    }

    public Ladron(
            Posicion posicionInicial,
            Posicion posicionActual,
            int danio,
            Random azar) {

        super(posicionInicial, posicionActual, danio, azar);
    }
    /**
     * @return el tipo de este monstruo, para que la vista elija su imagen
     */
    @Override
    public TipoDeMonstruo getTipoDeMonstruo() {
        return TipoDeMonstruo.LADRON;
    }

    /**
     * Metodo para indicar que reliza el ladron al alcanzar al heroe.
     *
     * @param heroe  heroe que alcanza
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    protected void alAlcanzarAlHeroe(Heroe heroe, Agenda agenda) {
        Objeto robado = heroe.getInventario().quitarUnoAlAzar(getAzar());
        if (robado == null) {
            atacarYVolver(heroe);
        }
        volverAlInicio();
    }
}
