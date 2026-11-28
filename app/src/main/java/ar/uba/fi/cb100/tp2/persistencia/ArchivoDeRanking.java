package ar.uba.fi.cb100.tp2.persistencia;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import ar.uba.fi.cb100.tp2.modelo.ranking.EntradaDeRanking;
import ar.uba.fi.cb100.tp2.modelo.ranking.Ranking;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;
import ar.uba.fi.cb100.tp2.persistencia.dto.EntradaDeRankingDto;
import ar.uba.fi.cb100.material.ValidacionesUtiles;

/**
 * Lee y escribe ranking.json con Gson. El archivo es una lista con todas las
 * partidas terminadas; al cargarlo, cada una vuelve a entrar al árbol de su
 * dificultad.
 */
public class ArchivoDeRanking {

    /** Dónde se guarda el ranking: la carpeta desde la que se corre el juego. */
    public static final Path RUTA_POR_DEFECTO = Path.of("ranking.json");

    /**
     * Lee el ranking guardado. Si el archivo todavía no existe (nadie terminó
     * una partida) o está vacío, devuelve un ranking vacío.
     *
     * @param ruta dónde está el archivo
     * @return el ranking con todas las partidas del archivo
     * @throws IllegalArgumentException si la ruta es nula
     * @throws IllegalStateException    si el archivo existe pero no se puede
     *                                  leer, no es un JSON válido o tiene una
     *                                  entrada incompleta o inválida
     */
    public static Ranking cargar(Path ruta) {
        ValidacionesUtiles.validarDistintoDeNull(ruta, "ruta");
        Ranking ranking = new Ranking();
        if (!Files.exists(ruta)) {
            return ranking;
        }
        EntradaDeRankingDto[] entradas;
        try (Reader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            entradas = new Gson().fromJson(lector, EntradaDeRankingDto[].class);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ruta, e);
        } catch (JsonParseException e) {
            throw new IllegalStateException(ruta + " no es un JSON válido: " + e.getMessage(), e);
        }
        if (entradas == null) {
            return ranking;
        }
        for (int i = 0; i < entradas.length; i++) {
            ranking.registrar(aEntrada(entradas[i], i + 1, ruta));
        }
        return ranking;
    }

    /**
     * Escribe todas las partidas del ranking en el archivo. Si el archivo ya
     * existía, lo reemplaza.
     *
     * @param ranking el ranking a guardar
     * @param ruta    dónde escribirlo
     * @throws IllegalArgumentException si el ranking o la ruta son nulos
     * @throws IllegalStateException    si no se puede escribir el archivo
     */
    public static void guardar(Ranking ranking, Path ruta) {
        ValidacionesUtiles.validarDistintoDeNull(ranking, "ranking");
        ValidacionesUtiles.validarDistintoDeNull(ruta, "ruta");
        List<EntradaDeRanking> todas = ranking.obtenerTodas();
        EntradaDeRankingDto[] entradas = new EntradaDeRankingDto[todas.size()];
        for (int i = 0; i < todas.size(); i++) {
            entradas[i] = aDto(todas.get(i));
        }
        // sin el escape de HTML, un nombre como "O'Brien" queda escrito tal cual
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try (Writer escritor = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            gson.toJson(entradas, escritor);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el ranking en " + ruta, e);
        }
    }

    /**
     * Pasa una entrada del modelo a la forma que tiene en el archivo.
     *
     * @param entrada la entrada del ranking
     * @return la misma entrada, con la dificultad y la fecha como texto
     */
    private static EntradaDeRankingDto aDto(EntradaDeRanking entrada) {
        return new EntradaDeRankingDto(entrada.nombre(), entrada.puntaje(),
                entrada.dificultad().name(), entrada.fecha().toString());
    }

    /**
     * Pasa una entrada leída del archivo a una entrada del modelo, revisando
     * que no le falte nada: Gson no avisa si falta un campo, lo deja en null.
     *
     * @param dto    la entrada como estaba en el archivo
     * @param numero qué entrada es, desde 1, para nombrarla en el mensaje
     * @param ruta   para nombrar el archivo en el mensaje
     * @return la entrada del ranking
     * @throws IllegalStateException si la entrada está incompleta o es inválida
     */
    private static EntradaDeRanking aEntrada(EntradaDeRankingDto dto, int numero, Path ruta) {
        String donde = ruta + ", entrada " + numero + ": ";
        ValidacionesUtiles.validarEstadoFalso(dto == null,
                donde + "está vacía");
        ValidacionesUtiles.validarEstadoFalso(dto.puntaje() == null,
                donde + "falta el puntaje");
        ValidacionesUtiles.validarEstadoFalso(dto.dificultad() == null,
                donde + "falta la dificultad");
        ValidacionesUtiles.validarEstadoFalso(dto.fecha() == null,
                donde + "falta la fecha");
        Dificultad dificultad;
        try {
            dificultad = Dificultad.valueOf(dto.dificultad());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(donde + "la dificultad " + dto.dificultad() + " no existe", e);
        }
        LocalDateTime fecha;
        try {
            fecha = LocalDateTime.parse(dto.fecha());
        } catch (DateTimeParseException e) {
            throw new IllegalStateException(donde + "la fecha " + dto.fecha() + " no es válida", e);
        }
        try {
            return new EntradaDeRanking(dto.nombre(), dto.puntaje(), dificultad, fecha);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(donde + e.getMessage(), e);
        }
    }
}
