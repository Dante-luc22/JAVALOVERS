package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.eventos.AdormecerMonstruos;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.eventos.DespertarMonstruos;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Clase cofre de somnifero, adormece a todos los monstruos del plano
 * durante 5 pasos; adormecidos no se mueven ni atacan; si el heroe
 * pisa a uno, lo despierta del todo, sin danio; dos somniferos se
 * suman, gracias al contador.
 */
public class CofreDeSomnifero extends Cofre{

    private static final int DURACION = 5;

    /**
     * Constructor del cofre de somnifero, recibe la posicion
     * donde va a estar
     * @param posicion del cofre.
     */
    public CofreDeSomnifero(Posicion posicion) {
        super(posicion);
    }

     @Override
    protected void abrir(Heroe heroe, Agenda agenda) {
        Ubicacion donde = heroe.getUbicacion();
        agenda.programar(1, new AdormecerMonstruos(donde.tablero(), donde.piso()));
        agenda.programar(1 + DURACION, new DespertarMonstruos(donde.tablero(), donde.piso()));
    }
}
