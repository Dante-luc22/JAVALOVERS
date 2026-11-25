package ar.uba.fi.cb100.tp2.aplicacion;

import java.nio.file.Path;

import ar.uba.fi.cb100.tp2.generador.GeneradorGrupoNN;
import ar.uba.fi.cb100.tp2.modelo.partida.Configuracion;
import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;
import ar.uba.fi.cb100.tp2.persistencia.LectorConfiguracion;
import ar.uba.fi.cb100.tp2.vista.EntradaConsola;
import ar.uba.fi.cb100.tp2.vista.Vista;
import ar.uba.fi.cb100.tp2.vista.VistaBmp;
import ar.uba.fi.cb100.tp2.vista.VistaGrafica;
import java.awt.GraphicsEnvironment;
import ar.uba.fi.cb100.tp2.vista.MenuGrafico;
/**
 * Punto de entrada del juego. Arma las piezas y las conecta:
 * configuración → dificultad → partida → vista → controlador.
 * La lógica del juego no está acá, está en el modelo.
 */
public class Main {

    /** Carpeta donde quedan los BMP de cada paso, como pide el enunciado. */
    private static final Path CARPETA_BMP = Path.of("salida");

    public static void main(String[] args) {
        EntradaConsola entrada = new EntradaConsola();
        boolean sinPantalla = GraphicsEnvironment.isHeadless();
        MenuGrafico menu = new MenuGrafico();

        try {
            Configuracion configuracion =
                    LectorConfiguracion.leer(LectorConfiguracion.RUTA_POR_DEFECTO);

            Dificultad dificultad;
            boolean usarBmp;

            if (sinPantalla) {
                dificultad = elegirDificultad(entrada);
                usarBmp = true;
            } else {
                var opciones = menu.mostrarInicio();
                if (opciones.isEmpty()) {
                    return;
                }

                var inicio = opciones.get();
                dificultad = inicio.dificultad();
                usarBmp = inicio.modoVista() == MenuGrafico.ModoVista.BMP;
            }

            CreadorPartida creador = new CreadorPartida(configuracion, new GeneradorGrupoNN());

            while (true) {
                Partida partida = creador.crear(dificultad);
                EstadoPartida fin = jugarPartida(
                        partida, usarBmp, configuracion.energiaInicial(), entrada);
                mostrarFinal(fin, partida, usarBmp);

                if (sinPantalla || fin == EstadoPartida.EN_CURSO
                        || !menu.mostrarResultado(fin, partida.puntaje())) {
                    break;
                }
            }
        } catch (IllegalStateException e) {
            System.out.println("No se pudo ejecutar el juego: " + e.getMessage());
        } finally {
            entrada.cerrar();
        }
    }

    private static EstadoPartida jugarPartida(
            Partida partida, boolean usarBmp, int energiaInicial, EntradaConsola entrada) {

        if (usarBmp) {
            Vista vista = new VistaBmp(CARPETA_BMP, energiaInicial, entrada);
            return new ControladorJuego(partida, vista).jugar();
        }

        try (VistaGrafica vista = new VistaGrafica()) {
            return new ControladorJuego(partida, vista).jugar();
        }
    }

    /**
     * Muestra las dificultades numeradas y devuelve la elegida.
     */
    private static Dificultad elegirDificultad(EntradaConsola entrada) {
        Dificultad[] opciones = Dificultad.values();
        System.out.println("Elegi la dificultad:");
        for (int i = 0; i < opciones.length; i++) {
            System.out.println("  " + (i + 1) + ") " + opciones[i]);
        }
        int elegida = entrada.leerEnteroEnRango("Opcion:", 1, opciones.length);
        return opciones[elegida - 1];
    }

    private static void mostrarFinal(EstadoPartida fin, Partida partida, boolean mostrarBmp) {
        switch (fin) {
            case GANADA -> System.out.println("¡Ganaste! Encontraste la salida.");
            case PERDIDA -> System.out.println("Perdiste: te quedaste sin energía.");
            case EN_CURSO -> System.out.println("Saliste de la partida.");
        }
        if (fin != EstadoPartida.EN_CURSO) {
            System.out.println("Puntaje: " + partida.puntaje());
        }
        if (mostrarBmp) {
            System.out.println("Los BMP de cada paso quedaron en " + CARPETA_BMP.toAbsolutePath());
        }
    }
}