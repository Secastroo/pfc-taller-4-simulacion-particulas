# Informe de paralelización — Punto 5: medición de la aceleración

**Asignatura:** Fundamentos de Programación Funcional y Concurrente  
**Proyecto:** Simulación de partículas  
**Punto:** 5 — Medición de la aceleración

## 1. Estrategia de paralelización

La simulación avanza cada partícula durante un intervalo `dt`. Para cada partícula, `mover` construye una nueva partícula cuya posición es la posición anterior más la velocidad multiplicada por `dt`.

`pasoParalelo` comienza con el intervalo completo `[0, s.length)`. Mientras el tamaño del segmento sea mayor que el umbral, calcula el punto medio y divide el intervalo en dos mitades. Las dos llamadas recursivas se ejecutan mediante la abstracción `parallel`:

```scala
val medio = desde + (hasta - desde) / 2
val (izquierda, derecha) =
  parallel(repartir(desde, medio), repartir(medio, hasta))
izquierda ++ derecha
```

Cuando el segmento tiene un tamaño menor o igual al umbral, deja de dividirse y se procesa secuencialmente mediante `pasoSegmento`. Los resultados parciales se combinan con `++`, conservando el orden de las partículas.

Un umbral alto genera menos divisiones y menos tareas. Un umbral bajo genera más divisiones, pero también aumenta los costos de creación y coordinación de tareas y de combinación de arreglos.

## 2. Equipo y condiciones de medición

| Componente | Especificación |
|---|---|
| Procesador | AMD Ryzen 5 5600H with Radeon Graphics, 3,30 GHz |
| Núcleos físicos | 6 |
| Procesadores lógicos / hilos | 12 |
| RAM | 16 GB instalados (13,9 GB utilizables) |
| Sistema operativo | Windows de 64 bits, arquitectura x64 |

Scala informó 12 procesadores lógicos disponibles. No deben confundirse con 12 núcleos físicos: el procesador tiene 6 núcleos físicos y 12 hilos lógicos.

Se utilizó `dt = 0.5`, arreglos de 1, 2 y 3 millones de partículas y la mediana de cinco mediciones de ScalaMeter por tiempo. La aplicación empleó `withWarmer(new Warmer.Default) measure`. El control se midió alternando el orden de ejecución secuencial y paralelo sin particiones.

## 3. Tiempos y aceleraciones medidos

La aceleración se define como:

\[
A = \frac{T_s}{T_p}
\]

Las tablas transcriben la última ejecución de `App`. La columna «Aceleración estimada» proviene del modelo del punto 4; no es una medición de tiempo.

### 1.000.000 de partículas

Tiempo secuencial: **26,9 ms**.

| Umbral | Particiones | Paralela (ms) | Aceleración medida | Aceleración estimada |
|---:|---:|---:|---:|---:|
| 1.000.000 (control) | 0 | 51,3 | 0,52 | 0,92 |
| 500.000 | 1 | 17,9 | 1,50 | 1,71 |
| 125.000 | 3 | 78,1 | 0,34 | 4,80 |
| 100.000 | 4 | 74,7 | 0,36 | 6,86 |
| 10.000 | 7 | 71,4 | 0,38 | 10,97 |
| 1.000 | 10 | 86,1 | 0,31 | 11,86 |

El umbral con mayor aceleración de la tabla fue **500.000 (1,50)**. `umbralConveniente` seleccionó el mismo umbral.

### 2.000.000 de partículas

Tiempo secuencial: **127,4 ms**.

| Umbral | Particiones | Paralela (ms) | Aceleración medida | Aceleración estimada |
|---:|---:|---:|---:|---:|
| 2.000.000 (control) | 0 | 130,7 | 0,98 | 0,92 |
| 1.000.000 | 1 | 75,1 | 1,70 | 1,71 |
| 250.000 | 3 | 115,7 | 1,10 | 4,80 |
| 100.000 | 5 | 131,0 | 0,97 | 8,73 |
| 10.000 | 8 | 129,2 | 0,99 | 11,46 |
| 1.000 | 11 | 125,2 | 1,02 | 11,93 |

El umbral con mayor aceleración de la tabla fue **1.000.000 (1,70)**. `umbralConveniente` seleccionó el mismo umbral.

### 3.000.000 de partículas

Tiempo secuencial: **175,2 ms**.

| Umbral | Particiones | Paralela (ms) | Aceleración medida | Aceleración estimada |
|---:|---:|---:|---:|---:|
| 3.000.000 (control) | 0 | 65,5 | 2,68 | 0,92 |
| 1.500.000 | 1 | 128,8 | 1,36 | 1,71 |
| 375.000 | 3 | 141,0 | 1,24 | 4,80 |
| 100.000 | 5 | 194,9 | 0,90 | 8,73 |
| 10.000 | 9 | 158,7 | 1,10 | 11,72 |
| 1.000 | 12 | 157,3 | 1,11 | 11,96 |

El mayor valor aparente fue el control sin particiones (**2,68**), y `umbralConveniente` seleccionó el umbral 3.000.000. Como este caso no crea tareas paralelas, el resultado no puede atribuirse a la paralelización.

## 4. Comparación con el modelo del punto 4

El modelo estima la aceleración a partir del trabajo y la profundidad del árbol de divisiones. En general, predice aceleraciones crecientes al disminuir el umbral y aumentar las oportunidades de ejecutar segmentos en paralelo. Por ejemplo, para un millón de partículas, estima 1,71 con umbral 500.000 y 11,86 con umbral 1.000.

Las mediciones no siguen esa tendencia: en los tres tamaños, los umbrales pequeños dieron resultados bajos o cercanos a 1, mientras que los mejores valores se observaron con cero o una división. Esto puede deberse a que el modelo simplificado no incluye el costo de crear y coordinar tareas, concatenar arreglos, asignar memoria, recolectar basura ni las limitaciones de ancho de banda de memoria. Además, el modelo ideal no incorpora las condiciones variables de ejecución de la JVM y del sistema operativo.

La aceleración estimada del control es 0,92 en el modelo. No debe confundirse con la aceleración medida: el control real debería estar cerca de 1 porque no divide el arreglo.

## 5. Interpretación y límites de los resultados

En esta ejecución, los mejores umbrales observados fueron 500.000 para un millón de partículas y 1.000.000 para dos millones. Para tres millones, el mayor valor fue el control sin particiones, lo cual es una anomalía y no evidencia una ventaja del paralelismo.

El control produjo aceleraciones de 0,52, 0,98 y 2,68 para uno, dos y tres millones de partículas. Como el mismo caso de control debería ser comparable con `pasoSecuencial`, las diferencias de 0,52 y 2,68 indican que las mediciones no fueron suficientemente estables. Entre las causas posibles están la compilación JIT, la recolección de basura, la presión de memoria, la carga de otros procesos y la variación de las condiciones de la máquina durante las pruebas.

Por esa razón, los resultados se deben presentar como observaciones de una ejecución concreta. No es correcto afirmar que el umbral 3.000.000 acelera la ejecución paralela, ni considerar definitivas las mejores aceleraciones hasta obtener controles repetibles cercanos a 1.

La aceleración puede dejar de crecer al agregar procesadores porque una parte del cálculo no se reparte y porque la coordinación y el movimiento de datos tienen un costo. La ley de Amdahl expresa ese límite. Usando la mejor aceleración observada de cada tabla y \(p=12\), la fracción serial estimada sería:

\[
f = \frac{\frac{1}{A}-\frac{1}{p}}{1-\frac{1}{p}}
\]

| Partículas | Aceleración usada | Fracción serial estimada |
|---:|---:|---:|
| 1.000.000 | 1,50 | 0,635 |
| 2.000.000 | 1,70 | 0,552 |
| 3.000.000 | 2,68 | 0,317 |

Estas fracciones son únicamente indicativas: en el tercer caso, la aceleración utilizada corresponde al control sin particiones y, en los otros tamaños, la variabilidad del control reduce la confianza en la estimación. No deben interpretarse como una medición precisa de la fracción serial del algoritmo.

## 6. Conclusión

La estrategia divide recursivamente el arreglo en dos segmentos, ejecuta las ramas con `parallel`, detiene la división al alcanzar el umbral y combina los arreglos parciales con concatenación. El umbral afecta el costo de coordinación y, por tanto, el tiempo observado.

La ejecución muestra que un número mayor de particiones no garantiza una aceleración mayor. Sin embargo, las anomalías en el control impiden extraer conclusiones definitivas sobre el umbral óptimo. Para validar el rendimiento sería necesario repetir las mediciones con controles estables y cercanos a 1, registrar las muestras individuales y controlar mejor las condiciones de ejecución.