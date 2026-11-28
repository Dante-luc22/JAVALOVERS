package ar.uba.fi.cb100.tp2.vista;

import ar.uba.fi.cb100.tp2.modelo.monstruos.TipoDeMonstruo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/** Catálogo visual de los tipos de monstruo disponibles en el juego. */
final class EnciclopediaMonstruos extends JPanel {

    private static final Color FONDO = new Color(16, 21, 30);
    private static final Color FICHA = new Color(25, 33, 45);
    private static final Color SELECCION = new Color(69, 52, 37);
    private static final Color DORADO = new Color(229, 177, 99);
    private static final Color TEXTO = new Color(249, 239, 217);
    private static final Color BORDE = new Color(76, 71, 70);

    private static final Ficha[] FICHAS = {
            new Ficha(TipoDeMonstruo.ERRANTE, "Errante",
                    "Recorre al azar las celdas transitables. Si te alcanza, "
                            + "te quita energía y vuelve a su posición inicial."),
            new Ficha(TipoDeMonstruo.CAZADOR, "Cazador",
                    "Busca un camino hacia vos y avanza una celda por turno. "
                            + "Si te alcanza, te quita energía y vuelve a su posición inicial."),
            new Ficha(TipoDeMonstruo.DORMILON, "Dormilón",
                    "Se queda quieto hasta que estás cerca. Entonces te persigue. "
                            + "Si volvés a alejarte, se duerme otra vez."),
            new Ficha(TipoDeMonstruo.FANTASMA, "Fantasma",
                    "Atraviesa paredes y avanza hacia vos en turnos alternos. "
                            + "En el primer turno se queda quieto."),
            new Ficha(TipoDeMonstruo.LADRON, "Ladrón",
                    "Se mueve al azar. Si te alcanza, te roba un objeto al azar. "
                            + "Si el inventario está vacío, te quita energía.")
    };

    private record Ficha(TipoDeMonstruo tipo, String nombre, String descripcion) {
    }

    private record Retrato(Icon miniatura, Icon imagen) {
    }

    private final Map<TipoDeMonstruo, Retrato> retratos = new EnumMap<>(TipoDeMonstruo.class);
    private double escala = 1.0;

    EnciclopediaMonstruos() {
        super(new BorderLayout(10, 0));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        cargarRetratos();
        JList<Ficha> monstruos = crearLista();

        JLabel nombre = new JLabel();
        nombre.setFont(new Font("Georgia", Font.BOLD, 23));
        nombre.setForeground(DORADO);

        JLabel foto = new JLabel();
        foto.setHorizontalAlignment(SwingConstants.CENTER);
        foto.setVerticalAlignment(SwingConstants.CENTER);
        foto.setPreferredSize(new Dimension(106, 106));
        foto.setOpaque(true);
        foto.setBackground(FONDO);
        foto.setForeground(TEXTO);
        foto.setBorder(BorderFactory.createLineBorder(BORDE));

        JTextArea descripcion = new JTextArea();
        descripcion.setFont(new Font("Georgia", Font.PLAIN, 15));
        descripcion.setForeground(TEXTO);
        descripcion.setOpaque(false);
        descripcion.setEditable(false);
        descripcion.setLineWrap(true);
        descripcion.setWrapStyleWord(true);

        JPanel detalle = new JPanel(new BorderLayout(12, 0));
        detalle.setOpaque(false);
        detalle.add(foto, BorderLayout.WEST);
        detalle.add(descripcion, BorderLayout.CENTER);

        JPanel ficha = new JPanel(new BorderLayout(0, 8));
        ficha.setBackground(FICHA);
        ficha.setBorder(BorderFactory.createEmptyBorder(9, 11, 9, 11));
        ficha.add(nombre, BorderLayout.NORTH);
        ficha.add(detalle, BorderLayout.CENTER);

        monstruos.addListSelectionListener(e -> {
            Ficha actual = monstruos.getSelectedValue();
            if (e.getValueIsAdjusting() || actual == null) {
                return;
            }
            Retrato retrato = retratos.get(actual.tipo());
            nombre.setText(actual.nombre());
            foto.setIcon(retrato == null ? null : retrato.imagen());
            foto.setText(retrato == null ? "Sin imagen" : "");
            foto.getAccessibleContext().setAccessibleName("Retrato de " + actual.nombre());
            descripcion.setText(actual.descripcion());
            descripcion.setCaretPosition(0);
        });

        add(monstruos, BorderLayout.WEST);
        add(ficha, BorderLayout.CENTER);
        monstruos.setSelectedIndex(0);
    }

    private JList<Ficha> crearLista() {
        JList<Ficha> lista = new JList<>(FICHAS);
        lista.setFont(new Font("Georgia", Font.BOLD, 14));
        lista.setBackground(FONDO);
        lista.setSelectionBackground(SELECCION);
        lista.setSelectionForeground(TEXTO);
        lista.setForeground(TEXTO);
        lista.setFixedCellWidth(143);
        lista.setFixedCellHeight(29);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.getAccessibleContext().setAccessibleName("Tipos de monstruo");

        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        lista.setCellRenderer((origen, ficha, indice, elegida, tieneFoco) -> {
            JLabel fila = (JLabel) renderer.getListCellRendererComponent(
                    origen, ficha.nombre(), indice, elegida, tieneFoco);
            Retrato retrato = retratos.get(ficha.tipo());
            fila.setIcon(retrato == null ? null : retrato.miniatura());
            fila.setIconTextGap(8);
            fila.setFont(origen.getFont());
            fila.setBorder(tieneFoco
                    ? BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 2, 0, 0, DORADO),
                    BorderFactory.createEmptyBorder(0, 5, 0, 5))
                    : BorderFactory.createEmptyBorder(0, 7, 0, 5));
            return fila;
        });
        return lista;
    }

    private void cargarRetratos() {
        Path carpeta;
        try {
            carpeta = VistaGrafica.buscarCarpetaImagenes();
        } catch (IllegalStateException error) {
            return;
        }

        for (Ficha ficha : FICHAS) {
            String archivo = ficha.tipo().name().toLowerCase(Locale.ROOT) + ".png";
            Path ruta = carpeta.resolve(archivo);
            if (!Files.isRegularFile(ruta)) {
                ruta = carpeta.resolve("monstruo.png");
            }
            try {
                BufferedImage imagen = ImageIO.read(ruta.toFile());
                if (imagen != null) {
                    retratos.put(ficha.tipo(), new Retrato(new Sprite(imagen, 24), new Sprite(imagen, 96)));
                }
            } catch (IOException error) {
                // La ficha textual sigue disponible aunque falte el sprite.
            }
        }
    }

    void ajustarEscala(double factor) {
        escala = factor;
    }

    private final class Sprite implements Icon {
        private final BufferedImage imagen;
        private final int ladoBase;

        private Sprite(BufferedImage imagen, int ladoBase) {
            this.imagen = imagen;
            this.ladoBase = ladoBase;
        }

        @Override
        public int getIconWidth() {
            return (int) Math.round(ladoBase * escala);
        }

        @Override
        public int getIconHeight() {
            return getIconWidth();
        }

        @Override
        public void paintIcon(Component componente, Graphics g, int x, int y) {
            int lado = getIconWidth();
            double proporcion = (double) lado / Math.max(imagen.getWidth(), imagen.getHeight());
            int ancho = (int) Math.round(imagen.getWidth() * proporcion);
            int alto = (int) Math.round(imagen.getHeight() * proporcion);
            Graphics2D pincel = (Graphics2D) g.create();
            try {
                pincel.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                pincel.drawImage(imagen, x + (lado - ancho) / 2, y + (lado - alto) / 2, ancho, alto, null);
            } finally {
                pincel.dispose();
            }
        }
    }
}
