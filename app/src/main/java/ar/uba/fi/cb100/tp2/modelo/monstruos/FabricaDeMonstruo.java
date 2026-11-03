package ar.uba.fi.cb100.tp2.modelo.monstruos;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

public interface FabricaDeMonstruo {

    public Monstruo crear(Posicion p);
}
