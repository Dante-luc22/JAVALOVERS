package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.partida.Configuracion;
import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;

public class LectorConfiguracion {

    /**
     * Lee un archivo JSON y lo convierte a una instancia de Configuracion.
     *
     * @param rutaArchivo Ruta relativa o absoluta del archivo .json
     * @return Objeto Configuracion mapeado
     * @throws IOException Si falla la lectura del archivo
     */
    public static Configuracion cargarDesdeJson(String rutaArchivo) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(rutaArchivo)) {
            return gson.fromJson(reader, Configuracion.class);
        }
    }
}