package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeBotasIgnifugas;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePocion;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePuntos;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeTrampaDePinchos;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeVeneno;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeZapatosDeAgua;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeEscudo;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePico;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeRepelente;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeSomnifero;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EntidadDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PosicionDTO;

import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Cazador;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Dormilon;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Errante;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Fantasma;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Ladron;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Monstruo;


import com.google.gson.JsonObject;

public class MapeadorEntidad {
    private static final String TIPO_ESCUDO = "CofreDeEscudo";
    private static final String TIPO_PICO = "CofreDePico";
    private static final String TIPO_REPELENTE = "CofreDeRepelente";
    private static final String TIPO_SOMNIFERO = "CofreDeSomnifero";
    private static final String TIPO_BOTAS_IGNIFUGAS = "CofreDeBotasIgnifugas";
    private static final String TIPO_POCION = "CofreDePocion";
    private static final String TIPO_PUNTOS = "CofreDePuntos";
    private static final String TIPO_TRAMPA_PINCHOS = "CofreDeTrampaDePinchos";
    private static final String TIPO_VENENO = "CofreDeVeneno";
    private static final String TIPO_ZAPATOS_AGUA = "CofreDeZapatosDeAgua";

    private static final String TIPO_CAZADOR = "Cazador";
    private static final String TIPO_DORMILON = "Dormilon";
    private static final String TIPO_ERRANTE = "Errante";
    private static final String TIPO_FANTASMA = "Fantasma";
    private static final String TIPO_LADRON = "Ladron";

    /**
     * Convierte una entidad del modelo en su representación DTO.
     * Por ahora solamente admite los seis tipos de cofres.
     *
     * @param entidad entidad que se desea guardar
     * @return representación DTO de la entidad
     */
    public EntidadDTO mapear(Entidad entidad) {

        if (entidad == null) {
            throw new IllegalArgumentException(
                    "La entidad no puede ser nula."
            );
        }

        if (entidad.getPosicion() == null) {
            throw new IllegalStateException(
                    "La entidad no tiene una posición válida."
            );
        }

        // Un cofre abierto debería haber desaparecido del plano.
        // Si todavía está presente, no se debe reconstruir como cerrado.
        if (entidad.debeSalirDelPlano()) {
            throw new IllegalStateException(
                    "No se puede guardar una entidad que debe salir del plano."
            );
        }

        EntidadDTO datos = new EntidadDTO();

        datos.posicion = mapearPosicion(entidad.getPosicion());
        datos.datos = new JsonObject();

        Class<?> tipo = entidad.getClass();

        if (tipo == CofreDeBotasIgnifugas.class) {datos.tipo = TIPO_BOTAS_IGNIFUGAS;
        } else if (tipo == CofreDePocion.class) {

            CofreDePocion cofre = (CofreDePocion) entidad;

            datos.tipo = TIPO_POCION;
            datos.datos.addProperty("cantidad", cofre.getCantidad());

        } else if (tipo == CofreDePuntos.class) {datos.tipo = TIPO_PUNTOS;
        } else if (tipo == CofreDeTrampaDePinchos.class) {

            CofreDeTrampaDePinchos cofre =
                    (CofreDeTrampaDePinchos) entidad;

            datos.tipo = TIPO_TRAMPA_PINCHOS;
            datos.datos.addProperty("danio", cofre.getDanio());

        } else if (tipo == CofreDeVeneno.class) {datos.tipo = TIPO_VENENO;
        } else if (tipo == CofreDeZapatosDeAgua.class) {datos.tipo = TIPO_ZAPATOS_AGUA;
        }else if (tipo == CofreDeEscudo.class) {datos.tipo = TIPO_ESCUDO;
        } else if (tipo == CofreDePico.class) {datos.tipo = TIPO_PICO;
        } else if (tipo == CofreDeRepelente.class) {datos.tipo = TIPO_REPELENTE;
        } else if (tipo == CofreDeSomnifero.class) {datos.tipo = TIPO_SOMNIFERO;
        } else if (tipo == Dormilon.class) {

        Dormilon monstruo = (Dormilon) entidad;

        datos.tipo = TIPO_DORMILON;
        mapearDatosMonstruo(datos, monstruo);
        datos.datos.addProperty("radio", monstruo.getRadio());

    } else if (tipo == Cazador.class) {

        Cazador monstruo = (Cazador) entidad;

        datos.tipo = TIPO_CAZADOR;
        mapearDatosMonstruo(datos, monstruo);

    } else if (tipo == Ladron.class) {

        Ladron monstruo = (Ladron) entidad;

        datos.tipo = TIPO_LADRON;
        mapearDatosMonstruo(datos, monstruo);

    } else if (tipo == Errante.class) {

        Errante monstruo = (Errante) entidad;

        datos.tipo = TIPO_ERRANTE;
        mapearDatosMonstruo(datos, monstruo);

    } else if (tipo == Fantasma.class) {

        Fantasma monstruo = (Fantasma) entidad;

        datos.tipo = TIPO_FANTASMA;
        mapearDatosMonstruo(datos, monstruo);
        datos.datos.addProperty(
                "descansaEsteTurno",
                monstruo.isDescansaEsteTurno()
        );

    } else {
        throw new UnsupportedOperationException(
                "Todavía no está implementada la persistencia de la entidad: "
                        + tipo.getName()
        );
    }

        return datos;
    }

    private PosicionDTO mapearPosicion(Posicion posicion) {

        PosicionDTO datos = new PosicionDTO();

        datos.fila = posicion.fila();
        datos.columna = posicion.columna();

        return datos;
    }

    private void mapearDatosMonstruo(
            EntidadDTO datos,
            Monstruo monstruo) {

        Posicion inicial = monstruo.getPosicionInicial();

        if (inicial == null) {
            throw new IllegalStateException(
                    "El monstruo no tiene una posición inicial válida."
            );
        }

        datos.datos.addProperty("filaInicial", inicial.fila());
        datos.datos.addProperty("columnaInicial", inicial.columna());
        datos.datos.addProperty("danio", monstruo.getDanio());
        datos.datos.addProperty("adormecimientos", monstruo.getAdormecimientos());
    }
}
