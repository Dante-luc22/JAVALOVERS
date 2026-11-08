package ar.uba.fi.cb100.tp2.aplicacion;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import ar.uba.fi.cb100.tp2.modelo.partida.ParametrosDeDificultad;
import ar.uba.fi.cb100.tp2.modelo.valores.Dificultad;
import ar.uba.fi.cb100.tp2.generador.GeneradorDeLaberinto;
import ar.uba.fi.cb100.tp2.modelo.cofres.CatalogoDeCofres;
import ar.uba.fi.cb100.tp2.modelo.cofres.CofreDePuntos;
import ar.uba.fi.cb100.tp2.modelo.monstruos.CatalogoDeMonstruos;
import ar.uba.fi.cb100.tp2.modelo.monstruos.Errante;
import ar.uba.fi.cb100.tp2.modelo.mundo.Terreno;
import ar.uba.fi.cb100.tp2.modelo.partida.Configuracion;
import ar.uba.fi.cb100.tp2.construccion.CargadorDePlano;
import ar.uba.fi.cb100.tp2.modelo.heroe.Heroe;
import ar.uba.fi.cb100.tp2.modelo.mundo.Mazmorra;
import ar.uba.fi.cb100.tp2.modelo.mundo.Plano;
import ar.uba.fi.cb100.tp2.modelo.partida.Partida;

/**
 * Arma una partida lista para jugar: genera los planos, los valida y carga,
 * los junta en la mazmorra y pone al héroe en la entrada.
 */
public class CreadorPartida {

    /** Energía que quitan los monstruos básicos al alcanzar al héroe (regla 6). */
    private static final int DANIO_DE_LOS_BASICOS = 30;

    private final Configuracion configuracion;
    private final GeneradorDeLaberinto generador;

    /**
     * @param configuracion la configuración leída de configuracion.json
     * @param generador     el generador de planos: el propio o el de otro grupo
     */
    public CreadorPartida(Configuracion configuracion, GeneradorDeLaberinto generador) {
        this.configuracion = configuracion;
        this.generador = generador;
    }

        /**
     * Arma una partida nueva: genera un plano por cada tablero y piso, los valida
     * y carga, los junta en la mazmorra y pone al héroe en la entrada.
     * Solo el último plano tiene la salida.
     * @param dificultad la elegida por el jugador
     * @return la partida lista para jugar
     */
    public Partida crear(Dificultad dificultad) {
        ParametrosDeDificultad parametros = this.configuracion.parametrosDe(dificultad);
        long semilla = resolverSemilla();
        Random azar = new Random(semilla);
        CargadorDePlano cargador = new CargadorDePlano(
                crearCatalogoDeCofres(), crearCatalogoDeMonstruos(azar), azar);

        int cantidadDePlanos = parametros.tableros() * parametros.pisos();
        List<Plano> planos = new ArrayList<>();
        for (int indice = 0; indice < cantidadDePlanos; indice++) {
            boolean conSalida = (indice == cantidadDePlanos - 1);
            String pedido = parametrosDelPlano(dificultad, parametros, semilla + indice,
                    repartir(parametros.cofres(), cantidadDePlanos, indice),
                    repartir(parametros.monstruos(), cantidadDePlanos, indice),
                    conSalida);
            planos.add(cargador.cargar(this.generador.generar(pedido)));
        }

        Mazmorra mazmorra = new Mazmorra(planos, parametros.pisos());
        Heroe heroe = new Heroe(mazmorra.getEntrada(), this.configuracion.energiaInicial());
        return new Partida(mazmorra, heroe, costosPorTerreno());
    }

    /**
     * Con semilla en la configuración, la usa: la misma semilla da el mismo mundo.
     * Con semilla null, sortea una, así cada partida es distinta.
     * @return la semilla de esta partida
     */
    private long resolverSemilla() {
        if (this.configuracion.semilla() != null) {
            return this.configuracion.semilla();
        }
        return new Random().nextLong();
    }

    /**
     * Pasa los costos del JSON ("piso", "agua", "lava") al enum Terreno.
     * @return cuánta energía cuesta entrar a cada terreno
     */
    private Map<Terreno, Integer> costosPorTerreno() {
        Map<Terreno, Integer> costos = new EnumMap<>(Terreno.class);
        costos.put(Terreno.PISO, this.configuracion.costos().get("piso"));
        costos.put(Terreno.AGUA, this.configuracion.costos().get("agua"));
        costos.put(Terreno.LAVA, this.configuracion.costos().get("lava"));
        return costos;
    }

    /**
     * @param azar el Random de la partida: el errante lo usa para moverse
     * @return el catálogo con los tipos de monstruo del juego
     */
    private CatalogoDeMonstruos crearCatalogoDeMonstruos(Random azar) {
        CatalogoDeMonstruos catalogo = new CatalogoDeMonstruos();
        catalogo.registrar("ERRANTE", posicion -> new Errante(posicion, DANIO_DE_LOS_BASICOS, azar));
        return catalogo;
    }

    /** @return el catálogo con los tipos de cofre del juego */
    private CatalogoDeCofres crearCatalogoDeCofres() {
        CatalogoDeCofres catalogo = new CatalogoDeCofres();
        catalogo.registrar("PUNTOS", CofreDePuntos::new);
        return catalogo;

        
    }

        /**
     * Arma el JSON que recibe el generador para un plano (formato del Anexo B.2).
     * @param dificultad la elegida por el jugador
     * @param parametros el tamaño del plano, según la dificultad
     * @param semilla    la semilla de este plano
     * @param cofres     cuántos cofres poner en este plano
     * @param monstruos  cuántos monstruos poner en este plano
     * @param conSalida  si este es el plano que tiene la salida
     * @return el JSON de entrada del generador
     */
    private String parametrosDelPlano(Dificultad dificultad, ParametrosDeDificultad parametros,
                                      long semilla, int cofres, int monstruos, boolean conSalida) {
        JsonObject json = new JsonObject();
        json.addProperty("version", 1);
        json.addProperty("dificultad", dificultad.name());
        json.addProperty("filas", parametros.filas());
        json.addProperty("columnas", parametros.columnas());
        json.addProperty("semilla", semilla);
        json.addProperty("cofres", cofres);
        json.addProperty("monstruos", monstruos);
        json.addProperty("conSalida", conSalida);
        return json.toString();
    }

    /**
     * Reparte un total (de cofres o de monstruos) entre los planos, lo más parejo
     * posible: los primeros planos se llevan el sobrante, de a uno.
     * @param total            cuántos hay en todo el mundo
     * @param cantidadDePlanos entre cuántos planos se reparten
     * @param indice           qué plano es, desde 0
     * @return cuántos le tocan a ese plano
     */
    private static int repartir(int total, int cantidadDePlanos, int indice) {
        int base = total / cantidadDePlanos;
        int sobrante = total % cantidadDePlanos;
        if (indice < sobrante) {
            return base + 1;
        }
        return base;
    }


}
