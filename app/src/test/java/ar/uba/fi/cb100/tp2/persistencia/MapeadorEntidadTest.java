
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.monstruos.Cazador;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Dormilon;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Errante;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Fantasma;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Ladron;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EntidadDTO;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MapeadorEntidadTest {

    private final MapeadorEntidad mapeador =
            new MapeadorEntidad();

    @Test
    void mapeaCazadorConSusDatosComunes() {

        Posicion posicion = new Posicion(2, 3);

        EntidadDTO dto = mapeador.mapear(
                new Cazador(posicion, 30)
        );

        assertDatosComunes(
                dto, "Cazador", posicion, posicion, 30
        );
    }

    @Test
    void mapeaMonstruoConservaCantidadDeAdormecimientos() {

        Posicion posicion = new Posicion(2, 3);
        Cazador monstruo = new Cazador(posicion, 30);

        monstruo.adormecer();
        monstruo.adormecer();
        monstruo.adormecer();

        EntidadDTO dto = mapeador.mapear(monstruo);

        assertEquals(3, dto.datos.get("adormecimientos").getAsInt());
    }

    @Test
    void mapeaDormilonConSuRadio() {

        Posicion posicion = new Posicion(3, 4);

        EntidadDTO dto = mapeador.mapear(
                new Dormilon(posicion, 30, 3)
        );

        assertDatosComunes(
                dto, "Dormilon", posicion, posicion, 30
        );

        assertEquals(3, dto.datos.get("radio").getAsInt());
    }

    @Test
    void mapeaErranteConSusDatosComunes() {

        Posicion posicion = new Posicion(4, 5);

        EntidadDTO dto = mapeador.mapear(
                new Errante(posicion, 30, new Random(42))
        );

        assertDatosComunes(
                dto, "Errante", posicion, posicion, 30
        );
    }

    @Test
    void mapeaFantasmaConSuEstadoDeDescanso() {

        Posicion posicion = new Posicion(5, 6);

        EntidadDTO dto = mapeador.mapear(
                new Fantasma(posicion, 30)
        );

        assertDatosComunes(
                dto, "Fantasma", posicion, posicion, 30
        );

        assertTrue(
                dto.datos.get("descansaEsteTurno").getAsBoolean()
        );
    }

    @Test
    void mapeaLadronSinConfundirloConErrante() {

        Posicion posicion = new Posicion(6, 7);

        EntidadDTO dto = mapeador.mapear(
                new Ladron(posicion, 30, new Random(42))
        );

        assertDatosComunes(
                dto, "Ladron", posicion, posicion, 30
        );
    }

    private void assertDatosComunes(
            EntidadDTO dto,
            String tipo,
            Posicion posicionActual,
            Posicion posicionInicial,
            int danio) {

        assertNotNull(dto);
        assertEquals(tipo, dto.tipo);

        assertEquals(
                posicionActual.fila(),
                dto.posicion.fila
        );

        assertEquals(
                posicionActual.columna(),
                dto.posicion.columna
        );

        assertEquals(
                posicionInicial.fila(),
                dto.datos.get("filaInicial").getAsInt()
        );

        assertEquals(
                posicionInicial.columna(),
                dto.datos.get("columnaInicial").getAsInt()
        );

        assertEquals(
                danio,
                dto.datos.get("danio").getAsInt()
        );
    }
}
