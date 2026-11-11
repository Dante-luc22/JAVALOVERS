package ar.uba.fi.cb100.tp2.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ar.uba.fi.cb100.tp2.modelo.partida.Configuracion;
import ar.uba.fi.cb100.tp2.modelo.partida.ParametrosDeDificultad;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

/**
 * Tests de la lectura de configuracion.json: lo que lee bien y cada cosa
 * que tiene que rechazar con un mensaje claro.
 */
public class LectorConfiguracionTest {

    private static final String VALIDA = """
            {
              "energiaInicial": 100,
              "costos": { "piso": 1, "agua": 3, "lava": 8 },
              "semilla": null,
              "dificultades": {
                "FACIL":   { "tableros": 1, "pisos": 1, "filas": 25, "columnas": 40, "cofres": 12, "monstruos": 4 },
                "MEDIO":   { "tableros": 1, "pisos": 3, "filas": 15, "columnas": 20, "cofres": 6, "monstruos": 6 },
                "DIFICIL": { "tableros": 3, "pisos": 3, "filas": 15, "columnas": 20, "cofres": 3, "monstruos": 10 }
              }
            }
            """;

    @TempDir
    Path carpeta;

    /** Escribe el texto en un archivo de la carpeta temporal y devuelve su ruta. */
    private Path archivoCon(String contenido) throws IOException {
        Path archivo = this.carpeta.resolve("configuracion.json");
        Files.writeString(archivo, contenido);
        return archivo;
    }

    @Test
    void leeTodosLosCamposDeUnArchivoValido() throws IOException {
        Configuracion configuracion = LectorConfiguracion.leer(archivoCon(VALIDA));

        assertEquals(100, configuracion.energiaInicial());
        assertEquals(8, configuracion.costos().get("lava"));
        assertNull(configuracion.semilla());
        assertEquals(new ParametrosDeDificultad(1, 1, 25, 40, 12, 4),
                configuracion.parametrosDe(Dificultad.FACIL));
        assertEquals(3, configuracion.parametrosDe(Dificultad.DIFICIL).tableros());
    }

    @Test
    void leeUnaSemillaFija() throws IOException {
        Configuracion configuracion =
                LectorConfiguracion.leer(archivoCon(VALIDA.replace("\"semilla\": null", "\"semilla\": 42")));

        assertEquals(42L, configuracion.semilla());
    }

    @Test
    void rechazaUnArchivoQueNoExiste() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> LectorConfiguracion.leer(this.carpeta.resolve("no-existe.json")));

        assertTrue(error.getMessage().contains("No se encontró"));
    }

    @Test
    void rechazaUnJsonMalEscrito() throws IOException {
        Path archivo = archivoCon("{ \"energiaInicial\": 100, ");

        assertThrows(IllegalStateException.class, () -> LectorConfiguracion.leer(archivo));
    }

    @Test
    void rechazaUnArchivoVacio() throws IOException {
        Path archivo = archivoCon("");

        assertThrows(IllegalStateException.class, () -> LectorConfiguracion.leer(archivo));
    }

    @Test
    void rechazaEnergiaInicialCero() throws IOException {
        Path archivo = archivoCon(VALIDA.replace("\"energiaInicial\": 100", "\"energiaInicial\": 0"));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> LectorConfiguracion.leer(archivo));
        assertTrue(error.getMessage().contains("energiaInicial"));
    }

    @Test
    void rechazaSiFaltaElCostoDeUnTerreno() throws IOException {
        Path archivo = archivoCon(VALIDA.replace(", \"lava\": 8", ""));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> LectorConfiguracion.leer(archivo));
        assertTrue(error.getMessage().contains("lava"));
    }

    @Test
    void rechazaSiFaltaUnaDificultad() throws IOException {
        String sinDificil = VALIDA.replaceAll(",\\s*\"DIFICIL\":[^}]*}", "");
        Path archivo = archivoCon(sinDificil);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> LectorConfiguracion.leer(archivo));
        assertTrue(error.getMessage().contains("DIFICIL"));
    }
}