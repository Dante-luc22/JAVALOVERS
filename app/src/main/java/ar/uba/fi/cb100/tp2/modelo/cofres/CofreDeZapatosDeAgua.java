package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.objetos.ZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

public class CofreDeZapatosDeAgua extends Cofre{

    public CofreDeZapatosDeAgua(Posicion posicion) {
        super(posicion);
    }

    @Override
    protected void abrir(Heroe heroe, Agenda agenda){
        heroe.getInventario().agregar(new ZapatosDeAgua());
    }
}
