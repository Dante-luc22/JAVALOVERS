package ar.uba.fi.cb100.tp2.vista;

import java.util.Scanner;

public class EntradaConsola {

    private final Scanner scanner;
    //-----CONSTRUCTOR-----
    public EntradaConsola() {
        this.scanner = new Scanner(System.in);}
    //-----METODOS DE COMPORTAMIENTO-----
    /**
     * Muestra una indicación y lee una línea de texto de la terminal.
     *
     * @param mensaje Mensaje a mostrar al usuario.
     * @return Cadena ingresada sin espacios al inicio ni al final.
     */
    public String leerTexto(String mensaje) {
        System.out.print(mensaje + " ");
        return scanner.nextLine().trim();
    }

    /**
     * Muestra una indicación y garantiza la lectura de un entero válido.
     * Reintenta hasta que el usuario ingrese un número.
     *
     * @param mensaje Mensaje a mostrar al usuario.
     * @return Número entero ingresado.
     */
    public int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje + " ");
            try {
                String entrada = scanner.nextLine().trim();
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor, ingrese un número entero válido.");
            }
        }
    }

    /**
     * Lee un número entero asegurando que esté dentro del rango [min, max].
     *
     * @param mensaje Mensaje a mostrar al usuario.
     * @param min Valor mínimo aceptado.
     * @param max Valor máximo aceptado.
     * @return Entero validado dentro del rango.
     */
    public int leerEnteroEnRango(String mensaje, int min, int max) {
        while (true) {
            int valor = leerEntero(mensaje);
            if (valor >= min && valor <= max) {
                return valor;
            }
            System.out.println("Error: El número debe estar entre " + min + " y " + max + ".");
        }
    }

    /**
     * Cierra el recurso Scanner al finalizar.
     */
    public void cerrar() {
        scanner.close();
    }
}