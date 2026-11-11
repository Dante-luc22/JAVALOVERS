# JAVALOVERS — La fuga de la mazmorra

TP2 de CB100 (Algoritmos y Estructuras de Datos), cátedra Schmidt, 2do cuatrimestre 2026.

## Cómo jugar

Desde la raíz del repo, en PowerShell:

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
.\gradlew.bat run --console=plain
```

En Linux o Mac: `./gradlew run --console=plain`.

1. Elegí la dificultad (por ahora solo Fácil se puede ganar).
2. Escribí una tecla y Enter:

| Tecla | Acción |
|---|---|
| `w` `a` `s` `d` | moverse arriba, izquierda, abajo, derecha |
| `e` | esperar (los monstruos igual se mueven) |
| `u` | usar un objeto (todavía no hace nada) |
| `g` | guardar (todavía no hace nada) |
| `q` | salir |

3. Después de cada paso se escribe `salida/paso-NNNN.bmp`. Abrí el último para ver el mapa:
   gris oscuro pared, gris claro piso, azul agua, naranja lava, verde la salida,
   amarillo los cofres, magenta los monstruos y rojo el héroe. Abajo, la barra de energía.

Se gana pisando la salida y se pierde si la energía llega a 0.
Puntaje: cofres × 100 + energía × 2 − pasos.

## Configuración

`configuracion.json` (en la raíz): energía inicial, costo de cada terreno, semilla
(`null` = mapa distinto cada vez) y el tamaño, cofres y monstruos de cada dificultad.

## Tests

```powershell
.\gradlew.bat test
```