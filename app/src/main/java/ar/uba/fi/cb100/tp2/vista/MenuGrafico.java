package ar.uba.fi.cb100.tp2.vista;

import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.ranking.Ranking;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;
import ar.uba.fi.cb100.tp2.persistencia.ArchivoDeRanking;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SecondaryLoop;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/** Presenta el menú principal, las opciones de inicio y el resultado de la partida. */
public final class MenuGrafico {

    private static final Color FONDO = new Color(12, 17, 25);
    private static final Color PANEL = new Color(19, 26, 36);
    private static final Color BORDE = new Color(119, 110, 100);
    private static final Color TEXTO = new Color(249, 239, 217);
    private static final Color DORADO = new Color(229, 177, 99);
    private static final Color PANEL_CLARO = new Color(33, 40, 51);
    private static final Color SELECCION = new Color(121, 76, 39);
    private static final int ANCHO_BASE = 920;
    private static final int ALTO_BASE = 530;
    private static final int ALTO_ENCICLOPEDIA = 346;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Dificultad[] DIFICULTADES =
            {Dificultad.FACIL, Dificultad.MEDIO, Dificultad.DIFICIL};

    public enum ModoVista {
        GRAFICA, BMP
    }

    public record OpcionesInicio(Dificultad dificultad, ModoVista modoVista, Long semilla) {
    }

    private enum Pantalla {
        INICIO, JUGAR, ENCICLOPEDIA, OPCIONES, RANKING
    }

    private enum TipoDeMapa {
        CONFIGURADO, ALEATORIO, PERSONALIZADO
    }

    private ModoVista modoVista = ModoVista.GRAFICA;

    /**
     * @param semillaConfigurada semilla del archivo de configuración
     * @return las opciones elegidas, o vacío al salir del menú
     */
    public Optional<OpcionesInicio> mostrarInicio(Long semillaConfigurada) {
        return enHiloGrafico(() -> new Navegacion(semillaConfigurada).mostrar());
    }

    /** Devuelve true si el jugador elige reintentar. */
    public boolean mostrarResultado(EstadoPartida estado, int puntaje, long semilla) {
        if (estado == EstadoPartida.EN_CURSO) {
            return false;
        }

        return enHiloGrafico(() -> {
            String titulo = estado == EstadoPartida.GANADA ? "¡Ganaste!" : "Perdiste";

            JTextField campoSemilla = new JTextField(Long.toString(semilla), 20);
            campoSemilla.setEditable(false);

            JLabel etiquetaSemilla = new JLabel("Semilla");
            etiquetaSemilla.setLabelFor(campoSemilla);

            JButton copiar = new JButton("Copiar");
            copiar.addActionListener(e -> {
                campoSemilla.selectAll();
                campoSemilla.copy();
            });

            JPanel filaSemilla = new JPanel(new BorderLayout(8, 0));
            filaSemilla.add(etiquetaSemilla, BorderLayout.WEST);
            filaSemilla.add(campoSemilla, BorderLayout.CENTER);
            filaSemilla.add(copiar, BorderLayout.EAST);

            JPanel contenido = new JPanel(new BorderLayout(0, 10));
            contenido.add(new JLabel("Puntaje: " + puntaje), BorderLayout.NORTH);
            contenido.add(filaSemilla, BorderLayout.CENTER);

            Object[] acciones = {"Reintentar", "Menú principal"};
            int opcion = JOptionPane.showOptionDialog(
                    null, contenido, titulo,
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, acciones, acciones[0]
            );
            return opcion == 0;
        });
    }

    private final class Navegacion {

        private final JFrame ventana = new JFrame("Escape de la mazmorra");
        private final CardLayout distribucion = new CardLayout();
        private final JPanel tarjetas = new JPanel(distribucion);
        private final JPanel ranking = new JPanel(new BorderLayout());
        private OpcionesInicio resultado;
        private double escala = 1.0;

        private Navegacion(Long semillaConfigurada) {
            tarjetas.setOpaque(false);
            ranking.setOpaque(false);
            tarjetas.add(inicio(), Pantalla.INICIO.name());
            tarjetas.add(jugar(semillaConfigurada), Pantalla.JUGAR.name());
            tarjetas.add(enciclopedia(), Pantalla.ENCICLOPEDIA.name());
            tarjetas.add(opciones(), Pantalla.OPCIONES.name());
            tarjetas.add(ranking, Pantalla.RANKING.name());

            JPanel contenido = new FondoMazmorra();
            TituloMazmorra titulo = new TituloMazmorra();
            contenido.add(titulo, BorderLayout.NORTH);
            contenido.add(tarjetas, BorderLayout.CENTER);
            contenido.setPreferredSize(new Dimension(ANCHO_BASE, ALTO_BASE));

            ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            ventana.setContentPane(contenido);
            ventana.pack();
            int altoMinimo = titulo.getPreferredSize().height + ALTO_ENCICLOPEDIA + 12
                    + ventana.getInsets().top + ventana.getInsets().bottom;
            ventana.setMinimumSize(new Dimension(760, altoMinimo));
            ventana.setLocationRelativeTo(null);
            contenido.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent evento) {
                    actualizarEscala(contenido, titulo);
                }
            });
        }

        private void actualizarEscala(JPanel contenido, TituloMazmorra titulo) {
            double nueva = Math.min((double) contenido.getWidth() / ANCHO_BASE,
                    (double) contenido.getHeight() / ALTO_BASE);
            if (nueva <= 0 || nueva == escala) {
                return;
            }

            escala = nueva;
            Escala.aplicar(titulo, escala);
            Escala.aplicar(tarjetas, escala);
            contenido.revalidate();
            contenido.repaint();
        }

        private Optional<OpcionesInicio> mostrar() {
            // Espera la selección con el menú abierto y los eventos de Swing activos.
            SecondaryLoop espera = Toolkit.getDefaultToolkit()
                    .getSystemEventQueue().createSecondaryLoop();
            ventana.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent evento) {
                    espera.exit();
                }
            });

            ventana.setVisible(true);
            espera.enter();
            return Optional.ofNullable(resultado);
        }

        private void irA(Pantalla pantalla) {
            distribucion.show(tarjetas, pantalla.name());
        }

        private JPanel inicio() {
            JPanel botones = new JPanel(new GridLayout(0, 1, 0, 10));
            botones.setOpaque(false);
            botones.add(boton("Jugar partida", true, () -> irA(Pantalla.JUGAR)));
            botones.add(boton("Enciclopedia", false, () -> irA(Pantalla.ENCICLOPEDIA)));
            botones.add(boton("Ranking", false, this::abrirRanking));

            JPanel filaInferior = new JPanel(new GridLayout(1, 2, 10, 0));
            filaInferior.setOpaque(false);
            filaInferior.add(boton("Opciones", false, () -> irA(Pantalla.OPCIONES)));
            filaInferior.add(boton("Salir", false, ventana::dispose));
            botones.add(filaInferior);
            botones.setPreferredSize(new Dimension(344, 186));

            JPanel pagina = new JPanel(new GridBagLayout());
            pagina.setOpaque(false);
            pagina.setBorder(BorderFactory.createEmptyBorder(0, 0, 73, 0));
            pagina.add(botones);
            return pagina;
        }

        private JPanel jugar(Long semillaConfigurada) {
            JComboBox<String> dificultad = selectorDeDificultad();
            JComboBox<String> mapa = new SelectorMazmorra(
                    new String[]{"De configuración", "Aleatorio", "Semilla personalizada"}
            );
            JTextField semilla = new CampoMazmorra(
                    semillaConfigurada == null ? "" : semillaConfigurada.toString()
            );
            semilla.setEnabled(false);
            mapa.addActionListener(e -> {
                boolean personalizar = TipoDeMapa.values()[mapa.getSelectedIndex()]
                        == TipoDeMapa.PERSONALIZADO;
                semilla.setEnabled(personalizar);
                if (personalizar) {
                    semilla.requestFocusInWindow();
                }
            });

            JPanel campos = new JPanel(new GridLayout(0, 2, 14, 12));
            campos.setOpaque(false);
            agregarCampo(campos, "Dificultad", dificultad);
            agregarCampo(campos, "Mapa", mapa);
            agregarCampo(campos, "Semilla", semilla);

            JPanel cuerpo = new JPanel(new BorderLayout());
            cuerpo.setOpaque(false);
            cuerpo.add(campos, BorderLayout.NORTH);

            JButton iniciar = boton("Empezar partida", true, () -> {
                TipoDeMapa tipo = TipoDeMapa.values()[mapa.getSelectedIndex()];
                Long semillaElegida;
                try {
                    semillaElegida = switch (tipo) {
                        case CONFIGURADO -> semillaConfigurada;
                        case ALEATORIO -> null;
                        case PERSONALIZADO -> Long.valueOf(semilla.getText().strip());
                    };
                } catch (NumberFormatException error) {
                    JOptionPane.showMessageDialog(ventana, "Ingresá una semilla entera válida.",
                            "Semilla personalizada", JOptionPane.WARNING_MESSAGE);
                    semilla.requestFocusInWindow();
                    semilla.selectAll();
                    return;
                }

                resultado = new OpcionesInicio(
                        DIFICULTADES[dificultad.getSelectedIndex()], modoVista, semillaElegida
                );
                ventana.dispose();
            });

            return pagina("Nueva partida", cuerpo, iniciar);
        }

        private JPanel opciones() {
            JComboBox<String> vista = new SelectorMazmorra(new String[]{"Gráfica", "BMP (consola)"});
            vista.setSelectedIndex(modoVista == ModoVista.GRAFICA ? 0 : 1);
            vista.addActionListener(e ->
                    modoVista = vista.getSelectedIndex() == 0 ? ModoVista.GRAFICA : ModoVista.BMP);

            JPanel campos = new JPanel(new GridLayout(1, 2, 14, 12));
            campos.setOpaque(false);
            agregarCampo(campos, "Vista durante la partida", vista);

            JPanel cuerpo = new JPanel(new BorderLayout());
            cuerpo.setOpaque(false);
            cuerpo.add(campos, BorderLayout.NORTH);
            return pagina("Opciones", cuerpo);
        }

        private JPanel enciclopedia() {
            CardLayout distribucionFichas = new CardLayout();
            JPanel fichas = new JPanel(distribucionFichas);
            fichas.add(new EnciclopediaMonstruos(), "monstruos");
            fichas.add(ficha("""
                    Cofre de puntos — Suma los 100 puntos de apertura.

                    Poción — Recupera energía al abrir el cofre.

                    Pinchos — Quitan energía al abrir el cofre.

                    Veneno — Aumenta temporalmente el costo de movimiento.

                    Zapatos de agua — Al usarlos, el agua cuesta como el piso.

                    Botas ignífugas — Al usarlas, la lava cuesta como el piso.
                    """), "cofres");

            JButton monstruos = new BotonMazmorra("Monstruos", false);
            JButton cofres = new BotonMazmorra("Cofres y objetos", false);
            Font fuentePestanas = new Font("Georgia", Font.BOLD, 15);
            monstruos.setFont(fuentePestanas);
            cofres.setFont(fuentePestanas);
            monstruos.setSelected(true);
            monstruos.addActionListener(e -> {
                distribucionFichas.show(fichas, "monstruos");
                monstruos.setSelected(true);
                cofres.setSelected(false);
            });
            cofres.addActionListener(e -> {
                distribucionFichas.show(fichas, "cofres");
                cofres.setSelected(true);
                monstruos.setSelected(false);
            });

            JPanel pestanas = new JPanel(new GridLayout(1, 2, 9, 0));
            pestanas.setOpaque(false);
            pestanas.add(monstruos);
            pestanas.add(cofres);

            JPanel contenido = new JPanel(new BorderLayout(0, 9));
            contenido.setOpaque(false);
            contenido.add(pestanas, BorderLayout.NORTH);
            contenido.add(fichas, BorderLayout.CENTER);

            JLabel encabezado = new JLabel("Enciclopedia");
            encabezado.setFont(new Font("Georgia", Font.BOLD, 28));
            encabezado.setForeground(DORADO);
            return pagina(encabezado, contenido, new Dimension(600, ALTO_ENCICLOPEDIA));
        }

        private void abrirRanking() {
            ranking.removeAll();
            ranking.add(pagina("Ranking", contenidoRanking()), BorderLayout.CENTER);
            Escala.aplicar(ranking, escala);
            ranking.revalidate();
            ranking.repaint();
            irA(Pantalla.RANKING);
        }

        private JPanel contenidoRanking() {
            JPanel contenido = new JPanel(new BorderLayout(0, 10));
            contenido.setOpaque(false);

            Ranking partidas;
            try {
                partidas = ArchivoDeRanking.cargar(ArchivoDeRanking.RUTA_POR_DEFECTO);
            } catch (IllegalStateException error) {
                contenido.add(texto("No se pudo leer el archivo de ranking.", 14), BorderLayout.NORTH);
                return contenido;
            }

            JComboBox<String> dificultad = selectorDeDificultad();
            JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
            filtro.setOpaque(false);
            JPanel campos = new JPanel(new GridLayout(1, 2, 12, 0));
            campos.setOpaque(false);
            agregarCampo(campos, "Dificultad", dificultad);
            filtro.add(campos);

            JTable tabla = new JTable();
            tabla.setDefaultEditor(Object.class, null);
            tabla.setRowHeight(24);
            tabla.getTableHeader().setReorderingAllowed(false);
            estilizarTabla(tabla);
            JLabel estado = texto("", 13);

            Runnable actualizar = () -> {
                var mejores = partidas.obtenerTop10(DIFICULTADES[dificultad.getSelectedIndex()]);
                Object[][] filas = new Object[mejores.size()][4];
                for (int i = 0; i < mejores.size(); i++) {
                    var entrada = mejores.get(i);
                    filas[i] = new Object[]{i + 1, entrada.nombre(), entrada.puntaje(),
                            entrada.fecha().format(FORMATO_FECHA)};
                }
                tabla.setModel(new DefaultTableModel(filas,
                        new String[]{"Puesto", "Jugador", "Puntaje", "Fecha"}));
                int[] anchos = {60, 90, 90, 190};
                for (int i = 0; i < anchos.length; i++) {
                    tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
                }
                estado.setText(mejores.isEmpty() ? "Todavía no hay partidas registradas." : " ");
            };
            dificultad.addActionListener(e -> actualizar.run());
            actualizar.run();

            contenido.add(filtro, BorderLayout.NORTH);
            JScrollPane desplazable = new JScrollPane(tabla);
            desplazable.setBorder(BorderFactory.createLineBorder(BORDE));
            desplazable.getViewport().setBackground(FONDO);
            estilizarBarra(desplazable.getVerticalScrollBar());
            JPanel esquina = new JPanel();
            esquina.setBackground(PANEL_CLARO);
            desplazable.setCorner(JScrollPane.UPPER_RIGHT_CORNER, esquina);
            contenido.add(desplazable, BorderLayout.CENTER);
            contenido.add(estado, BorderLayout.SOUTH);
            return contenido;
        }

        private JPanel pagina(String titulo, Component cuerpo, JButton... acciones) {
            return pagina(texto(titulo, 22), cuerpo, new Dimension(550, 296), acciones);
        }

        private JPanel pagina(JLabel titulo, Component cuerpo, Dimension tamano, JButton... acciones) {
            JPanel pagina = new JPanel(new BorderLayout(0, 14));
            pagina.setBackground(PANEL);
            pagina.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE),
                    BorderFactory.createEmptyBorder(15, 18, 14, 18)
            ));
            pagina.setPreferredSize(tamano);
            pagina.add(titulo, BorderLayout.NORTH);
            pagina.add(cuerpo, BorderLayout.CENTER);

            JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pie.setOpaque(false);
            pie.add(boton("Volver", false, () -> irA(Pantalla.INICIO)));
            for (JButton accion : acciones) {
                pie.add(accion);
            }
            pagina.add(pie, BorderLayout.SOUTH);

            JPanel marco = new JPanel(new GridBagLayout());
            marco.setOpaque(false);
            marco.add(pagina);
            return marco;
        }
    }

    private static void agregarCampo(JPanel panel, String nombre, JComponent campo) {
        JLabel etiqueta = texto(nombre, 14);
        etiqueta.setLabelFor(campo);
        panel.add(etiqueta);
        panel.add(campo);
    }

    private static JComboBox<String> selectorDeDificultad() {
        return new SelectorMazmorra(new String[]{"Fácil", "Medio", "Difícil"});
    }

    private static JLabel texto(String contenido, float tamano) {
        JLabel etiqueta = new JLabel(contenido);
        etiqueta.setForeground(TEXTO);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, tamano));
        return etiqueta;
    }

    private static JScrollPane ficha(String contenido) {
        JTextArea detalle = new JTextArea(contenido);
        detalle.setEditable(false);
        detalle.setLineWrap(true);
        detalle.setWrapStyleWord(true);
        detalle.setBackground(FONDO);
        detalle.setForeground(TEXTO);
        detalle.setFont(new Font("Georgia", Font.PLAIN, 15));
        detalle.setBorder(BorderFactory.createEmptyBorder(12, 14, 10, 14));
        detalle.setCaretPosition(0);
        JScrollPane panel = new JScrollPane(detalle);
        panel.setBorder(BorderFactory.createLineBorder(BORDE));
        return panel;
    }

    private static JButton boton(String texto, boolean principal, Runnable accion) {
        JButton boton = new BotonMazmorra(texto, principal);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private static final class Escala {

        private static final Object MEDIDAS_ORIGINALES = new Object();

        private record Medidas(Font fuente, Dimension preferida, Border borde,
                               Dimension separaciones, int anchoCelda, int altoFila) {
            private Medidas(JComponent vista) {
                this(vista.getFont(), vista.isPreferredSizeSet() ? vista.getPreferredSize() : null,
                        vista.getBorder(), separacionesDe(vista.getLayout()),
                        vista instanceof JList<?> lista ? lista.getFixedCellWidth() : -1,
                        vista instanceof JList<?> lista ? lista.getFixedCellHeight()
                                : vista instanceof JTable tabla ? tabla.getRowHeight() : -1);
            }
        }

        private static void aplicar(JComponent vista, double factor) {
            Medidas base = (Medidas) vista.getClientProperty(MEDIDAS_ORIGINALES);
            if (base == null) {
                base = new Medidas(vista);
                vista.putClientProperty(MEDIDAS_ORIGINALES, base);
            }
            if (base.fuente() != null) {
                vista.setFont(base.fuente().deriveFont((float) (base.fuente().getSize2D() * factor)));
            }
            if (base.preferida() != null) {
                vista.setPreferredSize(new Dimension(pixeles(base.preferida().width, factor),
                        pixeles(base.preferida().height, factor)));
            }
            vista.setBorder(escalarBorde(base.borde(), factor));
            separar(vista.getLayout(), base.separaciones(), factor);

            if (vista instanceof EnciclopediaMonstruos enciclopedia) {
                enciclopedia.ajustarEscala(factor);
            }
            if (vista instanceof JList<?> lista && base.altoFila() > 0) {
                lista.setFixedCellHeight(pixeles(base.altoFila(), factor));
                if (base.anchoCelda() > 0) {
                    lista.setFixedCellWidth(pixeles(base.anchoCelda(), factor));
                }
            }
            if (vista instanceof JTable tabla) {
                tabla.setRowHeight(pixeles(base.altoFila(), factor));
            }

            for (Component hijo : vista.getComponents()) {
                if (hijo instanceof JComponent componente) {
                    aplicar(componente, factor);
                }
            }
        }

        private static Border escalarBorde(Border borde, double factor) {
            if (borde instanceof CompoundBorder compuesto) {
                return BorderFactory.createCompoundBorder(
                        escalarBorde(compuesto.getOutsideBorder(), factor),
                        escalarBorde(compuesto.getInsideBorder(), factor));
            }
            if (borde != null && borde.getClass() == EmptyBorder.class) {
                Insets margen = ((EmptyBorder) borde).getBorderInsets();
                return BorderFactory.createEmptyBorder(
                        pixeles(margen.top, factor), pixeles(margen.left, factor),
                        pixeles(margen.bottom, factor), pixeles(margen.right, factor));
            }
            return borde;
        }

        private static Dimension separacionesDe(LayoutManager layout) {
            if (layout instanceof GridLayout grilla) {
                return new Dimension(grilla.getHgap(), grilla.getVgap());
            }
            if (layout instanceof BorderLayout borde) {
                return new Dimension(borde.getHgap(), borde.getVgap());
            }
            if (layout instanceof FlowLayout flujo) {
                return new Dimension(flujo.getHgap(), flujo.getVgap());
            }
            return null;
        }

        private static void separar(LayoutManager layout, Dimension separaciones, double factor) {
            if (separaciones == null) {
                return;
            }
            int horizontal = pixeles(separaciones.width, factor);
            int vertical = pixeles(separaciones.height, factor);
            if (layout instanceof GridLayout grilla) {
                grilla.setHgap(horizontal);
                grilla.setVgap(vertical);
            } else if (layout instanceof BorderLayout borde) {
                borde.setHgap(horizontal);
                borde.setVgap(vertical);
            } else if (layout instanceof FlowLayout flujo) {
                flujo.setHgap(horizontal);
                flujo.setVgap(vertical);
            }
        }

        private static int pixeles(int base, double factor) {
            return (int) Math.round(base * factor);
        }
    }

    private static final class FondoMazmorra extends JPanel {

        private final BufferedImage panorama = cargarPanorama();

        private FondoMazmorra() {
            super(new BorderLayout());
            setBackground(FONDO);
        }

        private static BufferedImage cargarPanorama() {
            try (var recurso = MenuGrafico.class.getResourceAsStream("/menu/fondo-mazmorra.png")) {
                return recurso == null ? null : ImageIO.read(recurso);
            } catch (IOException error) {
                return null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (panorama == null) {
                return;
            }

            Graphics2D pincel = (Graphics2D) g.create();
            try {
                pincel.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                double escala = Math.max((double) getWidth() / panorama.getWidth(),
                        (double) getHeight() / panorama.getHeight());
                int ancho = (int) Math.ceil(panorama.getWidth() * escala);
                int alto = (int) Math.ceil(panorama.getHeight() * escala);
                pincel.drawImage(panorama, (getWidth() - ancho) / 2, (getHeight() - alto) / 2,
                        ancho, alto, null);
                pincel.setPaint(new GradientPaint(0, 0, new Color(6, 10, 17, 151),
                        0, Math.max(1, getHeight() / 2), new Color(6, 10, 17, 12)));
                pincel.fillRect(0, 0, getWidth(), getHeight() / 2);
            } finally {
                pincel.dispose();
            }
        }
    }

    private static final class TituloMazmorra extends JComponent {

        private static final String TITULO = "ESCAPE DE LA MAZMORRA";
        private static final String GRUPO = "JAVALOVERS";
        private static final int ALTO = 145;

        private TituloMazmorra() {
            setPreferredSize(new Dimension(0, ALTO));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getWidth() <= 0 || getHeight() <= 0) {
                return;
            }
            Graphics2D pincel = (Graphics2D) g.create();
            try {
                double proporcion = (double) getHeight() / ALTO;
                double anchoDisponible = getWidth() / proporcion;
                pincel.scale(proporcion, proporcion);
                pincel.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                pincel.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Font fuente = new Font("Georgia", Font.BOLD, 54);
                Shape letras = fuente.createGlyphVector(pincel.getFontRenderContext(), TITULO).getOutline();
                var caja = letras.getBounds2D();
                double escala = Math.min(1.0, (anchoDisponible - 84.0) / caja.getWidth());
                double x = (anchoDisponible - caja.getWidth() * escala) / 2 - caja.getX() * escala;
                AffineTransform posicion = AffineTransform.getTranslateInstance(x, 86);
                posicion.scale(escala, escala);
                Shape frente = posicion.createTransformedShape(letras);
                Shape relieve = AffineTransform.getTranslateInstance(0, 6).createTransformedShape(frente);

                pincel.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                pincel.setColor(new Color(9, 8, 14, 220));
                pincel.draw(relieve);
                pincel.setColor(new Color(83, 50, 29));
                pincel.fill(relieve);
                pincel.setStroke(new BasicStroke(2));
                pincel.setColor(new Color(59, 36, 24));
                pincel.draw(frente);
                pincel.setPaint(new GradientPaint(0, 48, new Color(255, 239, 194),
                        0, 90, new Color(186, 129, 71)));
                pincel.fill(frente);

                pincel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                int ancho = pincel.getFontMetrics().stringWidth(GRUPO);
                int centro = (int) Math.round(anchoDisponible / 2);
                pincel.setColor(DORADO);
                pincel.drawLine(centro - ancho / 2 - 45, 117, centro - ancho / 2 - 14, 117);
                pincel.drawLine(centro + ancho / 2 + 14, 117, centro + ancho / 2 + 45, 117);
                pincel.drawString(GRUPO, centro - ancho / 2, 122);
            } finally {
                pincel.dispose();
            }
        }
    }

    private static final class BotonMazmorra extends JButton {

        private final boolean principal;

        private BotonMazmorra(String texto, boolean principal) {
            super(texto);
            this.principal = principal;
            setUI(new BasicButtonUI());
            setForeground(TEXTO);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(8, 20, 11, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D pincel = (Graphics2D) g.create();
            try {
                double escala = getFont().getSize2D() / 15.0;
                pincel.scale(escala, escala);
                int ancho = (int) Math.round(getWidth() / escala);
                int alto = (int) Math.round(getHeight() / escala);
                boolean resaltado = getModel().isRollover() || isFocusOwner();
                boolean activo = principal || isSelected();
                Color arriba = activo ? new Color(121, 76, 39) : new Color(58, 67, 81);
                Color abajo = activo ? new Color(66, 40, 29) : new Color(33, 40, 51);
                if (resaltado) {
                    arriba = arriba.brighter();
                }
                if (getModel().isArmed()) {
                    abajo = abajo.darker();
                }

                pincel.setColor(new Color(8, 11, 16, 204));
                pincel.fillRect(2, 3, ancho - 2, alto - 3);
                pincel.setPaint(new GradientPaint(0, 0, arriba, 0, Math.max(1, alto - 4), abajo));
                pincel.fillRect(1, 1, ancho - 3, alto - 5);
                pincel.setColor(activo || resaltado ? DORADO : BORDE);
                pincel.drawRect(1, 1, ancho - 4, alto - 6);
                pincel.fillRect(4, 4, 3, 3);
                pincel.fillRect(ancho - 8, 4, 3, 3);
                pincel.setColor(new Color(255, 245, 215, 48));
                pincel.drawLine(8, 3, ancho - 9, 3);
            } finally {
                pincel.dispose();
            }
            super.paintComponent(g);
        }
    }

    /** Le da a la tabla el estilo de la mazmorra: celdas oscuras, texto claro y encabezado dorado. */
    private static void estilizarTabla(JTable tabla) {
        tabla.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        tabla.setBackground(FONDO);
        tabla.setForeground(TEXTO);
        tabla.setSelectionBackground(SELECCION);
        tabla.setSelectionForeground(TEXTO);
        tabla.setGridColor(PANEL_CLARO);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setFillsViewportHeight(true);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable origen, Object valor,
                    boolean seleccionada, boolean conFoco, int fila, int columna) {
                super.getTableCellRendererComponent(origen, valor, seleccionada, false, fila, columna);
                setBackground(seleccionada ? SELECCION : FONDO);
                setForeground(TEXTO);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        encabezado.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable origen, Object valor,
                    boolean seleccionada, boolean conFoco, int fila, int columna) {
                super.getTableCellRendererComponent(origen, valor, false, false, fila, columna);
                setFont(origen.getTableHeader().getFont());
                setBackground(PANEL_CLARO);
                setForeground(DORADO);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
                return this;
            }
        });
    }

    /** Barra de desplazamiento fina y oscura, sin flechas, a juego con la tabla. */
    private static void estilizarBarra(JScrollBar barra) {
        barra.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = BORDE;
                trackColor = FONDO;
            }

            @Override
            protected JButton createDecreaseButton(int orientacion) {
                return botonInvisible();
            }

            @Override
            protected JButton createIncreaseButton(int orientacion) {
                return botonInvisible();
            }

            private JButton botonInvisible() {
                JButton boton = new JButton();
                boton.setPreferredSize(new Dimension(0, 0));
                return boton;
            }
        });
        barra.setBackground(FONDO);
        barra.setPreferredSize(new Dimension(12, 0));
    }

    /** El borde de los campos: línea de la mazmorra y un margen para el texto. */
    private static Border bordeDeCampo(int arriba, int izquierda, int abajo, int derecha) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(arriba, izquierda, abajo, derecha));
    }

    /** Desplegable con el mismo estilo que los botones: fondo oscuro, borde recto y flecha dorada. */
    private static final class SelectorMazmorra extends JComboBox<String> {

        private SelectorMazmorra(String[] opciones) {
            super(opciones);
            setUI(new InterfazDelSelector());
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            setBackground(FONDO);
            setForeground(TEXTO);
            setBorder(bordeDeCampo(0, 10, 0, 0));
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> lista, Object valor,
                        int indice, boolean seleccionado, boolean conFoco) {
                    super.getListCellRendererComponent(lista, valor, indice, seleccionado, false);
                    setBackground(seleccionado ? SELECCION : PANEL);
                    setForeground(TEXTO);
                    setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                    return this;
                }
            });
        }
    }

    private static final class InterfazDelSelector extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            BasicArrowButton flecha = new BasicArrowButton(
                    SwingConstants.SOUTH, PANEL_CLARO, PANEL_CLARO, DORADO, PANEL_CLARO);
            flecha.setBorder(BorderFactory.createEmptyBorder());
            return flecha;
        }

        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                @Override
                protected void configurePopup() {
                    super.configurePopup();
                    setBorder(BorderFactory.createLineBorder(DORADO));
                }
            };
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle limites, boolean conFoco) {
            g.setColor(comboBox.isEnabled() ? FONDO : PANEL);
            g.fillRect(limites.x, limites.y, limites.width, limites.height);
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle limites, boolean conFoco) {
            Object valor = comboBox.getSelectedItem();
            if (valor == null) {
                return;
            }
            Graphics2D pincel = (Graphics2D) g.create();
            try {
                pincel.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                pincel.setFont(comboBox.getFont());
                pincel.setColor(TEXTO);
                FontMetrics medidas = pincel.getFontMetrics();
                int base = limites.y + (limites.height + medidas.getAscent() - medidas.getDescent()) / 2;
                pincel.drawString(valor.toString(), limites.x, base);
            } finally {
                pincel.dispose();
            }
        }
    }

    /** Campo de texto con el mismo estilo que los botones y los desplegables. */
    private static final class CampoMazmorra extends JTextField {

        private CampoMazmorra(String contenido) {
            super(contenido);
            setUI(new BasicTextFieldUI());
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            setBackground(FONDO);
            setForeground(TEXTO);
            setCaretColor(DORADO);
            setDisabledTextColor(BORDE);
            setSelectionColor(SELECCION);
            setSelectedTextColor(TEXTO);
            setBorder(bordeDeCampo(6, 10, 6, 10));
        }

        @Override
        public void setEnabled(boolean activo) {
            super.setEnabled(activo);
            setBackground(activo ? FONDO : PANEL);
        }
    }

    private <T> T enHiloGrafico(Supplier<T> accion) {
        if (SwingUtilities.isEventDispatchThread()) {
            return accion.get();
        }

        FutureTask<T> tarea = new FutureTask<>(accion::get);
        SwingUtilities.invokeLater(tarea);
        try {
            return tarea.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió el menú", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("No se pudo mostrar el menú", e.getCause());
        }
    }
}
