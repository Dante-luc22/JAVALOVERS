package ar.uba.fi.cb100.tp2.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.generador.GeneradorGrupoNN;
import ar.uba.fi.cb100.tp2.modelo.mundo.Conexion;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.partida.Configuracion;
import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.partida.ParametrosDeDificultad;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;
import ar.uba.fi.cb100.tp2.modelo.cofres.CatalogoDeCofres;
import ar.uba.fi.cb100.tp2.modelo.monstruos.CatalogoDeMonstruos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.List;



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

        private static Partida crear(Dificultad dificultad, Long semilla) {
        return new CreadorPartida(configuracionCon(semilla), new GeneradorGrupoNN())
                .crear(dificultad);
    }

        @Test
    void facilNoTieneEscaleras() {
        assertTrue(crearFacil(42L).getMazmorra().getConexiones().isEmpty());
    }

    @Test
    void medioUneLosTresPisosConDosEscaleras() {
        Mazmorra mazmorra = crear(Dificultad.MEDIO, 42L).getMazmorra();

        assertEquals(2, mazmorra.getConexiones().size());
        assertEquals(1, mazmorra.conexionesDelPlano(1, 1).size());
        assertEquals(2, mazmorra.conexionesDelPlano(1, 2).size());
        assertEquals(1, mazmorra.conexionesDelPlano(1, 3).size());
    }

    @Test
    void cadaEscaleraUneLaMismaPosicionEnPisosSeguidos() {
        for (Conexion escalera : crear(Dificultad.MEDIO, 42L).getMazmorra().getConexiones()) {
            assertEquals(escalera.origen().posicion(), escalera.destino().posicion());
            assertEquals(escalera.origen().tablero(), escalera.destino().tablero());
            assertEquals(1, Math.abs(escalera.origen().piso() - escalera.destino().piso()));
        }
    }

       @Test
    void dificilTieneSeisEscalerasYTresPasillos() {
        List<Conexion> conexiones = crear(Dificultad.DIFICIL, 42L).getMazmorra().getConexiones();

        assertEquals(9, conexiones.size());
        assertEquals(3, pasillos(conexiones).size());
    }

    @Test
    void losPasillosUnenLosTresTablerosEnCiclo() {
        Set<String> uniones = new HashSet<>();
        for (Conexion pasillo : pasillos(crear(Dificultad.DIFICIL, 42L).getMazmorra().getConexiones())) {
            uniones.add(pasillo.origen().tablero() + "-" + pasillo.destino().tablero());
        }

        assertEquals(Set.of("1-2", "2-3", "3-1"), uniones);
    }

    @Test
    void losPasillosEstanEnElBordeDelPisoUnoYSonTransitables() {
        Mazmorra mazmorra = crear(Dificultad.DIFICIL, 42L).getMazmorra();

        for (Conexion pasillo : pasillos(mazmorra.getConexiones())) {
            for (Ubicacion punta : List.of(pasillo.origen(), pasillo.destino())) {
                assertEquals(1, punta.piso());
                Tablero tablero = mazmorra.getPlano(punta).getTablero();
                Posicion p = punta.posicion();
                assertTrue(p.fila() == 1 || p.fila() == tablero.getFilas(), "no está en el borde: " + punta);
                assertTrue(tablero.esTransitable(p));
            }
        }
    }

    @Test
    void medioNoTienePasillos() {
        assertTrue(pasillos(crear(Dificultad.MEDIO, 42L).getMazmorra().getConexiones()).isEmpty());
    }

    @Test
    void laMismaSemillaPoneLosMismosPasillos() {
        assertEquals(crear(Dificultad.DIFICIL, 7L).getMazmorra().getConexiones(),
                crear(Dificultad.DIFICIL, 7L).getMazmorra().getConexiones());
    }

    @Test
    void dificilSeArmaConDistintasSemillas() {
        for (long semilla = 1; semilla <= 10; semilla++) {
            assertEquals(3, pasillos(crear(Dificultad.DIFICIL, semilla).getMazmorra().getConexiones()).size());
        }
    }

    /** Las conexiones entre tableros distintos (las escaleras son del mismo tablero). */
    private static List<Conexion> pasillos(List<Conexion> conexiones) {
        List<Conexion> pasillos = new ArrayList<>();
        for (Conexion conexion : conexiones) {
            if (conexion.origen().tablero() != conexion.destino().tablero()) {
                pasillos.add(conexion);
            }
        }
        return pasillos;
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

        @Test
    void elCatalogoTieneLosCincoTiposDeMonstruo() {
        CatalogoDeMonstruos catalogo = CreadorPartida.crearCatalogoDeMonstruos(new Random(1));

        assertEquals(5, catalogo.cantidadDeTipos());
        for (String tipo : List.of("ERRANTE", "CAZADOR", "DORMILON", "FANTASMA", "LADRON")) {
            assertTrue(catalogo.estaRegistrado(tipo), tipo);
        }
    }

    @Test
    void elCatalogoTieneLosCofresTerminados() {
        CatalogoDeCofres catalogo = CreadorPartida.crearCatalogoDeCofres();

        for (String tipo : List.of("PUNTOS", "ZAPATOS_DE_AGUA", "BOTAS_IGNIFUGAS",
                "POCION", "TRAMPA_DE_PINCHOS", "VENENO")) {
            assertTrue(catalogo.estaRegistrado(tipo), tipo);
        }
    }

    @Test
    void conVariasSemillasAparecenDistintosTiposDeMonstruo() {
        Set<String> tipos = new HashSet<>();
        for (long semilla = 1; semilla <= 10; semilla++) {
            for (Entidad entidad : crearFacil(semilla).getPlanoActual().getEntidades()) {
                if (entidad.getSimbolo() == 'M') {
                    tipos.add(entidad.getClass().getSimpleName());
                }
            }
        }

        assertTrue(tipos.size() > 1, "Siempre sale el mismo tipo: " + tipos);
    }

    
}
