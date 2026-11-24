package ar.uba.fi.cb100.tp2.aplicacion;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.Random;

import ar.uba.fi.cb100.tp2.generador.GeneradorGrupoNN;
import ar.uba.fi.cb100.tp2.modelo.partida.Configuracion;
import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;
import ar.uba.fi.cb100.tp2.persistencia.LectorConfiguracion;
import ar.uba.fi.cb100.tp2.vista.EntradaConsola;
import ar.uba.fi.cb100.tp2.vista.MenuGrafico;
import ar.uba.fi.cb100.tp2.vista.Vista;
import ar.uba.fi.cb100.tp2.vista.VistaBmp;
import ar.uba.fi.cb100.tp2.vista.VistaGrafica;

/**
 * Punto de entrada del juego. Lee la configuración, crea la partida
 * y conecta la vista con el controlador.
 */
public class Main {

    /** Carpeta donde quedan los BMP de cada paso. */
    private static final Path CARPETA_BMP = Path.of("salida");

    public static void main(String[] args) {
        EntradaConsola entrada = new EntradaConsola();
        boolean sinPantalla = GraphicsEnvironment.isHeadless();
        MenuGrafico menu = new MenuGrafico();

        try {
            Configuracion configuracion =
                    LectorConfiguracion.leer(LectorConfiguracion.RUTA_POR_DEFECTO);

            if (sinPantalla) {
                Dificultad dificultad = elegirDificultad(entrada);
                CreadorPartida creador = new CreadorPartida(configuracion, new GeneradorGrupoNN());
                Partida partida = creador.crear(dificultad);
                EstadoPartida fin = jugarPartida(partida, true, configuracion.energiaInicial(), entrada);
                mostrarFinal(fin, partida, true);
                return;
            }

            while (true) {
                var opciones = menu.mostrarInicio(configuracion.semilla());
                if (opciones.isEmpty()) {
                    break;
                }
                jugarDesdeMenu(configuracion, opciones.get(), menu, entrada);
            }
        } catch (IllegalStateException e) {
            System.out.println("No se pudo ejecutar el juego: " + e.getMessage());
        } finally {
            entrada.cerrar();
        }
    }

    private static void jugarDesdeMenu(
            Configuracion configuracion, MenuGrafico.OpcionesInicio opciones,
            MenuGrafico menu, EntradaConsola entrada) {

        Long semillaElegida = opciones.semilla();
        long semilla = semillaElegida == null ? new Random().nextLong() : semillaElegida;
        Configuracion configuracionPartida = new Configuracion(
                configuracion.energiaInicial(), configuracion.costos(),
                semilla, configuracion.dificultades()
        );
        CreadorPartida creador = new CreadorPartida(configuracionPartida, new GeneradorGrupoNN());
        boolean usarBmp = opciones.modoVista() == MenuGrafico.ModoVista.BMP;

        Partida partida;
        EstadoPartida fin;
        do {
            partida = creador.crear(opciones.dificultad());
            fin = jugarPartida(partida, usarBmp, configuracionPartida.energiaInicial(), entrada);
            mostrarFinal(fin, partida, usarBmp);
        } while (fin != EstadoPartida.EN_CURSO && menu.mostrarResultado(fin, partida.puntaje()));
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

    /** Muestra las dificultades numeradas y devuelve la elegida. */
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
