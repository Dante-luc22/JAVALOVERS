
package ar.uba.fi.cb100.tp2.modelo.partida;

import java.util.Map;

import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;

/**
 * La configuración leída de configuracion.json. Es un record porque es un
 * valor: se lee una vez al iniciar y no cambia.
 * Los nombres coinciden con las claves del JSON para que Gson lo lea directo.
 *
 * @param energiaInicial con cuánta energía empieza el héroe
 * @param costos         energía que cuesta entrar a cada terreno ("piso", "agua", "lava")
 * @param semilla        null para que cada partida sea distinta; un número para repetirla
 * @param dificultades   los parámetros de FACIL, MEDIO y DIFICIL
 */
public record Configuracion(int energiaInicial, Map<String, Integer> costos, Long semilla,
                            Map<Dificultad, ParametrosDeDificultad> dificultades) {

    /**
     * @param dificultad la elegida por el jugador
     * @return sus parámetros
     * @throws IllegalArgumentException si el archivo no trae esa dificultad
     */
    public ParametrosDeDificultad parametrosDe(Dificultad dificultad) {
        ParametrosDeDificultad parametros = this.dificultades.get(dificultad);
        if (parametros == null) {
            throw new IllegalArgumentException(
                    "configuracion.json no tiene la dificultad " + dificultad);
        }
        return parametros;
    }
}
