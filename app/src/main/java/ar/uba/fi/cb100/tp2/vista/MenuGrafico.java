package ar.uba.fi.cb100.tp2.vista;

import ar.uba.fi.cb100.tp2.modelo.partida.EstadoPartida;
import ar.uba.fi.cb100.tp2.modelo.ranking.EntradaDeRanking;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
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

    /** Lo que elige el jugador en el diálogo del final de la partida. */
    public enum Decision {
        /** Jugar otra partida con las mismas opciones. */
        REINTENTAR,
        /** Volver al menú de inicio para elegir otras opciones. */
        MENU_PRINCIPAL,
        /** Terminar el juego, también si cierra el diálogo. */
        SALIR
    }

    /**
     * Muestra el final de la partida con los botones "Guardar en el ranking",
     * "Reintentar", "Menú principal" y "Salir". El de guardar pide un nombre de
     * exactamente tres caracteres, se lo pasa a {@code guardarEnRanking} y
     * desaparece: el puntaje de una partida se guarda una sola vez.
     *
     * @param estado           cómo terminó la partida
     * @param puntaje          el puntaje final
     * @param guardarEnRanking qué hacer con el nombre elegido, que ya es válido
     * @return lo que eligió el jugador; SALIR si la partida sigue en curso
     */
    public Decision mostrarResultado(EstadoPartida estado, int puntaje, Consumer<String> guardarEnRanking) {
        if (estado == EstadoPartida.EN_CURSO) {
            return Decision.SALIR;
        }

        return enHiloGrafico(() -> {
            String titulo = estado == EstadoPartida.GANADA ? "¡Ganaste!" : "Perdiste";
            String nombreGuardado = null;
            while (true) {
                boolean yaGuardado = nombreGuardado != null;
                JPanel contenido = panelConTitulo(titulo);
                String detalle = "Puntaje: " + puntaje
                        + (yaGuardado ? "   (guardado como " + nombreGuardado + ")" : "");
                contenido.add(new JLabel(detalle), BorderLayout.CENTER);
                Object[] botones = yaGuardado
                        ? new Object[]{"Reintentar", "Menú principal", "Salir"}
                        : new Object[]{"Guardar en el ranking", "Reintentar", "Menú principal", "Salir"};
                int opcion = JOptionPane.showOptionDialog(null, contenido, "Fin de la partida",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, botones, botones[0]);

                if (opcion < 0 || opcion >= botones.length) {
                    return Decision.SALIR;
                }
                if ("Guardar en el ranking".equals(botones[opcion])) {
                    Optional<String> nombre = pedirNombre();
                    if (nombre.isPresent()) {
                        guardarEnRanking.accept(nombre.get());
                        nombreGuardado = nombre.get();
                    }
                    continue;
                }
                if ("Reintentar".equals(botones[opcion])) {
                    return Decision.REINTENTAR;
                }
                if ("Menú principal".equals(botones[opcion])) {
                    return Decision.MENU_PRINCIPAL;
                }
                return Decision.SALIR;
            }
        });
    }

    /**
     * Pide el nombre del ranking hasta que tenga exactamente tres caracteres,
     * como en un arcade. Devuelve vacío si el jugador cancela.
     */
    private Optional<String> pedirNombre() {
        String aviso = "Tu nombre para el ranking (" + EntradaDeRanking.LARGO_DEL_NOMBRE
                + " caracteres, sin espacios):";
        while (true) {
            String texto = JOptionPane.showInputDialog(null, aviso, "Ranking", JOptionPane.PLAIN_MESSAGE);
            if (texto == null) {
                return Optional.empty();
            }
            String nombre = texto.strip();
            if (EntradaDeRanking.esNombreValido(nombre)) {
                return Optional.of(nombre);
            }
            JOptionPane.showMessageDialog(null,
                    "El nombre tiene que tener exactamente " + EntradaDeRanking.LARGO_DEL_NOMBRE
                            + " caracteres, sin espacios.",
                    "Nombre inválido", JOptionPane.WARNING_MESSAGE);
        }
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
