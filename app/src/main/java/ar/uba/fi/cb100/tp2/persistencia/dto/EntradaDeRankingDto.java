package ar.uba.fi.cb100.tp2.persistencia.dto;

/**
 * Una entrada del ranking tal como queda escrita en ranking.json.
 *
 * La dificultad y la fecha van como texto ("MEDIO", "2026-11-22T15:30:12"):
 * Gson no sabe leer ni escribir un LocalDateTime sin ayuda, y así el archivo
 * se puede leer a ojo. El puntaje es Integer y no int para poder distinguir
 * un puntaje que falta en el archivo (Gson lo deja en null) de un 0.
 *
 * @param nombre     quién jugó la partida
 * @param puntaje    los puntos que sacó
 * @param dificultad el nombre de la dificultad
 * @param fecha      cuándo terminó, en formato ISO
 */
public record EntradaDeRankingDto(String nombre, Integer puntaje, String dificultad, String fecha) {
}
