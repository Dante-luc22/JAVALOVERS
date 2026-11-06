package ar.uba.fi.cb100.tp2.vista;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VistaBmp implements Vista {

    private final String directorioSalida;
    //-----CONSTRUCTOR-----
    public VistaBmp(String directorioSalida) {
        this.directorioSalida = directorioSalida;
        File carpeta = new File(directorioSalida);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
    }
    //-----METODOS DE COMPORTAMIENTO-----
    @Override
    public void renderizar(int paso, int[][] matriz) {
        int alto = matriz.length;
        int ancho = matriz[0].length;

        // Crea una imagen en memoria con el tamaño de la matriz
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                // Asigna color según el valor de la celda (1 = negro, 0 = blanco)
                int rgb = (matriz[y][x] == 1) ? Color.BLACK.getRGB() : Color.WHITE.getRGB();
                imagen.setRGB(x, y, rgb);
            }
        }

        // Formatea el nombre con ceros a la izquierda (ej: paso_0001.bmp)
        String nombreArchivo = String.format("%s/paso_%04d.bmp", directorioSalida, paso);
        File archivo = new File(nombreArchivo);

        try {
            ImageIO.write(imagen, "bmp", archivo);
        } catch (IOException e) {
            System.err.println("Error al guardar la imagen BMP del paso " + paso + ": " + e.getMessage());
        }
    }
}