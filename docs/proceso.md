# Algoritmo Factorial con Recursión de Cola

## Definición del Algoritmo

```Scala
def factorial(n: Int): BigInt = {
  @annotation.tailrec
  def loop(x: Int, acumulador: BigInt): BigInt = {
    if (x <= 1) acumulador
    else loop(x - 1, acumulador * x)
  }
  loop(n, 1)
}
```

* La función `factorial` calcula el factorial de un número `n` utilizando **recursión de cola**.
* La función interna `loop` es la que hace la recursión:

  * Recibe dos parámetros:

    * `x`: el valor actual decreciente hasta llegar a 1.
    * `acumulador`: donde se guarda el resultado parcial en cada paso.
* El decorador `@annotation.tailrec` obliga a que la función sea optimizada como recursión de cola, es decir, **no se acumulan llamados en la pila**.

## Explicación paso a paso

### Caso base

```Scala
if (x <= 1) acumulador
```

Cuando `x` llega a `1`, la función retorna directamente el valor acumulado, evitando más llamadas.

### Caso recursivo

```Scala
loop(x - 1, acumulador * x)
```

En cada llamada:

* Se reduce el valor de `x` en 1.
* Se multiplica el acumulador por `x` y se pasa a la siguiente iteración.
* Como es recursión de cola, la llamada recursiva es la **última instrucción** en ejecutarse, lo que permite a Scala optimizar la pila.

---

## Llamados de pila en recursión de cola

Ejemplo:

```Scala
factorial(5)
```

### Paso 1: Llamada inicial

```Scala
loop(5, 1)
```

### Paso 2: Primera iteración

```Scala
loop(4, 5)   // acumulador = 1 * 5
```

### Paso 3: Segunda iteración

```Scala
loop(3, 20)  // acumulador = 5 * 4
```

### Paso 4: Tercera iteración

```Scala
loop(2, 60)  // acumulador = 20 * 3
```

### Paso 5: Cuarta iteración

```Scala
loop(1, 120) // acumulador = 60 * 2
```

### Paso 6: Caso base

```Scala
return 120
```

---

## Diferencia con recursión normal

* En **recursión normal** cada llamada queda en la pila esperando a que termine la siguiente, lo que puede causar desbordamiento si `n` es muy grande.
* En **recursión de cola**, el compilador transforma el proceso en un **bucle optimizado**, por lo que no se guarda cada llamada en la pila y el algoritmo puede ejecutarse para valores muy grandes sin problema.

---

## Ejemplo de uso

```Scala
val resultado = factorial(5)
println(resultado)  // 120
```

El resultado de `factorial(5)` es `120`.


## Diagrama de llamados de pila con recursión de cola

```mermaid
sequenceDiagram
    participant Main as factorial(5)
    participant L1 as loop(5, 1)
    participant L2 as loop(4, 5)
    participant L3 as loop(3, 20)
    participant L4 as loop(2, 60)
    participant L5 as loop(1, 120)

    Main->>L1: llamada inicial
    L1->>L2: tail call con (4, 5)
    L2->>L3: tail call con (3, 20)
    L3->>L4: tail call con (2, 60)
    L4->>L5: tail call con (1, 120)
    L5-->>Main: return 120
```


// PUNTO 1
## Punto 1: un paso de la simulación

Un paso de la simulación hace una sola cosa: cada partícula se mueve según su velocidad. Su posición nueva es la anterior más la velocidad por el tiempo `dt`, y su velocidad y su masa se quedan como estaban. Lo interesante es que el cálculo de una partícula no depende de ninguna otra, y por eso el trabajo se puede repartir. Son cuatro funciones: `mover` hace el cálculo de una partícula, `pasoSegmento` lo hace para un tramo del arreglo, `pasoSecuencial` lo hace para todo el sistema y `pasoParalelo` parte el sistema en tramos que se calculan al mismo tiempo.

Para las trazas usamos un sistema pequeño de cinco partículas, numeradas desde cero. La partícula `i` está en `(i, 0)`, se mueve con velocidad `(1, 1)` y tiene masa 1. Con `dt = 2.0`, cada una debería terminar en `(i + 2, 2)`.

| i | Posición | Velocidad | Masa |
|---|----------|-----------|------|
| 0 | `(0, 0)` | `(1, 1)` | 1.0 |
| 1 | `(1, 0)` | `(1, 1)` | 1.0 |
| 2 | `(2, 0)` | `(1, 1)` | 1.0 |
| 3 | `(3, 0)` | `(1, 1)` | 1.0 |
| 4 | `(4, 0)` | `(1, 1)` | 1.0 |

### mover

```scala
def mover(p: Particula, dt: Double): Particula =
  Particula(p.pos + p.vel * dt, p.vel, p.masa)
```

Es el cálculo de una sola partícula, y los demás se apoyan en él. Primero multiplica la velocidad por `dt`, después le suma ese desplazamiento a la posición, y arma una partícula nueva con esa posición y con la misma velocidad y la misma masa. No se modifica ninguna partícula: cada llamado devuelve una nueva.

Traza con la partícula 3 y `dt = 2.0`:

| Paso | Operación | Resultado |
|------|-----------|-----------|
| 1 | `p.vel * dt` | `(1, 1) * 2.0 = (2, 2)` |
| 2 | `p.pos + (2, 2)` | `(3, 0) + (2, 2) = (5, 2)` |
| 3 | se arma la partícula nueva | `Particula((5, 2), (1, 1), 1.0)` |

### pasoSegmento

```scala
def pasoSegmento(s: Sistema, dt: Double, desde: Int, hasta: Int): Sistema = {
  val inicio = math.max(desde, 0)
  val fin = math.min(hasta, s.length)
  Array.tabulate(math.max(fin - inicio, 0))(i => mover(s(inicio + i), dt))
}
```

Avanza solo el tramo que empieza en `desde`, incluido, y termina en `hasta`, excluido, y devuelve un arreglo con solo esas partículas ya movidas. Funciona en tres pasos:

1. Recorta el tramo a los límites del arreglo, por si `desde` es negativo o `hasta` se pasa del final.
2. Calcula cuántas partículas tiene el tramo, que es `fin - inicio`, o cero si el tramo quedó vacío o al revés.
3. `Array.tabulate` arma un arreglo de esa longitud, y en la posición `i` pone la partícula `inicio + i` del sistema, ya movida con `mover`.

Traza con `pasoSegmento(s, 2.0, 1, 4)` sobre el sistema de arriba:

| Paso | Qué se calcula | Valor |
|------|----------------|-------|
| 1 | `inicio` y `fin` | `inicio = 1` y `fin = 4`, que están dentro del arreglo |
| 2 | largo del tramo | `4 - 1 = 3` |
| 3 | posición 0 del resultado | `mover(s(1), 2.0)`, que da `(3, 2)` |
| 4 | posición 1 del resultado | `mover(s(2), 2.0)`, que da `(4, 2)` |
| 5 | posición 2 del resultado | `mover(s(3), 2.0)`, que da `(5, 2)` |

El resultado tiene tres partículas, que son la 1, la 2 y la 3 ya movidas. La 0 y la 4 no aparecen porque quedaron fuera del tramo. Con `desde` igual a `hasta`, el largo es cero y el resultado es un arreglo vacío. Con `hasta = 100` sobre este sistema de cinco partículas, el paso 1 lo recorta a 5.

### pasoSecuencial

```scala
def pasoSecuencial(s: Sistema, dt: Double): Sistema =
  pasoSegmento(s, dt, 0, s.length)
```

El paso secuencial es el segmento que cubre todo el sistema, desde la posición 0 hasta el final. Se escribió así a propósito: el enunciado advierte que, si la versión secuencial y la paralela calculan cada tramo de maneras distintas, la diferencia de tiempos mide dos operaciones diferentes y no solo el reparto. Con esta definición, las dos versiones usan exactamente la misma función para calcular cada tramo.

Traza con `pasoSecuencial(s, 2.0)`: es `pasoSegmento(s, 2.0, 0, 5)`, que calcula las cinco partículas y devuelve las posiciones `(2, 2)`, `(3, 2)`, `(4, 2)`, `(5, 2)` y `(6, 2)`, que es lo que esperábamos.

### pasoParalelo

```scala
def pasoParalelo(s: Sistema, dt: Double, umbral: Int): Sistema = {
  val limite = math.max(umbral, 1)
  // Si todo el sistema cabe en el umbral no se reparte nada: se resuelve igual
  // que la versión secuencial.
  if (s.length <= limite) {
    pasoSegmento(s, dt, 0, s.length)
  } else {
    def repartir(desde: Int, hasta: Int): Sistema =
      if (hasta - desde <= limite) pasoSegmento(s, dt, desde, hasta)
      else {
        val medio = desde + (hasta - desde) / 2
        val (izquierda, derecha) =
          parallel(repartir(desde, medio), repartir(medio, hasta))
        izquierda ++ derecha
      }

    repartir(0, s.length)
  }
}
```

Esta función tiene cuatro partes.

La primera es `limite`, que es el umbral pero nunca menor que uno. Si el umbral fuera cero, un tramo de un solo elemento se seguiría partiendo para siempre, porque su mitad izquierda quedaría vacía y su mitad derecha sería ese mismo tramo.

La segunda es una revisión inicial: si todo el sistema cabe en el umbral, es decir, si `s.length` no pasa de `limite`, no hay nada que repartir y se calcula de una vez con `pasoSegmento(s, dt, 0, s.length)`, que es justo lo que hace `pasoSecuencial`. Esta revisión no cambia lo que se calcula, porque si se saltara, el primer llamado de `repartir` caería en su caso base y haría esa misma llamada. Lo que hace es dejar explícito el caso en que no se reparte nada, que es el caso de control de las mediciones.

La tercera es la función interna `repartir`, que solo se define cuando el sistema sí pasa del umbral. Trabaja sobre el tramo que va de `desde` a `hasta`, y tiene dos casos:

* Si el tramo no pasa del umbral, se calcula directamente con `pasoSegmento`. Es el caso base: ahí no se reparte nada.
* Si el tramo pasa del umbral, se calcula el punto medio, `desde + (hasta - desde) / 2`, y se parten dos tramos: de `desde` a `medio` y de `medio` a `hasta`. Con `parallel`, la mitad derecha se entrega como una tarea para que la haga otro hilo, mientras el hilo que llama se ocupa de la izquierda. Cuando las dos terminan, se pegan con `izquierda ++ derecha`.

La cuarta es la llamada inicial, `repartir(0, s.length)`, que arranca con el sistema completo.

La división del medio se escribe como `desde + (hasta - desde) / 2` y no como `(desde + hasta) / 2` para que la suma no se pase del límite de un entero cuando el arreglo es muy grande. Cuando el tramo tiene tamaño impar, la mitad izquierda se queda con el piso de la mitad y la derecha con el resto, por ejemplo 2 y 3 para un tramo de 5.

Traza con el sistema de cinco partículas, `dt = 2.0` y `umbral = 2`. Cada fila es un llamado a `repartir`:

| Llamado | Tramo | Tamaño | ¿Pasa del umbral (2)? | Qué hace |
|---------|-------|--------|-----------------------|----------|
| R1 | de 0 a 5 | 5 | sí | `medio = 0 + 5/2 = 2`: parte en los tramos de 0 a 2 y de 2 a 5 |
| R2 | de 0 a 2 | 2 | no | resuelve con `pasoSegmento` y devuelve P0 y P1 ya movidas |
| R3 | de 2 a 5 | 3 | sí | `medio = 2 + 3/2 = 3`: parte en los tramos de 2 a 3 y de 3 a 5 |
| R4 | de 2 a 3 | 1 | no | resuelve con `pasoSegmento` y devuelve P2 ya movida |
| R5 | de 3 a 5 | 2 | no | resuelve con `pasoSegmento` y devuelve P3 y P4 ya movidas |

Aquí `Pi` es la partícula `i` y `Pi'` es la misma partícula ya movida, que queda en `(i + 2, 2)`. Estos tramos no son una suposición: los registramos ejecutando la función real y quedaron exactamente así.

```mermaid
flowchart TD
    A["R1: tramo de 0 a 5<br/>tamaño 5 mayor que 2: se parte"]
    A --> B["R2: tramo de 0 a 2<br/>tamaño 2: pasoSegmento"]
    A --> C["R3: tramo de 2 a 5<br/>tamaño 3 mayor que 2: se parte"]
    C --> D["R4: tramo de 2 a 3<br/>tamaño 1: pasoSegmento"]
    C --> E["R5: tramo de 3 a 5<br/>tamaño 2: pasoSegmento"]
```

Las hojas del árbol son R2, R4 y R5, y son los llamados que de verdad calculan partículas. Sus tramos, de 0 a 2, de 2 a 3 y de 3 a 5, tienen 2, 1 y 2 partículas, que suman las 5 del sistema, y ninguna aparece en dos hojas. Esto es justo lo que se demuestra en el informe de corrección.

El reparto se hace hacia abajo, partiendo el tramo. Los resultados se combinan hacia arriba, y en ese orden:

| Paso | Qué se une | Resultado |
|------|------------|-----------|
| 1 | R4 y R5, con `++` | `P2', P3', P4'`, que es el resultado del tramo de 2 a 5 |
| 2 | R2 y el resultado del paso 1, con `++` | `P0', P1', P2', P3', P4'` |

Con las posiciones, el resultado final es `(2, 2)`, `(3, 2)`, `(4, 2)`, `(5, 2)` y `(6, 2)`, igual que el de `pasoSecuencial`.

```mermaid
flowchart TD
    B["R2: P0', P1'"] --> G["R2 ++ R3<br/>P0', P1', P2', P3', P4'"]
    D["R4: P2'"] --> F["R4 ++ R5<br/>P2', P3', P4'"]
    E["R5: P3', P4'"] --> F
    F --> G
```

Lo que importa de la unión es el orden: siempre va la izquierda antes que la derecha. Si en algún `++` se invirtieran, las partículas quedarían en posiciones cambiadas, aunque ninguna se perdiera.

Cómo cambia el reparto con el umbral, para un sistema de cinco y de siete partículas. Los tramos son los de las hojas del árbol, registrados al ejecutar la función real:

| Partículas | Umbral | Tramos que se calculan directamente |
|------------|--------|--------------------------------------|
| 5 | 1 | de 0 a 1, de 1 a 2, de 2 a 3, de 3 a 4, de 4 a 5 (cinco tramos) |
| 5 | 2 | de 0 a 2, de 2 a 3, de 3 a 5 (tres tramos) |
| 5 | 5 o más | de 0 a 5 (un solo tramo: no se reparte nada) |
| 7 | 2 | de 0 a 1, de 1 a 3, de 3 a 5, de 5 a 7 (cuatro tramos) |
| 5 | 0 | igual que con umbral 1, porque el umbral nunca baja de uno |

Con un umbral que cubre todo el sistema, que es el caso de control que pide el enunciado, la revisión inicial lo detecta y la función nunca se parte: calcula directamente el tramo de 0 a `n`, que es exactamente lo que hace `pasoSecuencial`. Por eso en ese caso las dos versiones hacen el mismo trabajo, y la aceleración medida tiene que dar 1.

Una última observación: cada vez que se parte un tramo se lanza una tarea nueva para su mitad derecha, y cada `++` copia los elementos de las dos mitades en un arreglo nuevo. Mientras más pequeño es el umbral, más tareas se lanzan y más veces se copian los datos. Ese costo del reparto es el que el informe de paralelización compara contra lo que se ahorra.


// PUNTO 2


// PARTICIPANTE 3
## Punto 3: magnitudes globales en paralelo (energiaParalela y centroDeMasaParalelo)

Las dos funciones reparten el sistema con parallel igual que pasoParalelo: un tramo $[desde, hasta)$ se parte por la mitad mientras tenga más elementos que el umbral (y más de uno, para que con umbral $\le 0$ la recursión termine). Cuando el tramo ya no se parte, un acumular de cola lo recorre por índice y devuelve su resultado parcial. Después cada nivel combina los dos resultados parciales.

| Función | Resultado parcial de un tramo | Cómo se combinan dos tramos | Paso final |
|---|---|---|---|
| energiaParalela | $E = \sum \tfrac{1}{2} m_i \lVert v_i \rVert^2$ | $E_{izq} + E_{der}$ | ninguno |
| centroDeMasaParalelo | pareja $(W, M)$ con $W = \sum m_i\, p_i$ y $M = \sum m_i$ | $(W_{izq} + W_{der},\; M_{izq} + M_{der})$ | $W / M$, una sola vez; origen si $M = 0$ |

### Ejemplo pequeño

Sistema de tres partículas y umbral = 1:

| i | pos | vel | masa | $\tfrac{1}{2} m \lVert v \rVert^2$ | $m \cdot pos$ |
|---|---|---|---|---|---|
| 0 | $(1, 2)$ | $(3, 4)$ | $1$ | $12.5$ | $(1, 2)$ |
| 1 | $(5, 6)$ | $(0, 1)$ | $3$ | $1.5$ | $(15, 18)$ |
| 2 | $(-3, 0)$ | $(-1, -1)$ | $4$ | $4.0$ | $(-12, 0)$ |

**Traza de energiaParalela(s, 1)**

1. energiaTramo(0, 3): el tramo tiene 3 elementos, $3 > 1$, entonces medio = 0 + 3/2 = 1 y se llama parallel(energiaTramo(0, 1), energiaTramo(1, 3)).
2. energiaTramo(0, 1): tiene 1 elemento, no se parte; acumular suma la partícula 0 y devuelve $12.5$.
3. energiaTramo(1, 3): tiene 2 elementos, $2 > 1$, entonces medio = 1 + 2/2 = 2 y se llama parallel(energiaTramo(1, 2), energiaTramo(2, 3)), que devuelven $1.5$ y $4.0$. Se combinan: $1.5 + 4.0 = 5.5$.
4. De vuelta en el primer llamado: $12.5 + 5.5 = 18.0$.
   **Traza de centroDeMasaParalelo(s, 1)**: los tramos son los mismos, pero cada uno devuelve la pareja $(W, M)$.

1. parcial(0, 1) $= ((1, 2),\ 1)$.
2. parcial(1, 2) $= ((15, 18),\ 3)$ y parcial(2, 3) $= ((-12, 0),\ 4)$; combinados, parcial(1, 3) $= ((3, 18),\ 7)$.
3. parcial(0, 3) $= ((1, 2) + (3, 18),\ 1 + 7) = ((4, 20),\ 8)$.
4. Se divide una sola vez: $(4, 20) / 8 = (0.5,\ 2.5)$.
   Si en cambio se promediaran los centros de cada mitad, se obtendría $\tfrac{1}{2}\big((1, 2) + (\tfrac{3}{7}, \tfrac{18}{7})\big) = (\tfrac{5}{7}, \tfrac{16}{7}) \approx (0.71,\ 2.29)$, que no es el centro de masa $(0.5,\ 2.5)$: las dos mitades no pesan igual.

### Diagrama de los llamados

```mermaid
flowchart TD
    A["tramo [0, 3): se parte, medio = 1"] --> B["tramo [0, 1): acumular<br/>E = 12.5 ; (W, M) = ((1, 2), 1)"]
    A --> C["tramo [1, 3): se parte, medio = 2"]
    C --> D["tramo [1, 2): acumular<br/>E = 1.5 ; (W, M) = ((15, 18), 3)"]
    C --> F["tramo [2, 3): acumular<br/>E = 4.0 ; (W, M) = ((-12, 0), 4)"]
    D --> G["combina [1, 3)<br/>E = 5.5 ; (W, M) = ((3, 18), 7)"]
    F --> G
    B --> H["combina [0, 3)<br/>E = 18.0 ; (W, M) = ((4, 20), 8)"]
    G --> H
    H --> I["resultado: energía 18.0 ; centro = W / M = (0.5, 2.5)"]
```

### Casos especiales

- **Sistema vacío:** energiaTramo(0, 0) no se parte y acumular devuelve $0.0$. En el centro de masa, $M = 0$ y se devuelve el origen.
- **umbral $\le 0$ o mayor que el sistema:** con $umbral \le 0$ el tramo se parte hasta quedar de un elemento (la condición $hasta - desde > 1$ detiene la recursión); con un umbral mayor o igual al tamaño nada se parte y el resultado se obtiene en un solo acumular.

- // PUNTO 4


// PUNTO 5

## Punto 5: medir la aceleración

El punto 5 compara el tiempo de `pasoSecuencial` con el de `pasoParalelo` para
sistemas grandes, y escoge el umbral que obtuvo el menor tiempo. Las dos
funciones del punto no reparten trabajo; lo que tiene proceso es la medición
de `App.scala`, que las usa. Los tiempos que salieron están en
`paralelizacion.md`.

### `aceleracion`

```Scala
def aceleracion(tSecuencial: Double, tParalelo: Double): Double =
  tSecuencial / tParalelo
```

Traza con `aceleracion(100.0, 25.0)`:

1. Se evalúan los argumentos: `tSecuencial = 100.0` y `tParalelo = 25.0`.
2. Se divide: `100.0 / 25.0`.
3. Resultado: `4.0`, la versión paralela tardó la cuarta parte.

Con `aceleracion(100.0, 100.0)` el cociente es `1.0`: el reparto no ganó ni
perdió. Con `aceleracion(50.0, 200.0)` es `0.25`: repartir costó más de lo que
ahorró.

### `umbralConveniente`

```Scala
def umbralConveniente(s: Sistema, dt: Double, umbrales: List[Int]): Int =
  if (umbrales.isEmpty) s.length
  else {
    val tiempos = umbrales.map(u => (medir(5)(pasoParalelo(s, dt, u)), u))
    tiempos.minBy(_._1)._2
  }
```

Ejemplo con un sistema de 1 000 partículas y `umbrales = List(1000, 100, 10)`.
Los tiempos de la traza son supuestos, solo para mostrar cómo se combinan:

| Paso | Qué ocurre | Resultado |
|---|---|---|
| 1 | `umbrales.isEmpty` es falso: se sigue por la rama `else` | |
| 2 | `map` mide `pasoParalelo(s, dt, 1000)`: no se parte nada | `(0.9, 1000)` |
| 3 | `map` mide `pasoParalelo(s, dt, 100)`: tres particiones | `(0.4, 100)` |
| 4 | `map` mide `pasoParalelo(s, dt, 10)`: siete particiones | `(0.6, 10)` |
| 5 | `minBy(_._1)` se queda con la pareja de menor tiempo | `(0.4, 100)` |
| 6 | `._2` descarta el tiempo y deja el umbral | `100` |

Con la lista vacía devuelve `s.length` sin medir nada, que es el umbral que
equivale a no repartir.

```mermaid
flowchart TD
    A["umbralConveniente(s, dt, umbrales)"] --> B{"¿umbrales vacía?"}
    B -- sí --> C["devuelve s.length"]
    B -- no --> D["para cada u: medir pasoParalelo(s, dt, u)"]
    D --> E["parejas (tiempo, u)"]
    E --> F["minBy: la pareja de menor tiempo"]
    F --> G["devuelve su u"]
```

### Equipo utilizado

| Componente | Especificación |
|---|---|
| Procesador | AMD Ryzen 5 5600H with Radeon Graphics, 3,30 GHz |
| Núcleos físicos | 6 |
| Hilos / procesadores lógicos | 12 |
| Memoria RAM | 16 GB DDR4 a 3200 MT/s (13,9 GB utilizables) |
| Sistema | Windows de 64 bits, arquitectura x64 |

La simulación es un cálculo de CPU; la GPU integrada no interviene en el
reparto de tareas.

### Procedimiento de medición en `App.scala`

1. Se construye el arreglo de partículas para cada tamaño (1 000 000, 2 000 000
   y 3 000 000) a partir del índice de cada una, con `dt = 0.5`. El programa
   lee los 12 procesadores lógicos con `Runtime.getRuntime.availableProcessors`.
2. Antes de cada medición se llama a `System.gc()`, para que una pausa del
   recolector de basura no caiga en medio del tiempo que se mide.
3. El secuencial y el control se miden en pares: `pasoSecuencial` y
   `pasoParalelo` con umbral igual a $n$. En cinco pares, el orden de los dos
   se alterna, para que el orden de ejecución no favorezca a ninguno. Cada
   tiempo es la mediana de esas cinco mediciones, hechas con
   `withWarmer(new Warmer.Default) measure`.
4. Se mide `pasoParalelo` con los demás umbrales de
   `List(n, n / 2, n / 8, 100000, 10000, 1000).distinct`, con la mediana de
   cinco mediciones.
5. Para cada umbral se calcula la aceleración con `aceleracion` y la estimada
   con `trabajo(n) / tiempoEstimado(n, núcleos, umbral)`.
6. Se toma el mayor valor de aceleración de la tabla, se llama a
   `umbralConveniente`, que vuelve a medir los candidatos por separado, y se
   calcula la fracción que no se reparte con la ley de Amdahl.

La fila con umbral igual a $n$ es el control: no divide el arreglo, así que su
aceleración debería dar cerca de 1. La columna «Particiones» indica cuántas
divisiones por la mitad se hacen antes de llegar al umbral; no es el número de
procesadores.

```mermaid
sequenceDiagram
    participant App
    participant Medir as medir (ScalaMeter)
    participant Sec as pasoSecuencial
    participant Par as pasoParalelo

    App->>Medir: pasoSecuencial(sistema, dt)
    Medir->>Sec: calentar y medir
    Medir-->>App: tSecuencial (mediana)
    loop para cada umbral
        App->>Medir: pasoParalelo(sistema, dt, umbral)
        Medir->>Par: calentar y medir
        Medir-->>App: tParalelo (mediana)
        App->>App: aceleracion(tSecuencial, tParalelo)
    end
```

### Traza de la división

Como ejemplo pequeño, considérese un sistema de 8 partículas y un umbral de 2.
La llamada inicial es `repartir(0, 8)`:

```text
repartir(0, 8)
├── repartir(0, 4)
│   ├── repartir(0, 2)  → pasoSegmento(s, dt, 0, 2)
│   └── repartir(2, 4)  → pasoSegmento(s, dt, 2, 4)
│   → combina los resultados [0,2) y [2,4)
└── repartir(4, 8)
    ├── repartir(4, 6)  → pasoSegmento(s, dt, 4, 6)
    └── repartir(6, 8)  → pasoSegmento(s, dt, 6, 8)
    → combina los resultados [4,6) y [6,8)
→ combina los resultados de [0,4) y [4,8)
```

En cada división, `parallel` recibe las llamadas de los dos segmentos. Cuando
el tamaño de un segmento es menor o igual al umbral, deja de dividirse y se
procesa con `pasoSegmento`. Los arreglos parciales se concatenan en el mismo
orden mediante `izquierda ++ derecha`.