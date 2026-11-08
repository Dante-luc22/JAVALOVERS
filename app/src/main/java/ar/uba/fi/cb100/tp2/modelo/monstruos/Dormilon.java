package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import java.util.HashMap;

/**
 * Clase Domilon, es un cazador que a veces no caza, se queda quieto mientras el
 * heroe esta lejor. Cuando el heroe esta a distancia <= 3, despierta y caza.
 */
public class Dormilon extends Cazador{

    private final int radio;

    /**
     * Constructor de Dormilon, recibe la posicion inicial y el danio
     * que realiza.
     *
     * @param posicionInicial del dormilon.
     * @param danio           que realiza el dormilon.
     */
    public Dormilon(Posicion posicionInicial, int danio, int radio) {
        if (radio <= 0) {
            throw new IllegalArgumentException(
                    "El radio debe ser mayor que 0.");
        }
        super(posicionInicial, danio);
        this.radio = radio;
    }

    /**
     * Metodo para decidir el movimiento del dormilon, si el heroe esta a un radio
     * <=3 lo caza, sino sigue dormido.
     *
     * @param plano lugar del plano donde se va a mover;
     * @param heroe posicion del heroe;
     * @return si sigue dormido o si empezo a cazar.
     */
    @Override
    protected Posicion decidirMovimiento(Plano plano, Posicion heroe){
        HashMap<Posicion, Integer> distancias = calcularDistancias(plano, heroe);

        // si el heroe no puede llegar al dormilon o si el heroe esta lejos
        if (!distancias.containsKey(getPosicion()) || distancias.get(getPosicion()) > radio){
            //sigue durmiendo
            return getPosicion();
        } else {
            // caza como un cazador
            return super.decidirMovimiento(plano, heroe);
        }
    }
}
