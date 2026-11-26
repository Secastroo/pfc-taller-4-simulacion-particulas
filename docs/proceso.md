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


// PUNTO 2


// PUNTO 3


// PUNTO 4


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