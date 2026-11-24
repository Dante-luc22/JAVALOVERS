

package ar.uba.fi.cb100.tp2.persistencia.dto;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Representación de una partida guardada.
 *
 * Contiene datos simples, sin referencias directas a objetos del modelo.
 */
public class PartidaGuardadaDTO {

    public Integer version;

    public Integer pasos;

    public Map<String, Integer> costos = new LinkedHashMap<>();

    public HeroeDTO heroe;

    public MazmorraDTO mazmorra;

    public AgendaDTO agenda;

    // TODO: incorporar el estado restaurable del generador aleatorio.
    // Guardar solamente la semilla original no alcanza.

    public static class HeroeDTO {

        public UbicacionDTO ubicacion;

        public Integer energia;

        public Integer energiaMaxima;

        public Integer cofresAbiertos;

        public List<ObjetoInventarioDTO> inventario = new ArrayList<>();

        public List<EfectoDTO> efectosActivos = new ArrayList<>();
    }

    public static class ObjetoInventarioDTO {

        public String id;

        public Integer cantidad;
    }

    public static class EfectoDTO {

        public String tipo;

        public JsonObject datos = new JsonObject();
    }

    public static class MazmorraDTO {

        public Integer pisosPorTablero;

        public List<PlanoDTO> planos = new ArrayList<>();

        public List<ConexionDTO> conexiones = new ArrayList<>();
    }

    public static class PlanoDTO {

        public TableroDTO tablero;

        public PosicionDTO entrada;

        public PosicionDTO salida;

        public List<EntidadDTO> entidades = new ArrayList<>();
    }

    public static class TableroDTO {

        public Integer filas;

        public Integer columnas;

        public List<List<String>> celdas = new ArrayList<>();
    }

    public static class EntidadDTO {

        public String tipo;

        // Posición actual de la entidad.
        public PosicionDTO posicion;

        public JsonObject datos = new JsonObject();
    }

    public static class ConexionDTO {

        public UbicacionDTO origen;

        public UbicacionDTO destino;
    }

    public static class PosicionDTO {

        public Integer fila;

        public Integer columna;
    }

    public static class UbicacionDTO {

        public Integer tablero;

        public Integer piso;

        public PosicionDTO posicion;
    }

    /**
     * Representación inicial de la agenda.
     * Debe ajustarse a la implementación definitiva de Agenda.
     */
    public static class AgendaDTO {

        public Integer paso;

        public List<EventoDTO> pendientes = new ArrayList<>();
    }

    /**
     * Datos simples de un evento pendiente.
     * No contiene referencias a Heroe, Monstruo u otros objetos.
     */
    public static class EventoDTO {

        public Integer paso;

        public Integer orden;

        public String tipo;

        public JsonObject datos = new JsonObject();
    }
}
