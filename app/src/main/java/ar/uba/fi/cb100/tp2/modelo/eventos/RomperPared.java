package ar.uba.fi.cb100.tp2.modelo.eventos;

import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Direccion;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;


 /**
 * Evento que programa el pico: rompe UNA pared vecina al héroe y la convierte en piso.
 * Elige la primera en el orden de Direccion (arriba, abajo, izquierda, derecha).
 * Nunca rompe el borde del tablero (regla R3 del anexo). Si no hay ninguna
 * pared que se pueda romper, no hace nada.
 */
public class RomperPared implements Evento {

    /**
     * Metodo para ejecutar un Evento. Si se ejecuta rompe la primera
     * pared vecina que no sea borde.
     *
     * @param partida la partida en curso, para llegar al héroe, al plano y a los monstruos
     */
    @Override
    public void ejecutar(Partida partida) {
        Tablero tab = partida.getPlanoActual().getTablero();
        Posicion pos = partida.getHeroe().getUbicacion().posicion();

        for (Direccion dir : Direccion.values()) {
            Posicion vecina = pos.mover(dir);
            if (!tab.estaDentro(vecina)) {
                continue;
            }
            if (esBorde(vecina, tab)) {
                continue;
            }
            if (tab.getTerreno(vecina) == Terreno.PARED){
                tab.setTerreno(vecina, Terreno.PISO);
                return;
            }
        }
    }

    /**
     * Metodo boolean que indica si una posicion en el tablero es
     * el borde del tablero, para impedir que el heroe salga del mapa.
     *
     * @param pos posicion en el tablero
     * @param tab tablero en donde se encuentra el heroe.
     * @return true si es un borde.
     */
    private boolean esBorde(Posicion pos, Tablero tab) {
        return pos.fila() == 1 || pos.fila() == tab.getFilas() ||
                pos.columna() == 1 || pos.columna() == tab.getColumnas();
    }
}
