
package ar.uba.fi.cb100.tp2.vista;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ar.uba.fi.cb100.librerias.JuegoVisual;
import ar.uba.fi.cb100.librerias.Tecla;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.partida.Accion;
import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Vista gráfica del juego.
 *
 * Utiliza JuegoVisual para representar el tablero, el héroe
 * y las entidades de la partida.
 */
public class VistaGrafica implements Vista, AutoCloseable {

    private static final int TAMANIO_CELDA = 48;
    private static final String NOMBRE_HEROE = "heroe";

    private final Path carpetaImagenes;

    private JuegoVisual juego;
    private Plano planoMostrado;

    /*
     * Relaciona cada entidad del modelo con su personaje gráfico.
     * Se utiliza identidad porque cada entidad concreta es un objeto
     * independiente del modelo.
     */
    private final Map<Entidad, String> personajes =
            new IdentityHashMap<>();

    private int siguienteId = 1;

    public VistaGrafica() {
        this(buscarCarpetaImagenes());
    }

    public VistaGrafica(Path carpetaImagenes) {
        if (carpetaImagenes == null ||
                !Files.isRegularFile(carpetaImagenes.resolve("heroe.png"))) {
            throw new IllegalArgumentException(
                    "No se encontró la carpeta de imágenes: " + carpetaImagenes);
        }

        this.carpetaImagenes = carpetaImagenes;
    }

    /**
     * Muestra o actualiza el estado de la partida.
     */
    @Override
    public void mostrar(Partida partida) {
        Plano plano = partida.getPlanoActual();

        // Primera llamada o cambio de plano.
        if (this.juego == null || plano != this.planoMostrado) {
            inicializar(partida, plano);
            return;
        }

        // Actualiza las entidades.
        sincronizarEntidades(plano);

        // Actualiza la posición del héroe.
        Posicion posicionHeroe =
                partida.getHeroe().getUbicacion().posicion();

        this.juego.moverPersonaje(
                NOMBRE_HEROE,
                posicionHeroe.fila(),
                posicionHeroe.columna()
        );

        mostrarEstado(partida);
    }

    /**
     * Inicializa la ventana y representa el estado inicial del plano.
     */
    private void inicializar(Partida partida, Plano plano) {

        if (this.juego != null) {
            this.juego.cerrar();
        }
        this.personajes.clear();
        this.siguienteId = 1;

        Tablero tablero = plano.getTablero();

        String[] grilla = construirGrilla(plano);

        this.juego = new JuegoVisual(
                "La fuga de la mazmorra",
                grilla,
                TAMANIO_CELDA
        );

        configurarApariencias();

        // Agregamos las entidades antes que el héroe.
        sincronizarEntidades(plano);

        Posicion posicionHeroe =
                partida.getHeroe().getUbicacion().posicion();

        this.juego.agregarPersonaje(
                NOMBRE_HEROE,
                imagen("heroe.png"),
                posicionHeroe.fila(),
                posicionHeroe.columna()
        );

        this.planoMostrado = plano;

        this.juego.mostrar();

        mostrarEstado(partida);
    }

    /**
     * Construye la grilla gráfica a partir del tablero del modelo.
     */
    private String[] construirGrilla(Plano plano) {

        Tablero tablero = plano.getTablero();

        String[] grilla = new String[tablero.getFilas()];

        Posicion salida = plano.getSalida();

        for (int fila = 1; fila <= tablero.getFilas(); fila++) {

            StringBuilder linea = new StringBuilder();

            for (int columna = 1;
                 columna <= tablero.getColumnas();
                 columna++) {

                Posicion posicion = new Posicion(fila, columna);

                char simbolo;

                if (salida != null && salida.equals(posicion)) {
                    simbolo = 'S';
                } else {
                    simbolo = simboloDe(
                            tablero.getTerreno(posicion)
                    );
                }

                linea.append(simbolo);
            }

            grilla[fila - 1] = linea.toString();
        }

        return grilla;
    }

    /**
     * Configura las imágenes de los distintos terrenos.
     */
    private void configurarApariencias() {

        this.juego.definirCelda('#', imagen("pared.png"));
        this.juego.definirCelda('.', imagen("piso.png"));
        this.juego.definirCelda('~', imagen("agua.png"));
        this.juego.definirCelda('^', imagen("lava.png"));
        this.juego.definirCelda('S', imagen("salida.png"));
    }

    /**
     * Convierte un terreno del modelo en un símbolo gráfico.
     */
    private char simboloDe(Terreno terreno) {

        return switch (terreno) {
            case PARED -> '#';
            case PISO -> '.';
            case AGUA -> '~';
            case LAVA -> '^';
        };
    }

    /**
     * Mantiene sincronizados los personajes gráficos y las entidades
     * que actualmente existen en el plano.
     */
    private void sincronizarEntidades(Plano plano) {

        var entidadesActuales = plano.getEntidades();

        Set<Entidad> presentes = Collections.newSetFromMap(
                new IdentityHashMap<Entidad, Boolean>()
        );

        presentes.addAll(entidadesActuales);

        // Eliminamos los personajes cuyas entidades desaparecieron.
        Iterator<Map.Entry<Entidad, String>> iterador =
                this.personajes.entrySet().iterator();

        while (iterador.hasNext()) {

            Map.Entry<Entidad, String> entrada = iterador.next();

            if (!presentes.contains(entrada.getKey())) {
                this.juego.quitarPersonaje(entrada.getValue());
                iterador.remove();
            }
        }

        // Agregamos las entidades nuevas o actualizamos las existentes.
        for (Entidad entidad : entidadesActuales) {

            String nombre = this.personajes.get(entidad);

            Posicion posicion = entidad.getPosicion();

            if (nombre == null) {

                nombre = "entidad-" + this.siguienteId++;

                this.personajes.put(entidad, nombre);

                this.juego.agregarPersonaje(
                        nombre,
                        imagenDe(entidad),
                        posicion.fila(),
                        posicion.columna()
                );

            } else {

                this.juego.moverPersonaje(
                        nombre,
                        posicion.fila(),
                        posicion.columna()
                );
            }
        }
    }

    /**
     * Determina la imagen de una entidad a partir de su símbolo.
     */
    private Path imagenDe(Entidad entidad) {

        return switch (entidad.getSimbolo()) {
            case 'C' -> imagen("tesoro.png");
            case 'M' -> imagen("monstruo.png");
            default -> throw new IllegalArgumentException(
                    "Símbolo de entidad desconocido: " +
                            entidad.getSimbolo()
            );
        };
    }

    /**
     * Muestra información general de la partida.
     */
    private void mostrarEstado(Partida partida) {

        String mensaje =
                "Energia: " + partida.getHeroe().getEnergia()
                        + " | Cofres: " + partida.getCofresAbiertos()
                        + " | Pasos: " + partida.getPasos()
                        + " | Puntaje: " + partida.puntaje();

        if (partida.estadoActual() == EstadoPartida.GANADA) {
            mensaje = "¡GANASTE! " + mensaje;
        } else if (partida.estadoActual() == EstadoPartida.PERDIDA) {
            mensaje = "¡PERDISTE! " + mensaje;
        }

        this.juego.mostrarMensaje(mensaje);
    }

    /**
     * Espera una tecla y la transforma en una acción del modelo.
     */
    @Override
    public Accion leerAccion() {

        if (this.juego == null) {
            throw new IllegalStateException(
                    "La ventana gráfica todavía no fue inicializada"
            );
        }

        while (true) {

            Tecla tecla = this.juego.esperarTecla();

            switch (tecla) {

                case ARRIBA:
                    return Accion.MOVER_ARRIBA;

                case ABAJO:
                    return Accion.MOVER_ABAJO;

                case IZQUIERDA:
                    return Accion.MOVER_IZQUIERDA;

                case DERECHA:
                    return Accion.MOVER_DERECHA;

                case ESPACIO:
                    return Accion.ESPERAR;

                case ENTER:
                    return Accion.USAR;

                case RETROCESO:
                    return Accion.GUARDAR;

                case ESCAPE:
                case CERRAR:
                    return Accion.SALIR;

                case OTRA:
                    this.avisar(
                            "Tecla no reconocida. Utilizá las flechas o WASD."
                    );
                    break;
            }
        }
    }

    /**
     * Muestra un mensaje en la ventana gráfica.
     */
    @Override
    public void avisar(String mensaje) {

        if (this.juego != null) {
            this.juego.mostrarMensaje(mensaje);
        }
    }

    /**
     * Obtiene una imagen de la carpeta de recursos.
     */
    private Path imagen(String nombre) {

        Path ruta = this.carpetaImagenes.resolve(nombre);

        if (!Files.isRegularFile(ruta)) {
            throw new IllegalStateException(
                    "No se encontró la imagen: " + ruta.toAbsolutePath()
            );
        }

        return ruta;
    }

    /**
     * Busca las imágenes tanto desde la raíz del proyecto como desde app/.
     */
    private static Path buscarCarpetaImagenes() {

        Path[] candidatas = {
                Path.of(
                        "app/src/main/java/ar/uba/fi/cb100/" +
                                "librerias/imagen/imagenes"
                ),
                Path.of(
                        "src/main/java/ar/uba/fi/cb100/" +
                                "librerias/imagen/imagenes"
                )
        };

        for (Path candidata : candidatas) {
            if (Files.isRegularFile(candidata.resolve("heroe.png"))) {
                return candidata;
            }
        }

        throw new IllegalStateException(
                "No se encontró la carpeta de imágenes de JuegoVisual"
        );
    }

    /**
     * Cierra la ventana gráfica.
     */
    @Override
    public void close() {

        if (this.juego != null) {
            this.juego.cerrar();
            this.juego = null;
        }

        this.planoMostrado = null;
        this.personajes.clear();
    }
}
