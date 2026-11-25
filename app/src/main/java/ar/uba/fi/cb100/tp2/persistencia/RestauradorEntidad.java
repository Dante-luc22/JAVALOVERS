
package ar.uba.fi.cb100.tp2.persistencia;

import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeBotasIgnifugas;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePocion;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePuntos;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeTrampaDePinchos;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeVeneno;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDeZapatosDeAgua;

import ar.uba.fi.cb100.tp2.modelo.mundo.Entidad;

import ar.uba.fi.cb100.tp2.modelo.monstruos.Cazador;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Dormilon;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Errante;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Fantasma;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Ladron;

import ar.uba.fi.cb100.tp2.modelo.partida.AzarPersistible;
import ar.uba.fi.cb100.tp2.modelo.valores.Posicion;

import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.EntidadDTO;
import ar.uba.fi.cb100.tp2.persistencia.dto.PartidaGuardadaDTO.PosicionDTO;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class RestauradorEntidad {

    private static final String TIPO_BOTAS_IGNIFUGAS =
            "CofreDeBotasIgnifugas";

    private static final String TIPO_POCION =
            "CofreDePocion";

    private static final String TIPO_PUNTOS =
            "CofreDePuntos";

    private static final String TIPO_TRAMPA_PINCHOS =
            "CofreDeTrampaDePinchos";

    private static final String TIPO_VENENO =
            "CofreDeVeneno";

    private static final String TIPO_ZAPATOS_AGUA =
            "CofreDeZapatosDeAgua";

    private static final String TIPO_CAZADOR = "Cazador";
    private static final String TIPO_DORMILON = "Dormilon";
    private static final String TIPO_ERRANTE = "Errante";
    private static final String TIPO_FANTASMA = "Fantasma";
    private static final String TIPO_LADRON = "Ladron";

    /**
     * Reconstruye una entidad que no necesita generador aleatorio.
     */
    public Entidad restaurar(EntidadDTO datos) {
        return restaurar(datos, null);
    }

    /**
     * Reconstruye una entidad concreta a partir de su DTO.
     *
     * @param datos información guardada
     * @param azar generador compartido por la partida
     * @return entidad reconstruida
     */
    public Entidad restaurar(
            EntidadDTO datos,
            AzarPersistible azar) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos de la entidad no pueden ser nulos."
            );
        }

        if (datos.tipo == null || datos.tipo.isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de entidad no puede ser nulo ni vacío."
            );
        }

        Posicion posicionActual = convertirPosicion(datos.posicion);

        JsonObject atributos = datos.datos;

        if (atributos == null) {
            throw new IllegalArgumentException(
                    "Los datos específicos de la entidad no pueden ser nulos."
            );
        }

        return switch (datos.tipo) {

            // ================= COFRES =================

            case TIPO_BOTAS_IGNIFUGAS -> {
                validarCamposExactos(atributos);

                yield new CofreDeBotasIgnifugas(posicionActual);
            }

            case TIPO_POCION -> {
                validarCamposExactos(atributos, "cantidad");

                int cantidad = leerEnteroPositivo(
                        atributos, "cantidad"
                );

                yield new CofreDePocion(posicionActual, cantidad);
            }

            case TIPO_PUNTOS -> {
                validarCamposExactos(atributos);

                yield new CofreDePuntos(posicionActual);
            }

            case TIPO_TRAMPA_PINCHOS -> {
                validarCamposExactos(atributos, "danio");

                int danio = leerEnteroPositivo(
                        atributos, "danio"
                );

                yield new CofreDeTrampaDePinchos(
                        posicionActual, danio
                );
            }

            case TIPO_VENENO -> {
                validarCamposExactos(atributos);

                yield new CofreDeVeneno(posicionActual);
            }

            case TIPO_ZAPATOS_AGUA -> {
                validarCamposExactos(atributos);

                yield new CofreDeZapatosDeAgua(posicionActual);
            }

            // ================= MONSTRUOS =================

            case TIPO_CAZADOR -> {
                PosicionesMonstruo posiciones =
                        leerDatosComunesMonstruo(atributos);

                yield new Cazador(
                        posiciones.inicial(),
                        posicionActual,
                        posiciones.danio()
                );
            }

            case TIPO_DORMILON -> {
                validarCamposExactos(
                        atributos,
                        "filaInicial",
                        "columnaInicial",
                        "danio",
                        "radio"
                );

                Posicion inicial = leerPosicionInicial(atributos);
                int danio = leerEnteroPositivo(atributos, "danio");
                int radio = leerEnteroPositivo(atributos, "radio");

                yield new Dormilon(
                        inicial,
                        posicionActual,
                        danio,
                        radio
                );
            }

            case TIPO_ERRANTE -> {
                PosicionesMonstruo posiciones =
                        leerDatosComunesMonstruo(atributos);

                if (azar == null) {
                    throw new IllegalArgumentException(
                            "Se necesita el generador aleatorio "
                                    + "para reconstruir un Errante."
                    );
                }

                yield new Errante(
                        posiciones.inicial(),
                        posicionActual,
                        posiciones.danio(),
                        azar
                );
            }

            case TIPO_FANTASMA -> {
                validarCamposExactos(
                        atributos,
                        "filaInicial",
                        "columnaInicial",
                        "danio",
                        "descansaEsteTurno"
                );

                Posicion inicial = leerPosicionInicial(atributos);
                int danio = leerEnteroPositivo(atributos, "danio");
                boolean descansa = leerBooleano(
                        atributos, "descansaEsteTurno"
                );

                yield new Fantasma(
                        inicial,
                        posicionActual,
                        danio,
                        descansa
                );
            }

            case TIPO_LADRON -> {
                PosicionesMonstruo posiciones =
                        leerDatosComunesMonstruo(atributos);

                if (azar == null) {
                    throw new IllegalArgumentException(
                            "Se necesita el generador aleatorio "
                                    + "para reconstruir un Ladron."
                    );
                }

                yield new Ladron(
                        posiciones.inicial(),
                        posicionActual,
                        posiciones.danio(),
                        azar
                );
            }

            default -> throw new UnsupportedOperationException(
                    "Todavía no está implementada la restauración "
                            + "de la entidad: " + datos.tipo
            );
        };
    }

    /**
     * Agrupa los datos comunes de los monstruos.
     */
    private PosicionesMonstruo leerDatosComunesMonstruo(
            JsonObject datos) {

        validarCamposExactos(
                datos,
                "filaInicial",
                "columnaInicial",
                "danio"
        );

        Posicion inicial = leerPosicionInicial(datos);
        int danio = leerEnteroPositivo(datos, "danio");

        return new PosicionesMonstruo(inicial, danio);
    }

    private Posicion leerPosicionInicial(JsonObject datos) {

        int fila = leerEnteroPositivo(datos, "filaInicial");
        int columna = leerEnteroPositivo(datos, "columnaInicial");

        return new Posicion(fila, columna);
    }

    /**
     * Valida que el JSON tenga exactamente los atributos esperados.
     */
    private void validarCamposExactos(
            JsonObject datos,
            String... camposEsperados) {

        if (datos.size() != camposEsperados.length) {
            throw new IllegalArgumentException(
                    "La cantidad de atributos de la entidad es incorrecta."
            );
        }

        for (String campo : camposEsperados) {
            if (!datos.has(campo) || datos.get(campo).isJsonNull()) {
                throw new IllegalArgumentException(
                        "Falta el atributo obligatorio: " + campo
                );
            }
        }
    }

    private Posicion convertirPosicion(PosicionDTO datos) {

        if (datos == null
                || datos.fila == null
                || datos.columna == null
                || datos.fila <= 0
                || datos.columna <= 0) {

            throw new IllegalArgumentException(
                    "La posición actual de la entidad es inválida."
            );
        }

        return new Posicion(datos.fila, datos.columna);
    }

    private int leerEnteroPositivo(
            JsonObject datos,
            String nombreCampo) {

        if (!datos.has(nombreCampo)) {
            throw new IllegalArgumentException(
                    "Falta el atributo " + nombreCampo + "."
            );
        }

        JsonElement elemento = datos.get(nombreCampo);

        if (elemento == null
                || !elemento.isJsonPrimitive()
                || !elemento.getAsJsonPrimitive().isNumber()) {

            throw new IllegalArgumentException(
                    "El atributo " + nombreCampo
                            + " debe ser un número entero positivo."
            );
        }

        JsonPrimitive primitivo = elemento.getAsJsonPrimitive();

        try {
            int valor = primitivo.getAsInt();

            if (valor <= 0
                    || !primitivo.getAsString().equals(
                    Integer.toString(valor))) {

                throw new IllegalArgumentException(
                        "El atributo " + nombreCampo
                                + " debe ser un entero positivo."
                );
            }

            return valor;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El atributo " + nombreCampo
                            + " no contiene un entero válido.",
                    e
            );
        }
    }

    private boolean leerBooleano(
            JsonObject datos,
            String nombreCampo) {

        JsonElement elemento = datos.get(nombreCampo);

        if (elemento == null
                || !elemento.isJsonPrimitive()
                || !elemento.getAsJsonPrimitive().isBoolean()) {

            throw new IllegalArgumentException(
                    "El atributo " + nombreCampo
                            + " debe ser booleano."
            );
        }

        return elemento.getAsBoolean();
    }

    private record PosicionesMonstruo(
            Posicion inicial,
            int danio) {
    }
}
