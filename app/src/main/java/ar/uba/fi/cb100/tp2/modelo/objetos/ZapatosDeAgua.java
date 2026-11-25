package ar.uba.fi.cb100.tp2.modelo.objetos;

import ar.uba.fi.cb100.tp2.modelo.efectos.AguaComoPiso;
import ar.uba.fi.cb100.tp2.modelo.efectos.Efecto;
import ar.uba.fi.cb100.tp2.modelo.efectos.QuitarEfecto;
import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;

public class ZapatosDeAgua extends Objeto{

    public static final String ID = "ZAPATOS_DE_AGUA";
    private static final int DURACION = 20;

    /**
     * Constructor de la clase, le envia el ID con super a
     * su TDA padre.
     */
    public ZapatosDeAgua() {
        super(ID);
    }

    /**
     * Metodo de herencia, sirve para indicar que el heroe uso
     * los zapatos de agua.
     *
     * @param heroe  heroe que usa el objeto.
     * @param agenda la agenda, para efectos que vencen más adelante.
     */
    @Override
    public void usar(Heroe heroe, Agenda agenda){
        Efecto efecto = new AguaComoPiso();
        heroe.agregarEfecto(efecto);
        agenda.programar(DURACION, new QuitarEfecto(efecto));
    }
}
