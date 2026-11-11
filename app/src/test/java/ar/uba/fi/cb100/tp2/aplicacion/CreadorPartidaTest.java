package ar.uba.fi.cb100.tp2.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.generador.GeneradorGrupoNN;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.partida.Configuracion;
import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.partida.ParametrosDeDificultad;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Tests de CreadorPartida con el generador real: que Fácil arme la partida
 * que pide la configuración, y que la misma semilla dé el mismo mundo.
 */
public class CreadorPartidaTest {

    private static Configuracion configuracionCon(Long semilla) {
        return new Configuracion(100, Map.of("piso", 1, "agua", 3, "lava", 8), semilla, Map.of(
                Dificultad.FACIL, new ParametrosDeDificultad(1, 1, 25, 40, 12, 4),
                Dificultad.MEDIO, new ParametrosDeDificultad(1, 3, 15, 20, 6, 6),
                Dificultad.DIFICIL, new ParametrosDeDificultad(3, 3, 15, 20, 3, 10)));
    }

    private static Partida crearFacil(Long semilla) {
        return new CreadorPartida(configuracionCon(semilla), new GeneradorGrupoNN())
                .crear(Dificultad.FACIL);
    }

    @Test
    void facilArmaUnPlanoDelTamanioDeLaConfiguracion() {
        Tablero tablero = crearFacil(42L).getPlanoActual().getTablero();

        assertEquals(25, tablero.getFilas());
        assertEquals(40, tablero.getColumnas());
    }

    @Test
    void elHeroeEmpiezaEnLaEntradaConLaEnergiaDeLaConfiguracion() {
        Partida partida = crearFacil(42L);
        Plano plano = partida.getPlanoActual();

        assertEquals(plano.getEntrada(), partida.getHeroe().getUbicacion().posicion());
        assertEquals(100, partida.getHeroe().getEnergia());
        assertEquals(0, partida.getPasos());
        assertEquals(EstadoPartida.EN_CURSO, partida.estadoActual());
    }

    @Test
    void facilTieneSalidaYLosCofresYMonstruosDeLaConfiguracion() {
        Plano plano = crearFacil(42L).getPlanoActual();

        assertNotNull(plano.getSalida());
        assertEquals(12, contar(plano, 'C'));
        assertEquals(4, contar(plano, 'M'));
    }

    @Test
    void laMismaSemillaArmaElMismoMundo() {
        assertEquals(describir(crearFacil(7L)), describir(crearFacil(7L)));
    }

    @Test
    void sinSemillaIgualArmaUnaPartidaJugable() {
        Partida partida = crearFacil(null);

        assertEquals(EstadoPartida.EN_CURSO, partida.estadoActual());
        assertNotNull(partida.getPlanoActual().getSalida());
    }

    // ---------- ayudas ----------

    private static int contar(Plano plano, char simbolo) {
        int cantidad = 0;
        for (Entidad entidad : plano.getEntidades()) {
            if (entidad.getSimbolo() == simbolo) {
                cantidad++;
            }
        }
        return cantidad;
    }

    /**
     * El plano como texto: todos los terrenos, entrada, salida y cada entidad
     * con su posición. Dos mundos iguales dan el mismo texto.
     */
    private static String describir(Partida partida) {
        Plano plano = partida.getPlanoActual();
        Tablero tablero = plano.getTablero();
        StringBuilder texto = new StringBuilder();
        for (int fila = 1; fila <= tablero.getFilas(); fila++) {
            for (int columna = 1; columna <= tablero.getColumnas(); columna++) {
                texto.append(tablero.getTerreno(new Posicion(fila, columna)).name().charAt(0));
            }
            texto.append('\n');
        }
        texto.append("entrada ").append(plano.getEntrada())
                .append(" salida ").append(plano.getSalida()).append('\n');
        for (Entidad entidad : plano.getEntidades()) {
            texto.append(entidad.getSimbolo()).append(entidad.getPosicion()).append('\n');
        }
        return texto.toString();
    }
}
