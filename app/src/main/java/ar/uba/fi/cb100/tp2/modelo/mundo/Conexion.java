package ar.uba.fi.cb100.tp2.modelo.mundo;

import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;
import ar.uba.fi.cb100.material.ValidacionesUtiles;

/**
 * Una escalera o un pasillo: lleva de una celda de un plano a una celda de otro.
 * Para el modelo son lo mismo; sólo cambia dónde se colocan.
 *
 * @param origen  la celda donde está la escalera o el pasillo
 * @param destino adónde aparece el héroe al pisarla
 */
public record Conexion(Ubicacion origen, Ubicacion destino) {

    /**
     * @throws IllegalArgumentException si alguna punta es nula o si las dos
     *                                  están en el mismo plano
     */
    public Conexion {
        ValidacionesUtiles.validarDistintoDeNull(origen, "origen");
        ValidacionesUtiles.validarDistintoDeNull(destino, "destino");
        ValidacionesUtiles.validarFalso(origen.tablero() == destino.tablero() && origen.piso() == destino.piso(),
                "Una conexión une dos planos distintos: " + origen + " y " + destino);
    }

    /**
     * @return la misma conexión recorrida al revés
     */
    public Conexion invertida() {
        return new Conexion(this.destino, this.origen);
    }
}