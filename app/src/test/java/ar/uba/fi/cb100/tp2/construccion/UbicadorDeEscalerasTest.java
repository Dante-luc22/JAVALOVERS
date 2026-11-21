package ar.uba.fi.cb100.tp2.construccion;

import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePuntos;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UbicadorDeEscalerasTest {

    private final UbicadorDeEscaleras ubicador = new UbicadorDeEscaleras();

    @Test
    void eligeUnaCeldaTransitableYLibreEnAmbosPisos() {
        Posicion entrada = new Posicion(2, 2);
        Posicion celdaConCofre = new Posicion(2, 3);
        Posicion pisoEnUnSoloPlano = new Posicion(2, 4);
        Posicion celdaValida = new Posicion(3, 2);

        Plano inferior = crearPlano(entrada, celdaConCofre, pisoEnUnSoloPlano, celdaValida);
        Plano superior = crearPlano(new Posicion(4, 4), entrada, celdaConCofre, celdaValida);
        superior.agregarEntidad(new CofreDePuntos(celdaConCofre));

        assertEquals(Optional.of(celdaValida), ubicador.buscarPosicionComun(inferior, superior));
    }

    @Test
    void devuelveVacioSiNoHayUnaCeldaTransitableComun() {
        Plano inferior = crearPlano(new Posicion(2, 2));
        Plano superior = crearPlano(new Posicion(2, 3));

        assertTrue(ubicador.buscarPosicionComun(inferior, superior).isEmpty());
    }

    private Plano crearPlano(Posicion entrada, Posicion... pisos) {
        Tablero tablero = new Tablero(5, 5);
        tablero.setTerreno(entrada, Terreno.PISO);
        for (Posicion piso : pisos) {
            tablero.setTerreno(piso, Terreno.PISO);
        }
        return new Plano(tablero, entrada, null);
    }

    @Test
    void abreUnaParedConectadaAlCaminoDelOtroPiso() {
        Posicion aislada = new Posicion(2, 3);
        Posicion paredQueConecta = new Posicion(3, 4);

        Plano inferior = crearPlano(new Posicion(2, 2),
                aislada, new Posicion(3, 3), paredQueConecta);
        Plano superior = crearPlano(new Posicion(4, 4));

        assertEquals(Optional.of(paredQueConecta),
                ubicador.prepararPosicionComun(inferior, superior));
        assertEquals(Terreno.PISO, superior.getTablero().getTerreno(paredQueConecta));
        assertEquals(Terreno.PARED, superior.getTablero().getTerreno(aislada));
    }
}