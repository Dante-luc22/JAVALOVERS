package ar.uba.fi.cb100.tp2.vista;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.partida.Accion;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

/**
 * Vista obligatoria: después de cada paso escribe el plano actual en
 * salida/paso-0001.bmp, paso-0002.bmp... Al terminar, los archivos son
 * la película de la partida.
 * La imagen tiene una fila de celdas extra abajo para la barra de energía.
 */
public class VistaBmp implements Vista {

    /** Lado en píxeles de cada celda. */
    public static final int TAMANIO_CELDA = 16;
    
    private final EntradaConsola entrada;
    private final Path carpeta;
    private final int energiaMaxima;

    /**
     * @param carpeta       dónde se escriben los BMP (se crea si no existe)
     * @param energiaMaxima la energía que llena la barra entera
     * @param entrada       de dónde se leen las teclas (la misma que usa el Main)
     */
    public VistaBmp(Path carpeta, int energiaMaxima, EntradaConsola entrada) {
        if (energiaMaxima < 1) {
            throw new IllegalArgumentException("La energía máxima tiene que ser positiva");
        }
        this.carpeta = carpeta;
        this.energiaMaxima = energiaMaxima;
        this.entrada = entrada;
    }

    @Override
    public void mostrar(Partida partida) {
        Plano plano = partida.getPlanoActual();
        Tablero tablero = plano.getTablero();
        Heroe heroe = partida.getHeroe();

        int ancho = tablero.getColumnas() * TAMANIO_CELDA;
        int alto = (tablero.getFilas() + 1) * TAMANIO_CELDA;
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D lapiz = imagen.createGraphics();

        dibujarTerreno(lapiz, tablero);
        dibujarSalida(lapiz, plano);
        dibujarEntidades(lapiz, plano);
        dibujarHeroe(lapiz, heroe);
        dibujarEnergia(lapiz, heroe, ancho, tablero.getFilas());

        lapiz.dispose();
        guardar(imagen, partida.getPasos());

        System.out.println("Paso " + partida.getPasos()
                + " | energía " + heroe.getEnergia()
                + " | cofres " + heroe.getCofresAbiertos());
    
    }

    @Override
    public Accion leerAccion() {
        while (true) {
            String texto = this.entrada.leerTexto(
                    "Acción (w a s d mover, e esperar, u usar, g guardar, q salir):");
            Accion accion = accionDe(texto);
            if (accion != null) {
                return accion;
            }
            System.out.println("No entiendo \"" + texto + "\", probá de nuevo.");
        }
    }

        @Override
    public void avisar(String mensaje) {
        System.out.println(mensaje);
    }

    /**
     * @return la acción de esa tecla, o null si no es ninguna
     */
    private Accion accionDe(String texto) {
        if (texto.length() != 1) {
            return null;
        }
        return switch (Character.toLowerCase(texto.charAt(0))) {
            case 'w' -> Accion.MOVER_ARRIBA;
            case 'a' -> Accion.MOVER_IZQUIERDA;
            case 's' -> Accion.MOVER_ABAJO;
            case 'd' -> Accion.MOVER_DERECHA;
            case 'e' -> Accion.ESPERAR;
            case 'u' -> Accion.USAR;
            case 'g' -> Accion.GUARDAR;
            case 'q' -> Accion.SALIR;
            default -> null;
        };
    }

    // ---------- dibujo ----------

    private void dibujarTerreno(Graphics2D lapiz, Tablero tablero) {
        for (int fila = 1; fila <= tablero.getFilas(); fila++) {
            for (int columna = 1; columna <= tablero.getColumnas(); columna++) {
                Posicion posicion = new Posicion(fila, columna);
                lapiz.setColor(colorDe(tablero.getTerreno(posicion)));
                lapiz.fillRect(x(posicion), y(posicion), TAMANIO_CELDA, TAMANIO_CELDA);
            }
        }
    }

    private void dibujarSalida(Graphics2D lapiz, Plano plano) {
        Posicion salida = plano.getSalida();
        if (salida == null) {
            return;
        }
        lapiz.setColor(Color.GREEN);
        lapiz.fillRect(x(salida), y(salida), TAMANIO_CELDA, TAMANIO_CELDA);
    }

    private void dibujarEntidades(Graphics2D lapiz, Plano plano) {
        for (Entidad entidad : plano.getEntidades()) {
            Posicion posicion = entidad.getPosicion();
            lapiz.setColor(colorDe(entidad.getSimbolo()));
            if (entidad.getSimbolo() == 'M') {
                lapiz.fillOval(x(posicion) + 2, y(posicion) + 2, TAMANIO_CELDA - 4, TAMANIO_CELDA - 4);
            } else {
                lapiz.fillRect(x(posicion) + 3, y(posicion) + 3, TAMANIO_CELDA - 6, TAMANIO_CELDA - 6);
            }
        }
    }

    private void dibujarHeroe(Graphics2D lapiz, Heroe heroe) {
        Posicion posicion = heroe.getUbicacion().posicion();
        lapiz.setColor(Color.RED);
        lapiz.fillOval(x(posicion) + 1, y(posicion) + 1, TAMANIO_CELDA - 2, TAMANIO_CELDA - 2);
    }

    /**
     * La barra ocupa la fila extra de abajo: llena de verde según la energía
     * que queda, el resto en negro.
     */
    private void dibujarEnergia(Graphics2D lapiz, Heroe heroe, int ancho, int filas) {
        int y = filas * TAMANIO_CELDA;
        int energia = Math.max(0, Math.min(heroe.getEnergia(), this.energiaMaxima));
        int lleno = ancho * energia / this.energiaMaxima;

        lapiz.setColor(Color.BLACK);
        lapiz.fillRect(0, y, ancho, TAMANIO_CELDA);
        lapiz.setColor(Color.GREEN);
        lapiz.fillRect(0, y + 2, lleno, TAMANIO_CELDA - 4);
    }

    // ---------- colores ----------

    private Color colorDe(Terreno terreno) {
        return switch (terreno) {
            case PARED -> Color.DARK_GRAY;
            case PISO -> Color.LIGHT_GRAY;
            case AGUA -> Color.BLUE;
            case LAVA -> Color.ORANGE;
        };
    }

    private Color colorDe(char simbolo) {
        return switch (simbolo) {
            case 'C' -> Color.YELLOW;
            case 'M' -> Color.MAGENTA;
            default -> Color.WHITE;
        };
    }

    // ---------- coordenadas y archivo ----------

    /** Columna 1 empieza en el píxel 0. */
    private int x(Posicion posicion) {
        return (posicion.columna() - 1) * TAMANIO_CELDA;
    }

    /** Fila 1 empieza en el píxel 0. */
    private int y(Posicion posicion) {
        return (posicion.fila() - 1) * TAMANIO_CELDA;
    }

    private void guardar(BufferedImage imagen, int paso) {
        try {
            Files.createDirectories(this.carpeta);
            Path archivo = this.carpeta.resolve(String.format("paso-%04d.bmp", paso));
            ImageIO.write(imagen, "bmp", archivo.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir el BMP del paso " + paso, e);
        }
    }
}