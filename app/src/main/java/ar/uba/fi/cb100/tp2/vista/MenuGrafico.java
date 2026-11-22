package ar.uba.fi.cb100.tp2.vista;

import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Presenta las opciones de inicio y el resultado de una partida. */
public final class MenuGrafico {

    public enum ModoVista {
        GRAFICA, BMP
    }

    public record OpcionesInicio(Dificultad dificultad, ModoVista modoVista) {
    }

    /** Devuelve las opciones elegidas, o vacío al salir o cerrar el menú. */
    public Optional<OpcionesInicio> mostrarInicio() {
        return enHiloGrafico(() -> {
            Dificultad[] dificultades = {Dificultad.FACIL, Dificultad.MEDIO, Dificultad.DIFICIL};
            JComboBox<String> dificultad = new JComboBox<>(new String[]{"Fácil", "Medio", "Difícil"});
            JComboBox<String> presentacion = new JComboBox<>(new String[]{"Gráfica", "BMP (consola)"});

            JPanel campos = new JPanel(new GridLayout(2, 2, 12, 8));
            JLabel etiquetaDificultad = new JLabel("Dificultad");
            etiquetaDificultad.setLabelFor(dificultad);
            campos.add(etiquetaDificultad);
            campos.add(dificultad);
            JLabel etiquetaVista = new JLabel("Vista");
            etiquetaVista.setLabelFor(presentacion);
            campos.add(etiquetaVista);
            campos.add(presentacion);

            JPanel contenido = panelConTitulo("La fuga de la mazmorra");
            contenido.add(campos, BorderLayout.CENTER);
            Object[] botones = {"Jugar", "Salir"};
            int opcion = JOptionPane.showOptionDialog(null, contenido, "Nueva partida",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, botones, botones[0]);

            if (opcion != 0) {
                return Optional.empty();
            }

            ModoVista modo = presentacion.getSelectedIndex() == 0 ? ModoVista.GRAFICA : ModoVista.BMP;
            return Optional.of(new OpcionesInicio(dificultades[dificultad.getSelectedIndex()], modo));
        });
    }

    /** Devuelve true cuando el jugador elige iniciar otra partida con las mismas opciones. */
    public boolean mostrarResultado(EstadoPartida estado, int puntaje) {
        if (estado == EstadoPartida.EN_CURSO) {
            return false;
        }

        return enHiloGrafico(() -> {
            String titulo = estado == EstadoPartida.GANADA ? "¡Ganaste!" : "Perdiste";
            JPanel contenido = panelConTitulo(titulo);
            contenido.add(new JLabel("Puntaje: " + puntaje), BorderLayout.CENTER);
            Object[] botones = {"Reintentar", "Salir"};
            int opcion = JOptionPane.showOptionDialog(null, contenido, "Fin de la partida",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, botones, botones[0]);
            return opcion == 0;
        });
    }

    private JPanel panelConTitulo(String titulo) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel encabezado = new JLabel(titulo);
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 22f));
        panel.add(encabezado, BorderLayout.NORTH);
        return panel;
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
