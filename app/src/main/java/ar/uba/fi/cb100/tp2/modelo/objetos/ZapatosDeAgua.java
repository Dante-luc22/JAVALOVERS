package ar.uba.fi.cb100.tp2.modelo.objetos;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;

public class ZapatosDeAgua extends Objeto{

    private static final String ID = "ZAPATOS_DE_AGUA";

    public ZapatosDeAgua() {
        super(ID);
    }

    @Override
    public void usar(Heroe heroe, Agenda agenda){
    }
}
