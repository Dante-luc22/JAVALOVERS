package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.objetos.BotasIgnifugas;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

public class CofreDeBotasIgnifugas extends Cofre{

    public CofreDeBotasIgnifugas(Posicion posicion){
        super(posicion);
    }

    @Override
    protected void abrir(Heroe heroe, Agenda agenda){
        heroe.getInventario().agregar(new BotasIgnifugas());
    }
}
