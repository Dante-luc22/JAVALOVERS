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
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Shape;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;

/** Presenta el menú principal, las opciones de inicio y el resultado de la partida. */
public final class MenuGrafico {

    private static final Color FONDO = new Color(12, 17, 25);
    private static final Color PANEL = new Color(19, 26, 36);
    private static final Color BORDE = new Color(119, 110, 100);
    private static final Color TEXTO = new Color(249, 239, 217);
    private static final Color DORADO = new Color(229, 177, 99);

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

        private final JDialog ventana = new JDialog((Frame) null, "Escape de la mazmorra", true);
        private final CardLayout distribucion = new CardLayout();
        private final JPanel tarjetas = new JPanel(distribucion);
        private final JPanel ranking = new JPanel(new BorderLayout());
        private OpcionesInicio resultado;

        private Navegacion(Long semillaConfigurada) {
            tarjetas.setOpaque(false);
            ranking.setOpaque(false);
            tarjetas.add(inicio(), Pantalla.INICIO.name());
            tarjetas.add(jugar(semillaConfigurada), Pantalla.JUGAR.name());
            tarjetas.add(enciclopedia(), Pantalla.ENCICLOPEDIA.name());
            tarjetas.add(opciones(), Pantalla.OPCIONES.name());
            tarjetas.add(ranking, Pantalla.RANKING.name());

            JPanel contenido = new FondoMazmorra();
            contenido.add(new TituloMazmorra(), BorderLayout.NORTH);
            contenido.add(tarjetas, BorderLayout.CENTER);
            contenido.setPreferredSize(new Dimension(920, 530));

            ventana.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            ventana.setContentPane(contenido);
            ventana.pack();
            ventana.setMinimumSize(new Dimension(760, 510));
            ventana.setLocationRelativeTo(null);
        }

        private Optional<OpcionesInicio> mostrar() {
            ventana.setVisible(true);
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
            JComboBox<String> mapa = new JComboBox<>(
                    new String[]{"De configuración", "Aleatorio", "Semilla personalizada"}
            );
            JTextField semilla = new JTextField(
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
            JComboBox<String> vista = new JComboBox<>(new String[]{"Gráfica", "BMP (consola)"});
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
            JTabbedPane pestanas = new JTabbedPane();
            pestanas.addTab("Monstruos", ficha("""
                    ERRANTE — Se mueve al azar por las celdas transitables.

                    CAZADOR — Busca un camino hacia el héroe.

                    DORMILÓN — Persigue al héroe cuando lo detecta cerca.

                    FANTASMA — Puede atravesar paredes y se mueve en turnos alternos.

                    LADRÓN — Si alcanza al héroe, puede robarle un objeto del inventario.
                    """));
            pestanas.addTab("Cofres y objetos", ficha("""
                    COFRE DE PUNTOS — Suma los 100 puntos de apertura.

                    POCIÓN — Recupera energía al abrir el cofre.

                    PINCHOS — Quitan energía al abrir el cofre.

                    VENENO — Aumenta temporalmente el costo de movimiento.

                    ZAPATOS DE AGUA — Al usarlos, el agua cuesta como el piso.

                    BOTAS IGNÍFUGAS — Al usarlas, la lava cuesta como el piso.
                    """));
            return pagina("Enciclopedia", pestanas);
        }

        private void abrirRanking() {
            ranking.removeAll();
            ranking.add(pagina("Ranking", contenidoRanking()), BorderLayout.CENTER);
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
                estado.setText(mejores.isEmpty() ? "Todavía no hay partidas registradas." : " ");
            };
            dificultad.addActionListener(e -> actualizar.run());
            actualizar.run();

            contenido.add(filtro, BorderLayout.NORTH);
            contenido.add(new JScrollPane(tabla), BorderLayout.CENTER);
            contenido.add(estado, BorderLayout.SOUTH);
            return contenido;
        }

        private JPanel pagina(String titulo, Component cuerpo, JButton... acciones) {
            JPanel pagina = new JPanel(new BorderLayout(0, 14));
            pagina.setBackground(PANEL);
            pagina.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE),
                    BorderFactory.createEmptyBorder(15, 18, 14, 18)
            ));
            pagina.setPreferredSize(new Dimension(550, 296));
            pagina.add(texto(titulo, 22), BorderLayout.NORTH);
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
        return new JComboBox<>(new String[]{"Fácil", "Medio", "Difícil"});
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
        detalle.setFont(detalle.getFont().deriveFont(14f));
        detalle.setBorder(BorderFactory.createEmptyBorder(12, 14, 10, 14));
        detalle.setCaretPosition(0);
        return new JScrollPane(detalle);
    }

    private static JButton boton(String texto, boolean principal, Runnable accion) {
        JButton boton = new BotonMazmorra(texto, principal);
        boton.addActionListener(e -> accion.run());
        return boton;
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

        private TituloMazmorra() {
            setPreferredSize(new Dimension(0, 145));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D pincel = (Graphics2D) g.create();
            try {
                pincel.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                pincel.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Font fuente = new Font("Georgia", Font.BOLD, 54);
                Shape letras = fuente.createGlyphVector(pincel.getFontRenderContext(), TITULO).getOutline();
                var caja = letras.getBounds2D();
                double escala = Math.min(1.0, (getWidth() - 84.0) / caja.getWidth());
                double x = (getWidth() - caja.getWidth() * escala) / 2 - caja.getX() * escala;
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
                int centro = getWidth() / 2;
                pincel.setColor(new Color(12, 14, 21));
                pincel.fillRect(centro - ancho / 2 - 14, 105, ancho + 28, 24);
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
                int ancho = getWidth();
                int alto = getHeight();
                boolean resaltado = getModel().isRollover() || isFocusOwner();
                Color arriba = principal ? new Color(121, 76, 39) : new Color(58, 67, 81);
                Color abajo = principal ? new Color(66, 40, 29) : new Color(33, 40, 51);
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
                pincel.setColor(principal || resaltado ? DORADO : BORDE);
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
