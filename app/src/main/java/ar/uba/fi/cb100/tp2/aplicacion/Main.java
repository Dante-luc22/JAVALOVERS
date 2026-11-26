package ar.uba.fi.cb100.tp2.aplicacion;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Random;

import ar.uba.fi.cb100.tp2.generador.GeneradorGrupoNN;
import ar.uba.fi.cb100.tp2.modelo.partida.Configuracion;
import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.ranking.EntradaDeRanking;
import ar.uba.fi.cb100.tp2.modelo.ranking.Ranking;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;
import ar.uba.fi.cb100.tp2.persistencia.ArchivoDeRanking;
import ar.uba.fi.cb100.tp2.persistencia.LectorConfiguracion;
import ar.uba.fi.cb100.tp2.vista.EntradaConsola;
import ar.uba.fi.cb100.tp2.vista.MenuGrafico;
import ar.uba.fi.cb100.tp2.vista.Vista;
import ar.uba.fi.cb100.tp2.vista.VistaBmp;
import ar.uba.fi.cb100.tp2.vista.VistaGrafica;
import java.nio.file.Files;
import javax.swing.JOptionPane;
import ar.uba.fi.cb100.tp2.persistencia.ServicioPersistenciaPartida;

/**
 * Punto de entrada del juego. Lee la configuración, crea la partida
 * y conecta la vista con el controlador.
 */
public class Main {

    /** Carpeta donde quedan los BMP de cada paso. */
    private static final Path CARPETA_BMP = Path.of("salida");
    /** Archivo donde se guarda y desde donde se recupera la partida. */
    private static final Path RUTA_GUARDADO = Path.of("partida.json");

    public static void main(String[] args) {
        EntradaConsola entrada = new EntradaConsola();
        boolean sinPantalla = GraphicsEnvironment.isHeadless();
        MenuGrafico menu = new MenuGrafico();
        ServicioPersistenciaPartida servicioPersistencia =
                new ServicioPersistenciaPartida();

        try {
            Configuracion configuracion =
                    LectorConfiguracion.leer(LectorConfiguracion.RUTA_POR_DEFECTO);

            if (sinPantalla) {
                Dificultad dificultad = elegirDificultad(entrada);
                CreadorPartida creador = new CreadorPartida(configuracion, new GeneradorGrupoNN());
                Partida partida = creador.crear(dificultad);
                EstadoPartida fin = jugarPartida(
                        partida, true, configuracion.energiaInicial(),entrada, servicioPersistencia);
                mostrarFinal(fin, partida, true);
                if (fin != EstadoPartida.EN_CURSO) {
                    ofrecerGuardarPorConsola(partida, dificultad, entrada);
                }
                return;
            }

            while (true) {
                var opciones = menu.mostrarInicio(configuracion.semilla());
                if (opciones.isEmpty()) {
                    break;
                }

                if (jugarDesdeMenu(
                        configuracion, opciones.get(), menu, entrada, servicioPersistencia)
                        == MenuGrafico.Decision.SALIR) {
                    break;
                }
            }
        } catch (IllegalStateException e) {
            System.out.println("No se pudo ejecutar el juego: " + e.getMessage());
        } finally {
            entrada.cerrar();
        }
    }

    private static MenuGrafico.Decision jugarDesdeMenu(
            Configuracion configuracion,
            MenuGrafico.OpcionesInicio opciones,
            MenuGrafico menu,
            EntradaConsola entrada,
            ServicioPersistenciaPartida servicioPersistencia) {

        Long semillaElegida = opciones.semilla();
        long semilla = semillaElegida == null ? new Random().nextLong() : semillaElegida;

        Configuracion configuracionPartida = new Configuracion(
                configuracion.energiaInicial(),
                configuracion.costos(),
                semilla,
                configuracion.dificultades()
        );

        CreadorPartida creador =
                new CreadorPartida(configuracionPartida, new GeneradorGrupoNN());
        boolean cargarGuardada = preguntarSiCargarGuardada();
        boolean usarBmp = opciones.modoVista() == MenuGrafico.ModoVista.BMP;

        MenuGrafico.Decision decision;
        do {
            Partida partida;

            if (cargarGuardada) {
                partida = servicioPersistencia.cargar(RUTA_GUARDADO);
                cargarGuardada = false;
            } else {
                partida = creador.crear(opciones.dificultad());
            }
            EstadoPartida fin = jugarPartida(
                    partida,
                    usarBmp,
                    configuracionPartida.energiaInicial(),
                    entrada,
                    servicioPersistencia);

            if (fin == EstadoPartida.EN_CURSO) {
                return MenuGrafico.Decision.MENU_PRINCIPAL;
            }
            decision = menu.mostrarResultado(fin, partida.puntaje(), semilla,
                    nombre -> guardarEnRanking(partida, opciones.dificultad(), nombre));
        } while (decision == MenuGrafico.Decision.REINTENTAR);
        return decision;
    }

    private static EstadoPartida jugarPartida(Partida partida, boolean usarBmp, int energiaInicial, EntradaConsola entrada,
            ServicioPersistenciaPartida servicioPersistencia) {

        if (usarBmp) {
            Vista vista = new VistaBmp(CARPETA_BMP, energiaInicial, entrada);
            return new ControladorJuego(partida, vista, servicioPersistencia, RUTA_GUARDADO).jugar();
        }

        try (VistaGrafica vista = new VistaGrafica()) {
            return new ControladorJuego(partida, vista, servicioPersistencia, RUTA_GUARDADO).jugar();
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

    /**
     * Pregunta si se desea recuperar una partida guardada.
     *
     * @return true si el jugador acepta cargarla
     */
    private static boolean preguntarSiCargarGuardada() {
        if (!Files.isRegularFile(RUTA_GUARDADO)) {
            return false;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                null,
                "Se encontró una partida guardada. ¿Querés cargarla?",
                "Partida guardada",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        return respuesta == JOptionPane.YES_OPTION;
    }

    private static void ofrecerGuardarPorConsola(
            Partida partida, Dificultad dificultad, EntradaConsola entrada) {
        String respuesta = entrada.leerTexto("¿Guardar tu puntaje en el ranking? (s/n):").strip();
        if (!respuesta.equalsIgnoreCase("s")) {
            return;
        }

        String nombre = entrada.leerTexto("Tu nombre para el ranking ("
                + EntradaDeRanking.LARGO_DEL_NOMBRE + " caracteres, sin espacios):").strip();
        while (!EntradaDeRanking.esNombreValido(nombre)) {
            nombre = entrada.leerTexto("El nombre tiene que tener exactamente "
                    + EntradaDeRanking.LARGO_DEL_NOMBRE + " caracteres, sin espacios:").strip();
        }
        guardarEnRanking(partida, dificultad, nombre);
    }

    private static void guardarEnRanking(Partida partida, Dificultad dificultad, String nombre) {
        Ranking ranking = ArchivoDeRanking.cargar(ArchivoDeRanking.RUTA_POR_DEFECTO);
        ranking.registrarPartida(nombre, partida.puntaje(), dificultad, LocalDateTime.now());
        ArchivoDeRanking.guardar(ranking, ArchivoDeRanking.RUTA_POR_DEFECTO);

        System.out.println("Top 10 (" + dificultad + "):");
        int lugar = 1;
        for (EntradaDeRanking mejor : ranking.obtenerTop10(dificultad)) {
            System.out.println("  " + lugar + ") " + mejor.nombre() + " - " + mejor.puntaje());
            lugar++;
        }
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
