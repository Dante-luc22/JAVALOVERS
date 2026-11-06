package ar.uba.fi.cb100.tp2.generador;

/**
 * Contrato de intercambio de generadores definido en el Anexo B.
 */
public interface GeneradorDeLaberinto {

    /**
     * Genera el JSON de un plano a partir de sus parámetros.
     *
     * @param parametrosJson parámetros con el formato del Anexo B
     * @return plano con el formato JSON del Anexo B
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    String generar(String parametrosJson);
}