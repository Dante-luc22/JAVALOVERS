
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeEscudo;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePico;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeRepelente;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeSomnifero;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Cazador;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Dormilon;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Errante;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Fantasma;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Ladron;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Monstruo;
import ar.uba.fi.cb100.tp2.modelo.partida.AzarPersistible;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EntidadDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PosicionDTO;

import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RestauradorEntidadTest {

    private final RestauradorEntidad restaurador =
            new RestauradorEntidad();

    private final AzarPersistible azar =
            new AzarPersistible(12345L);

    @Test
    void restauraCazadorConPosicionActualEInicial() {

        Posicion actual = new Posicion(5, 7);
        Posicion inicial = new Posicion(2, 3);

        EntidadDTO dto = crearDatosMonstruo(
                "Cazador", actual, inicial, 30
        );

        Monstruo restaurado = (Monstruo)
                restaurador.restaurar(dto, azar);

        assertDatosComunes(restaurado, actual, inicial, 30);

        assertEquals(Cazador.class, restaurado.getClass());
    }
    @Test
    void restauraLosCuatroNuevosCofres() {
        Posicion posicion = new Posicion(2, 3);

        assertCofreRestaurado("CofreDeEscudo", CofreDeEscudo.class, posicion);
        assertCofreRestaurado("CofreDePico", CofreDePico.class, posicion);
        assertCofreRestaurado("CofreDeRepelente", CofreDeRepelente.class, posicion);
        assertCofreRestaurado("CofreDeSomnifero", CofreDeSomnifero.class, posicion);
    }

    @Test
    void restauraCazadorConservaCantidadDeAdormecimientos() {

        Posicion actual = new Posicion(5, 7);
        Posicion inicial = new Posicion(2, 3);

        EntidadDTO dto = crearDatosMonstruo(
                "Cazador", actual, inicial, 30
        );

        dto.datos.addProperty("adormecimientos", 3);

        Monstruo restaurado = (Monstruo)
                restaurador.restaurar(dto, azar);

        assertEquals(3, restaurado.getAdormecimientos());
        assertTrue(restaurado.estaAdormecido());
    }

    @Test
    void restauraDormilonConRadio() {

        Posicion actual = new Posicion(4, 6);
        Posicion inicial = new Posicion(2, 2);

        EntidadDTO dto = crearDatosMonstruo(
                "Dormilon", actual, inicial, 30
        );

        dto.datos.addProperty("radio", 3);

        Monstruo restaurado = (Monstruo)
                restaurador.restaurar(dto, azar);

        assertDatosComunes(restaurado, actual, inicial, 30);
        assertEquals(Dormilon.class, restaurado.getClass());
        assertEquals(3, ((Dormilon) restaurado).getRadio());
    }

    @Test
    void restauraErranteConPosicionesYDanio() {

        Posicion actual = new Posicion(6, 5);
        Posicion inicial = new Posicion(2, 4);

        EntidadDTO dto = crearDatosMonstruo(
                "Errante", actual, inicial, 25
        );

        Monstruo restaurado = (Monstruo)
                restaurador.restaurar(dto, azar);

        assertDatosComunes(restaurado, actual, inicial, 25);

        assertEquals(Errante.class, restaurado.getClass());
    }

    @Test
    void restauraFantasmaConEstadoDeDescanso() {

        Posicion actual = new Posicion(7, 6);
        Posicion inicial = new Posicion(3, 3);

        EntidadDTO dto = crearDatosMonstruo(
                "Fantasma", actual, inicial, 30
        );

        dto.datos.addProperty("descansaEsteTurno", false);

        Monstruo restaurado = (Monstruo)
                restaurador.restaurar(dto, azar);

        assertDatosComunes(restaurado, actual, inicial, 30);

        assertEquals(Fantasma.class, restaurado.getClass());

        assertFalse(
                ((Fantasma) restaurado).isDescansaEsteTurno()
        );
    }

    @Test
    void restauraLadronSinConfundirloConErrante() {

        Posicion actual = new Posicion(5, 8);
        Posicion inicial = new Posicion(1, 2);

        EntidadDTO dto = crearDatosMonstruo(
                "Ladron", actual, inicial, 30
        );

        Monstruo restaurado = (Monstruo)
                restaurador.restaurar(dto, azar);

        assertDatosComunes(restaurado, actual, inicial, 30);

        assertEquals(Ladron.class, restaurado.getClass());
    }

    @Test
    void rechazaUnMonstruoSinPosicionInicial() {

        EntidadDTO dto = new EntidadDTO();

        dto.tipo = "Cazador";
        dto.posicion = crearPosicionDTO(new Posicion(3, 4));
        dto.datos = new JsonObject();

        dto.datos.addProperty("danio", 30);

        assertThrows(
                IllegalArgumentException.class,
                () -> restaurador.restaurar(dto, azar)
        );
    }

    private EntidadDTO crearDatosMonstruo(
            String tipo,
            Posicion actual,
            Posicion inicial,
            int danio) {

        EntidadDTO dto = new EntidadDTO();

        dto.tipo = tipo;
        dto.posicion = crearPosicionDTO(actual);
        dto.datos = new JsonObject();

        dto.datos.addProperty("filaInicial", inicial.fila());
        dto.datos.addProperty("columnaInicial", inicial.columna());
        dto.datos.addProperty("danio", danio);

        return dto;
    }

    private PosicionDTO crearPosicionDTO(Posicion posicion) {

        PosicionDTO dto = new PosicionDTO();

        dto.fila = posicion.fila();
        dto.columna = posicion.columna();

        return dto;
    }

    private void assertDatosComunes(
            Monstruo monstruo,
            Posicion actual,
            Posicion inicial,
            int danio) {

        assertNotNull(monstruo);

        assertEquals(actual, monstruo.getPosicion());
        assertEquals(inicial, monstruo.getPosicionInicial());
        assertEquals(danio, monstruo.getDanio());
    }
    private void assertCofreRestaurado(String tipo, Class<? extends Entidad> clase, Posicion posicion) {
        EntidadDTO dto = new EntidadDTO();
        dto.tipo = tipo;
        dto.posicion = crearPosicionDTO(posicion);
        dto.datos = new JsonObject();

        Entidad restaurado = restaurador.restaurar(dto);

        assertEquals(clase, restaurado.getClass());
        assertEquals(posicion, restaurado.getPosicion());
    }
}
