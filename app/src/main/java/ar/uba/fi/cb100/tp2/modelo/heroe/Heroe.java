package ar.uba.fi.cb100.tp2.modelo.heroe;

import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * El personaje que maneja el jugador: sabe dónde está y cuánta energía le queda.
 * Versión mínima con los métodos acordados en el grupo, para que Partida compile
 * y se pueda probar. La versión completa (inventario, efectos) la hace Dam.
 */
public class Heroe {

    private Ubicacion ubicacion;
    private int energia;

    /**
     * @param ubicacion dónde empieza el héroe
     * @param energia   la energía inicial, de la configuración
     */
    public Heroe(Ubicacion ubicacion, int energia) {
        this.ubicacion = ubicacion;
        this.energia = energia;
    }

    /** @return la energía que le queda; nunca baja de 0 */
    public int getEnergia() {
        return this.energia;
    }

    /**
     * Le resta energía sin dejarla negativa, así el puntaje no se ve afectado
     * por un movimiento que costaba más de lo que le quedaba.
     * @param cantidad cuánta energía pierde
     */
    public void restarEnergia(int cantidad) {
        this.energia = Math.max(0, this.energia - cantidad);
    }

    /** @return dónde está: tablero, piso y celda */
    public Ubicacion getUbicacion() {
        return this.ubicacion;
    }

    /**
     * @param destino la ubicación a la que se mueve
     */
    public void mover(Ubicacion destino) {
        this.ubicacion = destino;
    }
}
