
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Escribe una representación de una partida en formato JSON.
 */
public class EscritorPartida {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Guarda la información recibida en un archivo JSON.
     *
     * @param partida información de la partida
     * @param destino ubicación del archivo JSON
     * @throws IllegalArgumentException si los argumentos son nulos
     * @throws IllegalStateException si no se puede escribir el archivo
     */
    public void guardar(PartidaGuardadaDTO partida, Path destino) {

        if (partida == null) {
            throw new IllegalArgumentException(
                    "La partida a guardar no puede ser nula.");
        }

        if (destino == null) {
            throw new IllegalArgumentException(
                    "La ruta del archivo no puede ser nula.");
        }

        if (partida.heroe == null || partida.mazmorra == null) {
            throw new IllegalArgumentException(
                    "La partida debe contener héroe y mazmorra.");
        }

        Path ruta = destino.toAbsolutePath().normalize();
        Path carpeta = ruta.getParent();
        Path temporal = null;

        try {
            Files.createDirectories(carpeta);

            temporal = Files.createTempFile(
                    carpeta,
                    "partida-",
                    ".tmp"
            );

            // Escribir primero en el archivo temporal.
            try (Writer escritor = Files.newBufferedWriter(
                    temporal,
                    StandardCharsets.UTF_8)) {

                GSON.toJson(partida, escritor);
            }

            // Reemplazar el archivo definitivo.
            try {
                Files.move(
                        temporal,
                        ruta,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );

            } catch (AtomicMoveNotSupportedException e) {

                // Algunos sistemas de archivos no admiten movimientos atómicos.
                Files.move(
                        temporal,
                        ruta,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No se pudo guardar la partida en: " + ruta,
                    e
            );

        } finally {

            // Si quedó un temporal por algún error, intentar eliminarlo.
            if (temporal != null) {
                try {
                    Files.deleteIfExists(temporal);
                } catch (IOException ignored) {
                    // La limpieza del temporal no debe ocultar
                    // el error original del guardado.
                }
            }
        }
    }
}
