package ar.uba.fi.cb100.tp2.modelo.ranking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

/**
 * Tests del Ranking: el top 10 de cada dificultad sale ordenado, con como
 * mucho diez partidas, sin mezclar dificultades y sin perder empates.
 */
class RankingTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 22, 10, 0);

    private Ranking ranking;

    @BeforeEach
    void crearRanking() {
        this.ranking = new Ranking();
    }

    /**
     * Una entrada de la dificultad dada. Cada minuto de diferencia es una
     * partida distinta, así se pueden armar empates de puntaje a propósito.
     */
    private static EntradaDeRanking entrada(String nombre, int puntaje, Dificultad dificultad, int minuto) {
        return new EntradaDeRanking(nombre, puntaje, dificultad, INICIO.plusMinutes(minuto));
    }

    /** Los puntajes de una lista de entradas, en el mismo orden. */
    private static List<Integer> puntajes(List<EntradaDeRanking> entradas) {
        List<Integer> puntajes = new ArrayList<>();
        for (EntradaDeRanking entrada : entradas) {
            puntajes.add(entrada.puntaje());
        }
        return puntajes;
    }

    @Test
    void unRankingNuevoNoTienePartidas() {
        assertEquals(0, this.ranking.cantidadDePartidas());
        for (Dificultad dificultad : Dificultad.values()) {
            assertTrue(this.ranking.obtenerTop10(dificultad).isEmpty());
        }
    }

    @Test
    void elTopVaDelMejorPuntajeAlPeor() {
        this.ranking.registrar(entrada("Juan", 100, Dificultad.FACIL, 0));
        this.ranking.registrar(entrada("Facu", 300, Dificultad.FACIL, 1));
        this.ranking.registrar(entrada("Mati", 200, Dificultad.FACIL, 2));

        assertEquals(List.of(300, 200, 100), puntajes(this.ranking.obtenerTop10(Dificultad.FACIL)));
    }

    @Test
    void elTopTieneComoMuchoDiezPartidasYSonLasMejores() {
        for (int puntaje = 1; puntaje <= 12; puntaje++) {
            this.ranking.registrar(entrada("Juan", puntaje * 10, Dificultad.MEDIO, puntaje));
        }

        List<EntradaDeRanking> top = this.ranking.obtenerTop10(Dificultad.MEDIO);

        assertEquals(Ranking.TAMANIO_DEL_TOP, top.size());
        assertEquals(List.of(120, 110, 100, 90, 80, 70, 60, 50, 40, 30), puntajes(top));
    }

    @Test
    void cadaTopTieneSoloLasPartidasDeSuDificultad() {
        EntradaDeRanking facil = entrada("Juan", 100, Dificultad.FACIL, 0);
        EntradaDeRanking medio = entrada("Facu", 500, Dificultad.MEDIO, 1);
        this.ranking.registrar(facil);
        this.ranking.registrar(medio);

        assertEquals(List.of(facil), this.ranking.obtenerTop10(Dificultad.FACIL));
        assertEquals(List.of(medio), this.ranking.obtenerTop10(Dificultad.MEDIO));
        assertTrue(this.ranking.obtenerTop10(Dificultad.DIFICIL).isEmpty());
    }

    @Test
    void unEmpateDePuntajeNoPierdeNingunaPartidaYVaPrimeroLaMasVieja() {
        EntradaDeRanking despues = entrada("Facu", 200, Dificultad.FACIL, 5);
        EntradaDeRanking antes = entrada("Juan", 200, Dificultad.FACIL, 1);
        this.ranking.registrar(despues);
        this.ranking.registrar(antes);

        assertEquals(2, this.ranking.cantidadDePartidas());
        assertEquals(List.of(antes, despues), this.ranking.obtenerTop10(Dificultad.FACIL));
    }

    @Test
    void puntajesQueLleganEnOrdenCrecienteQuedanOrdenados() {
        // el caso en que un ABB sin balancear se vuelve una lista
        for (int puntaje = 1; puntaje <= 50; puntaje++) {
            this.ranking.registrar(entrada("Juan", puntaje, Dificultad.DIFICIL, puntaje));
        }

        assertEquals(List.of(50, 49, 48, 47, 46, 45, 44, 43, 42, 41),
                puntajes(this.ranking.obtenerTop10(Dificultad.DIFICIL)));
    }

    @Test
    void registrarDosVecesLaMismaPartidaLaGuardaUnaSolaVez() {
        this.ranking.registrar(entrada("Juan", 100, Dificultad.FACIL, 0));
        this.ranking.registrar(entrada("Juan", 100, Dificultad.FACIL, 0));

        assertEquals(1, this.ranking.cantidadDePartidas());
    }

    @Test
    void obtenerTodasDevuelveTambienLasQueQuedanFueraDelTop() {
        for (int puntaje = 1; puntaje <= 12; puntaje++) {
            this.ranking.registrar(entrada("Juan", puntaje, Dificultad.FACIL, puntaje));
        }
        this.ranking.registrar(entrada("Facu", 7, Dificultad.DIFICIL, 0));

        List<EntradaDeRanking> todas = this.ranking.obtenerTodas();

        assertEquals(13, todas.size());
        assertEquals(13, this.ranking.cantidadDePartidas());
        // primero Fácil en el orden del ranking, después Difícil
        assertEquals(12, todas.get(0).puntaje());
        assertEquals(1, todas.get(11).puntaje());
        assertEquals(Dificultad.DIFICIL, todas.get(12).dificultad());
    }

    @Test
    void cambiarLaListaDelTopNoCambiaElRanking() {
        this.ranking.registrar(entrada("Juan", 100, Dificultad.FACIL, 0));

        this.ranking.obtenerTop10(Dificultad.FACIL).clear();
        this.ranking.obtenerTodas().clear();

        assertEquals(1, this.ranking.obtenerTop10(Dificultad.FACIL).size());
        assertEquals(1, this.ranking.cantidadDePartidas());
    }

    @Test
    void rechazaRegistrarUnaEntradaNula() {
        assertThrows(IllegalArgumentException.class, () -> this.ranking.registrar(null));
    }

    @Test
    void rechazaPedirElTopDeUnaDificultadNula() {
        assertThrows(IllegalArgumentException.class, () -> this.ranking.obtenerTop10(null));
    }

    @Test
    void registraEnCeroUnaPartidaConPuntajeNegativo() {
        this.ranking.registrarPartida("Facu", -40, Dificultad.FACIL, INICIO);

        assertEquals(List.of(0),
                puntajes(this.ranking.obtenerTop10(Dificultad.FACIL)));
    }
}
