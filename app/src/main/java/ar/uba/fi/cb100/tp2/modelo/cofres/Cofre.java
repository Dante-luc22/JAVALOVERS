package ar.uba.fi.cb100.tp2.modelo.cofres;

import ar.uba.fi.cb100.tp2.modelo.eventos.Agenda;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

public abstract class Cofre extends Entidad {

    private boolean abierto = false;  

    /**
     * Constructor de la clase cofre, recibe la posicion donde va a
     * estar el cofre.
     *
     * @param posicion posicion del cofre;
     * @throws IllegalArgumentException si la posicion es nula;
     */
    protected Cofre(Posicion posicion) {
        if (posicion == null) {
            throw new IllegalArgumentException(
                    "No se puede recibir una posicion nula.");
        }
        super(posicion);
    }

    /**
     * Metodo astracto que deja que el heroe abra el cofre
     *
     * @param heroe  el que abre el cofre;
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    protected abstract void abrir(Heroe heroe, Agenda agenda);

    /**
     * Cuerpo vacio ya que un cofre no se mueve
     */
    @Override
    public final void actuar(Plano plano, Heroe heroe){}

    /**
     * Suma el cofre abierto al héroe y aplica el efecto propio
     * de cada tipo
     *
     * @param heroe  el héroe
     * @param agenda la agenda, para efectos que vencen más adelante
     */
    @Override
    public final void alEncontrarseConHeroe(Heroe heroe, Agenda agenda){
        if (heroe == null) {
            throw new IllegalArgumentException(
                    "El heroe no puede ser nulo.");
        }
        if (this.abierto) {
            return;
        }
        this.abierto = true;     
        heroe.sumarCofreAbierto();
        abrir(heroe, agenda);
    }

    /**                                                       // NUEVO (3)
     * Un cofre abierto desaparece del plano (regla 4 del enunciado).
     * @return true una vez que el héroe lo abrió
     */
    @Override
    public final boolean debeSalirDelPlano() {
        return this.abierto;
    }
}
