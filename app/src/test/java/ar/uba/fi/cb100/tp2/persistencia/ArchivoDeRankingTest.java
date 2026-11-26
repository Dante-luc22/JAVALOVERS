package ar.uba.fi.cb100.tp2.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ar.uba.fi.cb100.tp2.modelo.ranking.EntradaDeRanking;
import ar.uba.fi.cb100.tp2.modelo.ranking.Ranking;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

/**
 * Tests de ranking.json: guardar y volver a cargar da exactamente el mismo
 * ranking, y un archivo roto se rechaza con un mensaje que dice qué falló.
 */
class ArchivoDeRankingTest {

    private static final LocalDateTime HOY = LocalDateTime.of(2026, 11, 22, 15, 30, 12);

    @TempDir
    Path carpeta;

    /** La ruta de ranking.json dentro de la carpeta temporal. */
    private Path rutaDelRanking() {
        return this.carpeta.resolve("ranking.json");
    }

    /** Escribe el texto en ranking.json y devuelve su ruta. */
    private Path archivoCon(String contenido) throws IOException {
        Path archivo = rutaDelRanking();
        Files.writeString(archivo, contenido, StandardCharsets.UTF_8);
        return archivo;
    }

    /** Un archivo con una sola entrada, armada con los cuatro campos dados como JSON. */
    private Path archivoConUnaEntrada(String nombre, String puntaje, String dificultad, String fecha)
            throws IOException {
        return archivoCon("[ { \"nombre\": " + nombre + ", \"puntaje\": " + puntaje
                + ", \"dificultad\": " + dificultad + ", \"fecha\": " + fecha + " } ]");
    }

    @Test
    void siElArchivoNoExisteDevuelveUnRankingVacio() {
        Ranking ranking = ArchivoDeRanking.cargar(rutaDelRanking());

        assertEquals(0, ranking.cantidadDePartidas());
    }

    @Test
    void siElArchivoEstaVacioDevuelveUnRankingVacio() throws IOException {
        Ranking ranking = ArchivoDeRanking.cargar(archivoCon(""));

        assertEquals(0, ranking.cantidadDePartidas());
    }

    @Test
    void guardarYVolverACargarDaLasMismasPartidas() {
        Ranking original = new Ranking();
        original.registrar(new EntradaDeRanking("JUA", 250, Dificultad.FACIL, HOY));
        original.registrar(new EntradaDeRanking("FAC", 250, Dificultad.FACIL, HOY.minusDays(1)));
        original.registrar(new EntradaDeRanking("LUC", 0, Dificultad.MEDIO, HOY.plusNanos(123_456_000)));
        original.registrar(new EntradaDeRanking("O'B", 900, Dificultad.DIFICIL, HOY));

        ArchivoDeRanking.guardar(original, rutaDelRanking());
        Ranking cargado = ArchivoDeRanking.cargar(rutaDelRanking());

        assertEquals(original.obtenerTodas(), cargado.obtenerTodas());
        for (Dificultad dificultad : Dificultad.values()) {
            assertEquals(original.obtenerTop10(dificultad), cargado.obtenerTop10(dificultad));
        }
    }

    @Test
    void guardaLosCuatroCamposComoTextoLegible() throws IOException {
        Ranking ranking = new Ranking();
        ranking.registrar(new EntradaDeRanking("LUC", 250, Dificultad.MEDIO, HOY));

        ArchivoDeRanking.guardar(ranking, rutaDelRanking());
        String contenido = Files.readString(rutaDelRanking(), StandardCharsets.UTF_8);

        assertTrue(contenido.contains("\"nombre\": \"LUC\""));
        assertTrue(contenido.contains("\"puntaje\": 250"));
        assertTrue(contenido.contains("\"dificultad\": \"MEDIO\""));
        assertTrue(contenido.contains("\"fecha\": \"2026-11-22T15:30:12\""));
    }

    @Test
    void guardarReemplazaLoQueHabiaEnElArchivo() {
        Ranking viejo = new Ranking();
        viejo.registrar(new EntradaDeRanking("JUA", 100, Dificultad.FACIL, HOY));
        Ranking nuevo = new Ranking();
        nuevo.registrar(new EntradaDeRanking("FAC", 300, Dificultad.MEDIO, HOY));

        ArchivoDeRanking.guardar(viejo, rutaDelRanking());
        ArchivoDeRanking.guardar(nuevo, rutaDelRanking());

        assertEquals(nuevo.obtenerTodas(), ArchivoDeRanking.cargar(rutaDelRanking()).obtenerTodas());
    }

    @Test
    void cargarRespetaElOrdenDelRankingAunqueElArchivoEsteDesordenado() throws IOException {
        Path archivo = archivoCon("""
                [
                  { "nombre": "JUA", "puntaje": 100, "dificultad": "FACIL", "fecha": "2026-11-22T10:00" },
                  { "nombre": "FAC", "puntaje": 300, "dificultad": "FACIL", "fecha": "2026-11-22T11:00" },
                  { "nombre": "MAT", "puntaje": 200, "dificultad": "FACIL", "fecha": "2026-11-22T12:00" }
                ]
                """);

        List<EntradaDeRanking> top = ArchivoDeRanking.cargar(archivo).obtenerTop10(Dificultad.FACIL);

        assertEquals("FAC", top.get(0).nombre());
        assertEquals("MAT", top.get(1).nombre());
        assertEquals("JUA", top.get(2).nombre());
    }

    @Test
    void rechazaUnJsonMalEscrito() throws IOException {
        Path archivo = archivoCon("[ { \"nombre\": \"JUA\", ");

        assertThrows(IllegalStateException.class, () -> ArchivoDeRanking.cargar(archivo));
    }

    @Test
    void rechazaUnaEntradaSinPuntaje() throws IOException {
        Path archivo = archivoConUnaEntrada("\"JUA\"", "null", "\"FACIL\"", "\"2026-11-22T10:00\"");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ArchivoDeRanking.cargar(archivo));

        assertTrue(error.getMessage().contains("puntaje"));
    }

    @Test
    void rechazaUnPuntajeNegativo() throws IOException {
        Path archivo = archivoConUnaEntrada("\"JUA\"", "-56", "\"FACIL\"", "\"2026-11-22T10:00\"");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ArchivoDeRanking.cargar(archivo));

        assertTrue(error.getMessage().contains("negativo"));
    }

    @Test
    void rechazaUnaEntradaSinNombre() throws IOException {
        Path archivo = archivoConUnaEntrada("null", "100", "\"FACIL\"", "\"2026-11-22T10:00\"");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ArchivoDeRanking.cargar(archivo));

        assertTrue(error.getMessage().contains("nombre"));
    }

    @Test
    void rechazaUnaDificultadQueNoExiste() throws IOException {
        Path archivo = archivoConUnaEntrada("\"JUA\"", "100", "\"IMPOSIBLE\"", "\"2026-11-22T10:00\"");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ArchivoDeRanking.cargar(archivo));

        assertTrue(error.getMessage().contains("IMPOSIBLE"));
    }

    @Test
    void rechazaUnaFechaInvalida() throws IOException {
        Path archivo = archivoConUnaEntrada("\"JUA\"", "100", "\"FACIL\"", "\"22/11/2026\"");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ArchivoDeRanking.cargar(archivo));

        assertTrue(error.getMessage().contains("22/11/2026"));
    }

    @Test
    void rechazaRutasYRankingNulos() {
        assertThrows(IllegalArgumentException.class, () -> ArchivoDeRanking.cargar(null));
        assertThrows(IllegalArgumentException.class, () -> ArchivoDeRanking.guardar(null, rutaDelRanking()));
        assertThrows(IllegalArgumentException.class, () -> ArchivoDeRanking.guardar(new Ranking(), null));
    }
}
