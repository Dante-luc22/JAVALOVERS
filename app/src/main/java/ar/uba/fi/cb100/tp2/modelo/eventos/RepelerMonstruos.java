package ar.uba.fi.cb100.tp2.modelo.eventos;


import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;


public record RepelerMonstruos(int tablero, int piso) implements Evento {

    public RepelerMonstruos {
        if (tablero < 1 || piso < 1) {
            throw new IllegalArgumentException("Tablero y piso empiezan en 1");
        }
    }

    @Override
    public void ejecutar(Partida partida) {
        for (Entidad entidad : partida.getMazmorra().getPlano(this.tablero, this.piso).getEntidades()) {
            entidad.repeler();
        }
    }
}
