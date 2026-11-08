package ar.uba.fi.cb100.tp2.generador;

import ar.uba.fi.cb100.tp2.construccion.ValidadorPlano;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExportarEjemplos {

    public static void main(String[] args) throws IOException {
        Path carpeta = Path.of("ejemplos", "generador");
        Files.createDirectories(carpeta);

        exportar(carpeta, "FACIL", 25, 40, 12, 4, true);
        exportar(carpeta, "MEDIO", 15, 20, 2, 2, false);
        exportar(carpeta, "DIFICIL", 15, 20, 1, 1, true);

        System.out.println(
                "Ejemplos guardados en: " + carpeta.toAbsolutePath()
        );
    }

    private static void exportar(
            Path carpeta,
            String dificultad,
            int filas,
            int columnas,
            int cofres,
            int monstruos,
            boolean conSalida
    ) throws IOException {
        JsonObject parametros = new JsonObject();

        parametros.addProperty("version", 1);
        parametros.addProperty("dificultad", dificultad);
        parametros.addProperty("filas", filas);
        parametros.addProperty("columnas", columnas);
        parametros.addProperty("semilla", 1L);
        parametros.addProperty("cofres", cofres);
        parametros.addProperty("monstruos", monstruos);
        parametros.addProperty("conSalida", conSalida);

        String resultado = new GeneradorGrupoNN()
                .generar(parametros.toString());

        new ValidadorPlano().validar(resultado);

        Path archivo = carpeta.resolve(
                dificultad.toLowerCase(java.util.Locale.ROOT)
                        + "-semilla-1.json"
        );

        Files.writeString(
                archivo,
                resultado,
                StandardCharsets.UTF_8
        );

        System.out.println("Generado y validado: " + archivo);
    }
}