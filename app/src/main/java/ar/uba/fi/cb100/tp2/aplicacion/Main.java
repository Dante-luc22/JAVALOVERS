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
        VistaGrafica vistaGrafica = null;

        try {
            Configuracion configuracion = LectorConfiguracion.leer(LectorConfiguracion.RUTA_POR_DEFECTO);
            Dificultad dificultad = elegirDificultad(entrada);
            int opcionVista = elegirVista(entrada);
            CreadorPartida creador = new CreadorPartida(configuracion, new GeneradorGrupoNN());
            Partida partida = creador.crear(dificultad);
            Vista vista;
            if (opcionVista == 1) {
                vista = new VistaBmp(
                        CARPETA_BMP,
                        configuracion.energiaInicial(),
                        entrada);
            } else {
                vistaGrafica = new VistaGrafica();
                vista = vistaGrafica;
            }

            EstadoPartida fin = new ControladorJuego(partida, vista).jugar();
            mostrarFinal(fin, partida, opcionVista == 1);
        } catch (IllegalStateException e) {
            System.out.println("No se pudo arrancar el juego: " + e.getMessage());
        } finally {
            if (vistaGrafica != null) {
                vistaGrafica.close();
            }
            entrada.cerrar();
        }
    }

    /**
     * Muestra las dificultades numeradas y devuelve la elegida.
     */
    private static Dificultad elegirDificultad(EntradaConsola entrada) {
        Dificultad[] opciones = Dificultad.values();
        System.out.println("Elegí la dificultad:");
        for (int i = 0; i < opciones.length; i++) {
            System.out.println("  " + (i + 1) + ") " + opciones[i]);
        }
        int elegida = entrada.leerEnteroEnRango("Opción:", 1, opciones.length);
        return opciones[elegida - 1];
    }
    private static int elegirVista(EntradaConsola entrada) {
        System.out.println("Elegí cómo querés jugar:");
        System.out.println("  1) Vista BMP (consola)");
        System.out.println("  2) Vista gráfica (ventana)");

        return entrada.leerEnteroEnRango("Opción:", 1, 2);
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