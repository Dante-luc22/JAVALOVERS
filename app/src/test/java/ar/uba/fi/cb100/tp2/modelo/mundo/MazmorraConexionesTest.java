package ar.uba.fi.cb100.tp2.modelo.mundo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de las conexiones de la Mazmorra, con un tablero de tres pisos
 * como el de Medio. Cada piso es un 6x6 con el interior de piso.
 */
class MazmorraConexionesTest {

    private static final Posicion ESCALERA_1_2 = new Posicion(3, 3);
    private static final Posicion ESCALERA_2_3 = new Posicion(4, 4);

    private Mazmorra mazmorra;

    @BeforeEach
    void armarTresPisos() {
        List<Plano> pisos = new ArrayList<>();
        for (int piso = 1; piso <= 3; piso++) {
            Tablero tablero = new Tablero(6, 6);
            for (int fila = 2; fila <= 5; fila++) {
                for (int columna = 2; columna <= 5; columna++) {
                    tablero.setTerreno(new Posicion(fila, columna), Terreno.PISO);
                }
            }
            Posicion salida = (piso == 3) ? new Posicion(5, 5) : null;
            pisos.add(new Plano(tablero, new Posicion(2, 2), salida));
        }
        this.mazmorra = new Mazmorra(pisos, 3);
    }

    private static Ubicacion en(int piso, Posicion posicion) {
        return new Ubicacion(1, piso, posicion);
    }

    @Test
    void sinConexionesNingunaCeldaLlevaANingunLado() {
        assertEquals(Optional.empty(), this.mazmorra.destinoDesde(en(1, ESCALERA_1_2)));
        assertTrue(this.mazmorra.getConexiones().isEmpty());
    }

    @Test
    void conectarLlevaDelPrimerPisoAlSegundo() {
        this.mazmorra.conectar(en(1, ESCALERA_1_2), en(2, ESCALERA_1_2));

        assertEquals(Optional.of(en(2, ESCALERA_1_2)), this.mazmorra.destinoDesde(en(1, ESCALERA_1_2)));
    }

    @Test
    void conectarTambienRegistraLaVuelta() {
        this.mazmorra.conectar(en(1, ESCALERA_1_2), en(2, ESCALERA_1_2));

        assertEquals(Optional.of(en(1, ESCALERA_1_2)), this.mazmorra.destinoDesde(en(2, ESCALERA_1_2)));
    }

    @Test
    void otraCeldaDelMismoPisoNoLlevaANingunLado() {
        this.mazmorra.conectar(en(1, ESCALERA_1_2), en(2, ESCALERA_1_2));

        assertEquals(Optional.empty(), this.mazmorra.destinoDesde(en(1, new Posicion(2, 2))));
    }

    @Test
    void getConexionesDevuelveCadaEscaleraUnaSolaVezEnOrden() {
        this.mazmorra.conectar(en(1, ESCALERA_1_2), en(2, ESCALERA_1_2));
        this.mazmorra.conectar(en(2, ESCALERA_2_3), en(3, ESCALERA_2_3));

        assertEquals(List.of(
                new Conexion(en(1, ESCALERA_1_2), en(2, ESCALERA_1_2)),
                new Conexion(en(2, ESCALERA_2_3), en(3, ESCALERA_2_3))),
                this.mazmorra.getConexiones());
    }

    @Test
    void elPisoDelMedioTieneLasDosEscaleras() {
        this.mazmorra.conectar(en(1, ESCALERA_1_2), en(2, ESCALERA_1_2));
        this.mazmorra.conectar(en(2, ESCALERA_2_3), en(3, ESCALERA_2_3));

        List<Conexion> delPiso2 = this.mazmorra.conexionesDelPlano(1, 2);

        assertEquals(2, delPiso2.size());
        assertTrue(delPiso2.contains(new Conexion(en(2, ESCALERA_1_2), en(1, ESCALERA_1_2))));
        assertTrue(delPiso2.contains(new Conexion(en(2, ESCALERA_2_3), en(3, ESCALERA_2_3))));
        assertEquals(1, this.mazmorra.conexionesDelPlano(1, 1).size());
    }

    @Test
    void rearmarConGetConexionesDaLasMismasConexiones() {
        this.mazmorra.conectar(en(1, ESCALERA_1_2), en(2, ESCALERA_1_2));
        this.mazmorra.conectar(en(2, ESCALERA_2_3), en(3, ESCALERA_2_3));
        List<Conexion> guardadas = this.mazmorra.getConexiones();

        armarTresPisos();
        for (Conexion conexion : guardadas) {
            this.mazmorra.conectar(conexion.origen(), conexion.destino());
        }

        assertEquals(guardadas, this.mazmorra.getConexiones());
        assertEquals(Optional.of(en(1, ESCALERA_1_2)), this.mazmorra.destinoDesde(en(2, ESCALERA_1_2)));
    }

    @Test
    void getConexionesEsUnaCopia() {
        this.mazmorra.conectar(en(1, ESCALERA_1_2), en(2, ESCALERA_1_2));

        this.mazmorra.getConexiones().clear();

        assertEquals(1, this.mazmorra.getConexiones().size());
    }

    @Test
    void rechazaUnaPuntaEnPared() {
        Ubicacion pared = en(1, new Posicion(1, 1));

        assertThrows(IllegalArgumentException.class,
                () -> this.mazmorra.conectar(pared, en(2, ESCALERA_1_2)));
    }

    @Test
    void rechazaUnPisoQueNoExiste() {
        assertThrows(IllegalArgumentException.class,
                () -> this.mazmorra.conectar(en(3, ESCALERA_2_3), en(4, ESCALERA_2_3)));
    }

    @Test
    void rechazaUnaCeldaQueYaTieneConexion() {
        this.mazmorra.conectar(en(1, ESCALERA_1_2), en(2, ESCALERA_1_2));

        assertThrows(IllegalArgumentException.class,
                () -> this.mazmorra.conectar(en(2, ESCALERA_1_2), en(3, ESCALERA_1_2)));
    }

    @Test
    void siUnaPuntaFallaNoQuedaNadaRegistrado() {
        Ubicacion pared = en(2, new Posicion(1, 1));

        assertThrows(IllegalArgumentException.class,
                () -> this.mazmorra.conectar(en(1, ESCALERA_1_2), pared));

        assertEquals(Optional.empty(), this.mazmorra.destinoDesde(en(1, ESCALERA_1_2)));
        assertTrue(this.mazmorra.getConexiones().isEmpty());
    }

    @Test
    void rechazaUnirDosCeldasDelMismoPiso() {
        assertThrows(IllegalArgumentException.class,
                () -> this.mazmorra.conectar(en(1, ESCALERA_1_2), en(1, ESCALERA_2_3)));
    }
}
