package ar.uba.fi.cb100.librerias.imagen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

/**
 * El panel de Swing que dibuja un {@link EstadoDelTablero}. Es la única clase
 * de la librería que pinta. Swing llama a {@link #paintComponent} cuando hace
 * falta; nosotros sólo le avisamos con {@code repaint()} que algo cambió.
 */
final class PanelDelTablero extends JPanel {

    private static final Color LINEA_DE_GRILLA = new Color(0, 0, 0, 28);
    private static final Color DESCONOCIDO = Color.MAGENTA;

    private final EstadoDelTablero estado;
    private final int tamanio;

    PanelDelTablero(EstadoDelTablero estado, int tamanioDeCelda) {
        this.estado = estado;
        this.tamanio = tamanioDeCelda;
        setPreferredSize(new Dimension(estado.getColumnas() * tamanio, estado.getFilas() * tamanio));
        setBackground(new Color(48, 48, 48));
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);

        Graphics2D g = (Graphics2D) graficos.create();

        try {
            // Pixel-art: conservar los bordes definidos de los sprites.
            g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
            );

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            synchronized (estado) {

                // Dimensiones originales del tablero.
                int anchoTablero = estado.getColumnas() * tamanio;
                int altoTablero = estado.getFilas() * tamanio;

                // Evitar divisiones por cero.
                if (anchoTablero <= 0 || altoTablero <= 0
                        || getWidth() <= 0 || getHeight() <= 0) {
                    return;
                }

                // Calcular la escala necesaria para que entre todo el tablero.
                double escala = Math.min(
                        (double) getWidth() / anchoTablero,
                        (double) getHeight() / altoTablero
                );

                // Calcular el espacio sobrante para centrar el tablero.
                double anchoDibujado = anchoTablero * escala;
                double altoDibujado = altoTablero * escala;

                double desplazamientoX = (getWidth() - anchoDibujado) / 2.0;
                double desplazamientoY = (getHeight() - altoDibujado) / 2.0;

                // Aplicar desplazamiento y escala.
                g.translate(desplazamientoX, desplazamientoY);
                g.scale(escala, escala);

                // Dibujar utilizando las dimensiones originales.
                dibujarCeldas(g);
                dibujarPersonajes(g);
                dibujarEtiquetas(g);
                dibujarTextosFlotantes(g);
            }

        } finally {
            g.dispose();
        }
    }


    private void dibujarCeldas(Graphics2D g) {
        for (int f = 1; f <= estado.getFilas(); f++) {
            for (int c = 1; c <= estado.getColumnas(); c++) {
                int x = (c - 1) * tamanio;
                int y = (f - 1) * tamanio;
                char simbolo = estado.getCelda(f, c);
                EstadoDelTablero.Apariencia apariencia = estado.aparienciaDe(simbolo);
                if (apariencia == null) {
                    // Símbolo sin apariencia definida: que se note, en vez de fallar en silencio.
                    g.setColor(DESCONOCIDO);
                    g.fillRect(x, y, tamanio, tamanio);
                    dibujarLetra(g, String.valueOf(simbolo), x, y, Color.BLACK);
                } else if (apariencia.imagen() != null) {
                    g.drawImage(apariencia.imagen(), x, y, tamanio, tamanio, null);
                } else {
                    g.setColor(apariencia.color());
                    g.fillRect(x, y, tamanio, tamanio);
                }
                g.setColor(LINEA_DE_GRILLA);
                g.drawRect(x, y, tamanio - 1, tamanio - 1);
            }
        }
    }

    private void dibujarPersonajes(Graphics2D g) {
        int margen = Math.max(2, tamanio / 8);
        int lado = tamanio - 2 * margen;
        for (EstadoDelTablero.Personaje personaje : estado.personajes()) {
            int x = (personaje.columna - 1) * tamanio + margen;
            int y = (personaje.fila - 1) * tamanio + margen;
            if (personaje.apariencia.imagen() != null) {
                g.drawImage(personaje.apariencia.imagen(), x, y, lado, lado, null);
            } else {
                g.setColor(personaje.apariencia.color());
                g.fillOval(x, y, lado, lado);
                g.setColor(Color.BLACK);
                g.drawOval(x, y, lado, lado);
                dibujarLetra(g, personaje.nombre.substring(0, 1).toUpperCase(),
                        x - margen, y - margen, Color.WHITE);
            }
        }
    }

    /**
     * Los textos que suben desde una celda y se van desvaneciendo. Cada uno
     * tiene un borde oscuro para que se lea sobre cualquier fondo.
     */
    private void dibujarTextosFlotantes(Graphics2D g) {
        long ahora = System.currentTimeMillis();
        Font fuente = new Font(Font.SANS_SERIF, Font.BOLD, Math.max(12, tamanio * 2 / 5));
        for (EstadoDelTablero.TextoFlotante texto : estado.textosFlotantes()) {
            double avance = (double) (ahora - texto.inicio) / EstadoDelTablero.DURACION_DEL_TEXTO_MS;
            if (avance < 0 || avance >= 1) {
                continue;                                   // todavía no empezó, o ya terminó
            }
            // sube rápido al principio y se frena; se desvanece en la segunda mitad.
            // Sube solo dentro de la celda, para no tapar lo que va arriba del personaje.
            double subida = tamanio * 0.45 * (1 - Math.pow(1 - avance, 2));
            float opacidad = (float) (avance < 0.5 ? 1.0 : 1.0 - (avance - 0.5) / 0.5);

            double centroX = (texto.columna - 1) * tamanio + tamanio / 2.0;
            double base = (texto.fila - 1) * tamanio + tamanio * 0.9 - subida;
            dibujarTextoConBorde(g, fuente, texto.texto, texto.color, centroX, base, opacidad);
        }
    }

    /**
     * El texto fijo que algunos personajes llevan arriba (por ejemplo el
     * veneno que le queda al héroe), pegado al borde de arriba de su celda.
     */
    private void dibujarEtiquetas(Graphics2D g) {
        Font fuente = new Font(Font.SANS_SERIF, Font.BOLD, Math.max(11, tamanio / 3));
        for (EstadoDelTablero.Personaje personaje : estado.personajes()) {
            if (personaje.etiqueta == null) {
                continue;
            }
            double centroX = (personaje.columna - 1) * tamanio + tamanio / 2.0;
            double base = (personaje.fila - 1) * tamanio - 2;
            dibujarTextoConBorde(g, fuente, personaje.etiqueta, personaje.colorDeLaEtiqueta, centroX, base, 1f);
        }
    }

    /**
     * Un texto centrado en {@code centroX} y apoyado en la línea {@code base},
     * con un borde oscuro para que se lea sobre cualquier fondo.
     */
    private void dibujarTextoConBorde(Graphics2D g, Font fuente, String texto, Color color,
                                      double centroX, double base, float opacidad) {
        Shape letras = fuente.createGlyphVector(g.getFontRenderContext(), texto).getOutline();
        var caja = letras.getBounds2D();
        double x = centroX - caja.getWidth() / 2 - caja.getX();
        Shape ubicado = AffineTransform.getTranslateInstance(x, base).createTransformedShape(letras);

        Graphics2D pincel = (Graphics2D) g.create();
        try {
            pincel.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
            pincel.setStroke(new BasicStroke(Math.max(3f, tamanio / 12f),
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            pincel.setColor(new Color(10, 10, 14));
            pincel.draw(ubicado);
            pincel.setColor(color);
            pincel.fill(ubicado);
        } finally {
            pincel.dispose();
        }
    }

    /** Una letra centrada en la celda cuya esquina es (x, y). */
    private void dibujarLetra(Graphics2D g, String letra, int x, int y, Color color) {
        g.setColor(color);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(10, tamanio / 2)));
        FontMetrics metricas = g.getFontMetrics();
        int ancho = metricas.stringWidth(letra);
        int alto = metricas.getAscent();
        g.drawString(letra, x + (tamanio - ancho) / 2, y + (tamanio + alto) / 2 - 2);
    }
}
