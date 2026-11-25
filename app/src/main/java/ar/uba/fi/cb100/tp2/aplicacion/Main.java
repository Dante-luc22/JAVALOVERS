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
import ar.uba.fi.cb100.tp2.persistencia.ServicioPersistenciaPartida;

/**
 * Punto de entrada del juego. Arma las piezas y las conecta:
 * configuración → dificultad → partida → vista → controlador.
 *
 * La lógica del juego no está acá, está en el modelo.
 */
public class Main {

    /** Carpeta donde quedan los BMP de cada paso, como pide el enunciado. */
    private static final Path CARPETA_BMP = Path.of("salida");

    /** Archivo donde se guarda y desde donde se recupera la partida. */
    private static final Path RUTA_GUARDADO = Path.of("partida.json");

    public static void main(String[] args) {

        EntradaConsola entrada = new EntradaConsola();

        boolean sinPantalla = GraphicsEnvironment.isHeadless();

        MenuGrafico menu = new MenuGrafico();

        try {

            Configuracion configuracion =
                    LectorConfiguracion.leer(
                            LectorConfiguracion.RUTA_POR_DEFECTO
                    );

            Dificultad dificultad;
            boolean usarBmp;
            boolean cargarGuardada;

            if (sinPantalla) {

                // Inicio mediante consola.
                cargarGuardada = elegirCargarGuardada(entrada);

                usarBmp = true;

                dificultad = cargarGuardada
                        ? Dificultad.FACIL
                        : elegirDificultad(entrada);

            } else {

                // Inicio mediante menú gráfico.
                var opciones = menu.mostrarInicio();

                if (opciones.isEmpty()) {
                    return;
                }

                var inicio = opciones.get();

                dificultad = inicio.dificultad();

                usarBmp = inicio.modoVista()
                        == MenuGrafico.ModoVista.BMP;

                cargarGuardada = inicio.cargarGuardada();
            }

            // Componentes compartidos durante toda la ejecución.
            CreadorPartida creador = new CreadorPartida(
                    configuracion,
                    new GeneradorGrupoNN()
            );

            ServicioPersistenciaPartida servicioPersistencia =
                    new ServicioPersistenciaPartida();

            while (true) {

                Partida partida;

                if (cargarGuardada) {

                    // Recuperar la partida existente.
                    System.out.println(
                            "Cargando partida guardada..."
                    );

                    partida = servicioPersistencia.cargar(
                            RUTA_GUARDADO
                    );

                    // La próxima partida, si se reintenta, será nueva.
                    cargarGuardada = false;

                } else {

                    // Crear una partida desde cero.
                    partida = creador.crear(dificultad);
                }

                EstadoPartida fin = jugarPartida(
                        partida,
                        usarBmp,
                        configuracion.energiaInicial(),
                        entrada,
                        servicioPersistencia
                );

                mostrarFinal(fin, partida, usarBmp);

                if (sinPantalla
                        || fin == EstadoPartida.EN_CURSO
                        || !menu.mostrarResultado(
                        fin,
                        partida.puntaje()
                )) {

                    break;
                }
            }

        } catch (IllegalStateException e) {

            System.out.println(
                    "No se pudo ejecutar el juego: "
                            + e.getMessage()
            );

        } finally {

            entrada.cerrar();
        }
    }

    /**
     * Ejecuta una partida utilizando la vista seleccionada.
     *
     * La persistencia se inyecta en el controlador y es independiente
     * de la implementación gráfica o BMP.
     */
    private static EstadoPartida jugarPartida(
            Partida partida,
            boolean usarBmp,
            int energiaInicial,
            EntradaConsola entrada,
            ServicioPersistenciaPartida servicioPersistencia) {

        if (usarBmp) {

            Vista vista = new VistaBmp(
                    CARPETA_BMP,
                    energiaInicial,
                    entrada
            );

            return new ControladorJuego(
                    partida,
                    vista,
                    servicioPersistencia,
                    RUTA_GUARDADO
            ).jugar();
        }

        try (VistaGrafica vista = new VistaGrafica()) {

            return new ControladorJuego(
                    partida,
                    vista,
                    servicioPersistencia,
                    RUTA_GUARDADO
            ).jugar();
        }
    }

    /**
     * Permite elegir entre comenzar una partida nueva
     * o cargar una partida previamente guardada.
     *
     * @return true si se eligió cargar una partida guardada
     */
    private static boolean elegirCargarGuardada(
            EntradaConsola entrada) {

        System.out.println("¿Cómo querés comenzar?");
        System.out.println("  1) Nueva partida");
        System.out.println("  2) Cargar partida guardada");

        int opcion = entrada.leerEnteroEnRango(
                "Opción:",
                1,
                2
        );

        return opcion == 2;
    }

    /**
     * Muestra las dificultades numeradas y devuelve la elegida.
     */
    private static Dificultad elegirDificultad(
            EntradaConsola entrada) {

        Dificultad[] opciones = Dificultad.values();

        System.out.println("Elegí la dificultad:");

        for (int i = 0; i < opciones.length; i++) {

            System.out.println(
                    "  " + (i + 1) + ") " + opciones[i]
            );
        }

        int elegida = entrada.leerEnteroEnRango(
                "Opción:",
                1,
                opciones.length
        );

        return opciones[elegida - 1];
    }

    /**
     * Muestra el resultado final de la partida.
     */
    private static void mostrarFinal(
            EstadoPartida fin,
            Partida partida,
            boolean mostrarBmp) {

        switch (fin) {

            case GANADA ->
                    System.out.println(
                            "¡Ganaste! Encontraste la salida."
                    );

            case PERDIDA ->
                    System.out.println(
                            "Perdiste: te quedaste sin energía."
                    );

            case EN_CURSO ->
                    System.out.println(
                            "Saliste de la partida."
                    );
        }

        if (fin != EstadoPartida.EN_CURSO) {

            System.out.println(
                    "Puntaje: " + partida.puntaje()
            );
        }

        if (mostrarBmp) {

            System.out.println(
                    "Los BMP de cada paso quedaron en "
                            + CARPETA_BMP.toAbsolutePath()
            );
        }
    }
}