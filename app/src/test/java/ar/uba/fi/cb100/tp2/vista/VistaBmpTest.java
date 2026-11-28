package ar.uba.fi.cb100.tp2.vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.mundo.Tablero;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import ar.uba.fi.cb100.tp2.modelo.valores.Ubicacion;

/**
 * Tests de VistaBmp: el archivo de cada paso y su tamaño en píxeles.
 */
public class VistaBmpTest {

    private static final int FILAS = 5;
    private static final int COLUMNAS = 7;

    @TempDir
    Path carpeta;

    private Partida partida;
    private VistaBmp vista;

    /**
     * Un plano de 5x7 todo pared, con un pasillo de piso en la fila 2,
     * y el héroe parado en la entrada (2, 2).
     */
    @BeforeEach
    void armarPartida() {
        Tablero tablero = new Tablero(FILAS, COLUMNAS);
        for (int columna = 2; columna <= 6; columna++) {
            tablero.setTerreno(new Posicion(2, columna), Terreno.PISO);
        }
        Plano plano = new Plano(tablero, new Posicion(2, 2), new Posicion(2, 6));
        Mazmorra mazmorra = new Mazmorra(List.of(plano), 1);
        Heroe heroe = new Heroe(mazmorra.getEntrada(), 100);
        this.partida = new Partida(mazmorra, heroe,
                Map.of(Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 10));
        this.vista = new VistaBmp(this.carpeta, 100, new EntradaConsola());
    }

    @Test
    void despuesDeMostrarExisteElArchivoDelPaso() {
        this.vista.mostrar(this.partida);

        assertTrue(Files.exists(this.carpeta.resolve("paso-0000.bmp")));
    }

    @Test
    void elBmpTieneElTamanioDelPlanoMasLaBarraDeEnergia() throws IOException {
        this.vista.mostrar(this.partida);

        BufferedImage imagen = ImageIO.read(this.carpeta.resolve("paso-0000.bmp").toFile());
        assertEquals(COLUMNAS * VistaBmp.TAMANIO_CELDA, imagen.getWidth());
        assertEquals((FILAS + 1) * VistaBmp.TAMANIO_CELDA, imagen.getHeight());
    }

    @Test
    void elHeroeSeDibujaEnSuCelda() throws IOException {
        this.vista.mostrar(this.partida);

        BufferedImage imagen = ImageIO.read(this.carpeta.resolve("paso-0000.bmp").toFile());
        int centroX = (2 - 1) * VistaBmp.TAMANIO_CELDA + VistaBmp.TAMANIO_CELDA / 2;
        int centroY = (2 - 1) * VistaBmp.TAMANIO_CELDA + VistaBmp.TAMANIO_CELDA / 2;
        assertEquals(Color.RED.getRGB(), imagen.getRGB(centroX, centroY));
    }
    @Test
    void elBmpDibujaLasEscalerasDelPlanoActual() throws IOException {
        Partida partida = crearPartidaConDosPisos(1);
        this.vista = new VistaBmp(this.carpeta, 100, new EntradaConsola());

        this.vista.mostrar(partida);

        BufferedImage imagen = ImageIO.read(this.carpeta.resolve("paso-0000.bmp").toFile());

        int x = (4 - 1) * VistaBmp.TAMANIO_CELDA + 4;
        int y = (2 - 1) * VistaBmp.TAMANIO_CELDA + 3;

        assertEquals(Color.CYAN.getRGB(), imagen.getRGB(x, y));
    }
    @Test
    void elBmpDibujaLosPasillosDelPlanoActual() throws IOException {
        Partida partida = crearPartidaConDosTableros();

        this.vista.mostrar(partida);

        BufferedImage imagen = ImageIO.read(this.carpeta.resolve("paso-0000.bmp").toFile());

        int x = (4 - 1) * VistaBmp.TAMANIO_CELDA + 8;
        int y = (2 - 1) * VistaBmp.TAMANIO_CELDA + 8;

        assertEquals(Color.PINK.getRGB(), imagen.getRGB(x, y));
    }

    @Test
    void noAceptaEnergiaMaximaCero() {
        assertThrows(IllegalArgumentException.class,
                () -> new VistaBmp(this.carpeta, 0, new EntradaConsola()));
    }

        @Test
    void alCrearseBorraLosPasosDeUnaPartidaAnteriorYNadaMas() throws IOException {
        Path pasoViejo = this.carpeta.resolve("paso-0042.bmp");
        Path otroArchivo = this.carpeta.resolve("notas.txt");
        Files.writeString(pasoViejo, "viejo");
        Files.writeString(otroArchivo, "no tocar");

        new VistaBmp(this.carpeta, 100, new EntradaConsola());

        assertFalse(Files.exists(pasoViejo));
        assertTrue(Files.exists(otroArchivo));
    }
    @Test
    void muestraTableroYPisoActualEnConsola() {
        Partida partida = crearPartidaConDosPisos(2);
        this.vista = new VistaBmp(this.carpeta, 100, new EntradaConsola());

        ByteArrayOutputStream captura = new ByteArrayOutputStream();
        PrintStream salidaOriginal = System.out;
        System.setOut(new PrintStream(captura));

        try {
            this.vista.mostrar(partida);
        } finally {
            System.setOut(salidaOriginal);
        }

        assertTrue(captura.toString().contains("tablero 1"));
        assertTrue(captura.toString().contains("piso 2"));
    }
    @Test
    void identificaTableroYPisoActualEnElBmp() throws IOException {
        Partida partida = crearPartidaConDosPisos(2);
        this.vista.mostrar(partida);
        BufferedImage imagen = ImageIO.read(
                this.carpeta.resolve("paso-0000.bmp").toFile()
        );
        int inicioBarra = FILAS * VistaBmp.TAMANIO_CELDA;
        boolean tieneIdentificacion = false;

        for (int y = inicioBarra; y < imagen.getHeight(); y++) {
            for (int x = 0; x < imagen.getWidth(); x++) {
                if (imagen.getRGB(x, y) == Color.WHITE.getRGB()) {
                    tieneIdentificacion = true;
                    break;
                }
            }
            if (tieneIdentificacion) {
                break;
            }
        }
        assertTrue(tieneIdentificacion, "El BMP debe identificar el tablero y el piso actual");
    }

    private Partida crearPartidaConDosPisos(int pisoHeroe) {
        Tablero tablero1 = crearTablero();
        Tablero tablero2 = crearTablero();
        Plano plano1 = new Plano(tablero1, new Posicion(2, 2), null);
        Plano plano2 = new Plano(tablero2, new Posicion(2, 2), new Posicion(2, 6));
        Mazmorra mazmorra = new Mazmorra(List.of(plano1, plano2), 2);
        mazmorra.conectar(
                new Ubicacion(1, 1, new Posicion(2, 4)),
                new Ubicacion(1, 2, new Posicion(2, 4))
        );
        Heroe heroe = new Heroe(
                new Ubicacion(1, pisoHeroe, new Posicion(2, 2)), 100
        );
        return new Partida(mazmorra, heroe, Map.of(
                Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 10
        ));
    }

    private Partida crearPartidaConDosTableros() {
        Tablero tablero1 = crearTablero();
        Tablero tablero2 = crearTablero();
        Plano plano1 = new Plano(tablero1, new Posicion(2, 2), null);
        Plano plano2 = new Plano(tablero2, new Posicion(2, 2), new Posicion(2, 6));
        Mazmorra mazmorra = new Mazmorra(List.of(plano1, plano2), 1);
        mazmorra.conectar(
                new Ubicacion(1, 1, new Posicion(2, 4)),
                new Ubicacion(2, 1, new Posicion(2, 4))
        );
        Heroe heroe = new Heroe(
                new Ubicacion(1, 1, new Posicion(2, 2)), 100
        );
        return new Partida(mazmorra, heroe, Map.of(
                Terreno.PISO, 1, Terreno.AGUA, 3, Terreno.LAVA, 10
        ));
    }

    private Tablero crearTablero() {
        Tablero tablero = new Tablero(FILAS, COLUMNAS);

        for (int columna = 2; columna <= 6; columna++) {
            tablero.setTerreno(new Posicion(2, columna), Terreno.PISO);
        }

        return tablero;
    }
}
