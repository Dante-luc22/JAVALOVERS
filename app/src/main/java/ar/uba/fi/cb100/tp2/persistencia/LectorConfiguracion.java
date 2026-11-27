package ar.uba.fi.cb100.tp2.persistencia;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import ar.uba.fi.cb100.tp2.modelo.partida.Configuracion;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;
import ar.uba.fi.cb100.material.ValidacionesUtiles;

/**
 * Lee configuracion.json con Gson. Si el archivo falta o le falta algún dato,
 * lo rechaza con un mensaje claro: no hay valores escondidos en el código.
 */
public class LectorConfiguracion {

    /** Dónde se busca el archivo: la carpeta desde la que se corre el juego. */
    public static final Path RUTA_POR_DEFECTO = Path.of("configuracion.json");

    private static final String[] TERRENOS_CON_COSTO = { "piso", "agua", "lava" };

    /**
     * @param ruta dónde está el archivo
     * @return la configuración leída y verificada
     * @throws IllegalStateException si el archivo no existe, no es un JSON válido
     *                               o le falta algún dato
     */
    public static Configuracion leer(Path ruta) {
        ValidacionesUtiles.validarEstadoFalso(!Files.exists(ruta),
                "No se encontró el archivo de configuración: "
                + ruta.toAbsolutePath());
        Configuracion configuracion;
        try (Reader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            configuracion = new Gson().fromJson(lector, Configuracion.class);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ruta, e);
        } catch (JsonParseException e) {
            throw new IllegalStateException(ruta + " no es un JSON válido: " + e.getMessage(), e);
        }
        verificar(configuracion, ruta);
        return configuracion;
    }

    /**
     * Revisa que no falte nada. Gson no avisa si falta un campo: lo deja en 0 o null.
     * @param configuracion lo que leyó Gson
     * @param ruta          para nombrar el archivo en el mensaje
     */
    private static void verificar(Configuracion configuracion, Path ruta) {
        ValidacionesUtiles.validarEstadoFalso(configuracion == null,
                ruta + " está vacío");
        ValidacionesUtiles.validarEstadoFalso(configuracion.energiaInicial() <= 0,
                ruta + ": falta energiaInicial o no es positiva");
        ValidacionesUtiles.validarEstadoFalso(configuracion.costos() == null,
                ruta + ": faltan los costos");
        for (String terreno : TERRENOS_CON_COSTO) {
            ValidacionesUtiles.validarEstadoFalso(!configuracion.costos().containsKey(terreno),
                    ruta + ": falta el costo de " + terreno);
        }
        ValidacionesUtiles.validarEstadoFalso(configuracion.dificultades() == null,
                ruta + ": faltan las dificultades");
        for (Dificultad dificultad : Dificultad.values()) {
            ValidacionesUtiles.validarEstadoFalso(!configuracion.dificultades().containsKey(dificultad),
                    ruta + ": falta la dificultad " + dificultad);
        }
    }
}