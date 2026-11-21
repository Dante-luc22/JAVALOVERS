package ar.uba.fi.cb100.tp2.modelo.mundo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

public class ConexionTest {

    private static final Ubicacion PISO_1 = new Ubicacion(1, 1, new Posicion(3, 4));
    private static final Ubicacion PISO_2 = new Ubicacion(1, 2, new Posicion(3, 4));

    @Test
    void invertidaVaDelDestinoAlOrigen() {
        Conexion escalera = new Conexion(PISO_1, PISO_2);

        assertEquals(new Conexion(PISO_2, PISO_1), escalera.invertida());
    }

    @Test
    void dosConexionesConLasMismasPuntasSonIguales() {
        assertEquals(new Conexion(PISO_1, PISO_2), new Conexion(PISO_1, PISO_2));
        assertEquals(new Conexion(PISO_1, PISO_2).hashCode(), new Conexion(PISO_1, PISO_2).hashCode());
    }

    @Test
    void rechazaPuntasNulas() {
        assertThrows(IllegalArgumentException.class, () -> new Conexion(null, PISO_2));
        assertThrows(IllegalArgumentException.class, () -> new Conexion(PISO_1, null));
    }

    @Test
    void rechazaUnirUnPlanoConsigoMismo() {
        Ubicacion otraCeldaDelPiso1 = new Ubicacion(1, 1, new Posicion(5, 5));

        assertThrows(IllegalArgumentException.class, () -> new Conexion(PISO_1, otraCeldaDelPiso1));
    }
}