package ar.uba.fi.cb100.tp2.modelo.mundo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePuntos;
import ar.uba.fi.cb100.tp2.modelo.eventos.AdormecerMonstruos;
import ar.uba.fi.cb100.tp2.modelo.eventos.DespertarMonstruos;
import ar.uba.fi.cb100.tp2.modelo.eventos.RepelerMonstruos;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Cazador;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de adormecer, despertar y repeler en Entidad: por defecto no hacen
 * nada, así los eventos del somnífero y del repelente recorren todas las
 * entidades del plano sin preguntar cuáles son monstruos.
 */
class EntidadPorDefectoTest {

    private static final Map<Terreno, Integer> COSTOS =
            Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 8);
    private static final Posicion POSICION_DEL_COFRE = new Posicion(2, 3);
    private static final Posicion INICIO_DEL_CAZADOR = new Posicion(2, 6);

    private Plano plano;
    private Partida partida;
    private CofreDePuntos cofre;
    private Cazador cazador;

    /** Un pasillo en la fila 2 con un cofre y un cazador. */
    private void armar() {
        Tablero tablero = new Tablero(4, 8);
        for (int columna = 2; columna <= 7; columna++) {
            tablero.setTerreno(new Posicion(2, columna), Terreno.PISO);
        }
        this.plano = new Plano(tablero, new Posicion(2, 2), new Posicion(2, 7));
        this.cofre = new CofreDePuntos(POSICION_DEL_COFRE);
        this.cazador = new Cazador(INICIO_DEL_CAZADOR, 30, COSTOS);
        this.plano.agregarEntidad(this.cofre);
        this.plano.agregarEntidad(this.cazador);
        Mazmorra mazmorra = new Mazmorra(List.of(this.plano), 1);
        Heroe heroe = new Heroe(new Ubicacion(1, 1, new Posicion(2, 2)), 100);
        this.partida = new Partida(mazmorra, heroe, COSTOS);
    }

    // =================== UN COFRE NO HACE NADA =================== //

    @Test
    void unCofreNoSeDuermeNiCambiaAlAdormecerlo() {
        armar();

        this.cofre.adormecer();

        assertEquals(POSICION_DEL_COFRE, this.cofre.getPosicion());
        assertFalse(this.cofre.debeSalirDelPlano());
    }

    @Test
    void unCofreNoCambiaAlDespertarlo() {
        armar();

        this.cofre.despertar();

        assertEquals(POSICION_DEL_COFRE, this.cofre.getPosicion());
    }

    @Test
    void unCofreNoSeMueveAlRepelerlo() {
        armar();

        this.cofre.repeler();

        assertEquals(POSICION_DEL_COFRE, this.cofre.getPosicion());
    }

    // =================== LOS EVENTOS CON COFRES EN EL PLANO =================== //

    @Test
    void adormecerConUnCofreEnElPlanoSoloDuermeAlMonstruo() {
        armar();

        new AdormecerMonstruos().ejecutar(this.partida);

        assertTrue(this.cazador.estaAdormecido());
        assertEquals(2, this.plano.getEntidades().size());
    }

    @Test
    void despertarConUnCofreEnElPlanoSoloDespiertaAlMonstruo() {
        armar();
        new AdormecerMonstruos().ejecutar(this.partida);

        new DespertarMonstruos().ejecutar(this.partida);

        assertFalse(this.cazador.estaAdormecido());
    }

    @Test
    void repelerConUnCofreEnElPlanoSoloMueveAlMonstruo() {
        armar();
        this.cazador.setPosicion(new Posicion(2, 4));

        new RepelerMonstruos().ejecutar(this.partida);

        assertEquals(INICIO_DEL_CAZADOR, this.cazador.getPosicion());
        assertEquals(POSICION_DEL_COFRE, this.cofre.getPosicion());
    }
}
