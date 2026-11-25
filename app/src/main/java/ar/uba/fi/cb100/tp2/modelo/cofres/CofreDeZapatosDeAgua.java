package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

public class CofreDeZapatosDeAgua extends Cofre{

    /**
     * Constructor del cofre de repelente, recibe la posicion
     * donde va a estar.
     *
     * @param posicion del cofre
     */
    public CofreDeZapatosDeAgua(Posicion posicion) {
        super(posicion);
    }

    /**
     * Al abrirse el cofre, se aniade el objeto Zapatos de Agua al
     * inventario del heroe.
     *
     * @param heroe  el que abre el cofre;
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    protected void abrir(Heroe heroe, Agenda agenda){
        heroe.getInventario().agregar(new ZapatosDeAgua());
    }
}
