# Ejemplo de informe de corrección

Fundamentos de Programación Funcional y Concurrente.
Documento realizado por el docente Juan Francisco Díaz.

## 1. Argumentar la corrección de programas recursivos

Sea $f : A \to B$ una función, y $A$ un conjunto definido recursivamente
(recordar la definición de Matemáticas Discretas I), como por ejemplo los
naturales o las listas.

Sea $P_f$ un programa recursivo (lineal o en árbol) desarrollado en Scala (o en
cualquier lenguaje de programación) hecho para calcular $f$:

```scala
def Pf(a: A): B = { // Pf recibe a de tipo A, y devuelve f(a) de tipo B
  ...
}
```

¿Cómo argumentar que $P_f(a)$ siempre devuelve $f(a)$ como respuesta? Es decir,
¿cómo argumentar que $P_f$ es correcto con respecto a su especificación?

La respuesta es sencilla: demostrando el siguiente teorema.

```math
\forall a \in A : P_f(a) == f(a)
```

Cuando uno tiene que demostrar que algo se cumple para todos los elementos de
un conjunto definido recursivamente, es natural usar inducción estructural. En
términos prácticos, esto significa demostrar que:

- Para cada valor básico $a$ de $A$, se tiene que $P_f(a) == f(a)$.
- Para cada valor $a \in A$ construido recursivamente a partir de otro(s)
  valor(es) $a' \in A$, se tiene que
  $P_f(a') == f(a') \rightarrow P_f(a) == f(a)$. (Esta es la hipótesis de
  inducción).

### Ejemplo: factorial recursivo

Sea $f : \mathbb{N} \to \mathbb{N}$ la función que calcula el factorial de un
número natural, es decir, $f(n) = n!$. Y sea $P_f$ el siguiente programa en
Scala:

```scala
def Pf(n: Int): Int = { // Pf recibe n de tipo Int, y devuelve n! de tipo Int
  if (n == 0) 1 else n * Pf(n - 1)
}
```

Vamos a demostrar que $\forall n \in \mathbb{N} : P_f(n) == n!$

**Caso base:** $n = 0$

```math
P_f(0) \rightarrow \text{if } (0 == 0)\ 1 \text{ else } 0 \ast P_f(-1) \rightarrow 1
```

Por otro lado, $f(0) = 0! = 1$. Entonces $P_f(0) == f(0)$.

**Caso de inducción:** $n = k + 1$, $k \geq 0$. Hay que demostrar:
$P_f(k) == f(k) \rightarrow P_f(k + 1) == f(k + 1)$

```math
P_f(k+1) \rightarrow \text{if } (k+1 == 0)\ 1 \text{ else } (k+1) \ast P_f(k) \rightarrow (k+1) \ast P_f(k)
```

Usando la hipótesis de inducción (HI):

```math
\rightarrow (k+1) \ast k! = (k+1)!
```

Por lo tanto, $P_f(k + 1) == f(k + 1)$.

Concluimos por inducción que $\forall n \in \mathbb{N} : P_f(n) == n!$

### Ejemplo: el máximo de una lista

Sea $f : \text{List}[\mathbb{N}] \to \mathbb{N}$ la función que calcula el
máximo de una lista de enteros positivos, no vacía. Y sea $P_f$ el siguiente
programa en Scala:

```scala
def maxLin(l: List[Int]): Int = {
  if (l.tail.isEmpty) l.head
  else math.max(maxLin(l.tail), l.head)
}
```

Demostraremos que:

```math
\forall n \in \mathbb{N} \setminus \{0\} : P_f(\text{List}(a_1, a_2, \ldots, a_n)) == f(\text{List}(a_1, a_2, \ldots, a_n))
```

**Caso base:** $n = 1$

```math
P_f(\text{List}(a_1)) \rightarrow \text{if } \text{List}(a_1).\text{tail.isEmpty then } \text{List}(a_1).\text{head else } \ldots \rightarrow \text{List}(a_1).\text{head} \rightarrow a_1
```

Por otro lado, $f(\text{List}(a_1)) = a_1$. Entonces
$P_f(\text{List}(a_1)) == f(\text{List}(a_1))$.

**Caso de inducción:** $n = k + 1$, $k \geq 1$. Se debe demostrar:

```math
P_f(\text{List}(b_1, b_2, \ldots, b_k)) == f(\text{List}(b_1, b_2, \ldots, b_k)) \rightarrow P_f(\text{List}(a_1, a_2, \ldots, a_{k+1})) == f(\text{List}(a_1, a_2, \ldots, a_{k+1}))
```

Empecemos por calcular qué devuelve $P_f$ usando el modelo de sustitución:

```math
P_f(L) \rightarrow \text{if } L.\text{tail.isEmpty then } L.\text{head else math.max}(P_f(L.\text{tail}), L.\text{head})
```

```math
\rightarrow \text{math.max}(P_f(\text{List}(a_2, \ldots, a_{k+1})), a_1)
```

Sea $b = P_f(\text{List}(a_2, \ldots, a_{k+1}))$; por la hipótesis de
inducción, $b = f(\text{List}(a_2, \ldots, a_{k+1}))$. Hay dos posibilidades:

- Si $\text{math.max}(b, a_1) = b$, entonces $b \geq a_1$ y
 $b == f(\text{List}(a_1, a_2, \ldots, a_{k+1}))$.
- Si $\text{math.max}(b, a_1) = a_1$, entonces $a_1 \geq b$ y
 $a_1 == f(\text{List}(a_1, a_2, \ldots, a_{k+1}))$.

Por lo tanto, $P_f(L) == f(L)$.

Concluimos por inducción que:

```math
\forall n \in \mathbb{N} \setminus \{0\} : P_f(\text{List}(a_1, a_2, \ldots, a_n)) == f(\text{List}(a_1, a_2, \ldots, a_n))
```

## 2. Argumentar la corrección de programas iterativos

Para argumentar la corrección de programas iterativos, se debe formalizar cómo
es la iteración. Esto implica definir:

- Cómo se representa un estado de la iteración, $s$.
- Cuál es el estado inicial, $s_0$.
- Cuál es el estado final (o cómo se reconoce que un estado es final): $s_f$.
- Qué condición (o predicado) cumple todo estado: $\text{Inv}(s)$ (invariante
  de la iteración).
- El mecanismo para pasar de un estado al siguiente: $\text{transformar}(s)$.
  Si $s_i$ es el estado $i$, entonces $\text{transformar}(s_i) = s_{i+1}$.

Un programa iterativo tiene la siguiente forma:

```scala
def Pf(a: A): B = { // Pf recibe a de tipo A, y devuelve f(a) de tipo B
  def Pf_iter(s: Estado): B =
    if (esFinal(s)) respuesta(s) else Pf_iter(transformar(s))
  Pf_iter(s0)
}
```

Demostración de corrección:

- $\text{Inv}(s_0)$: el estado inicial cumple la condición invariante.
- Si $(s_i \neq s_f \land \text{Inv}(s_i)) \rightarrow \text{Inv}(\text{transformar}(s_i))$:
  el nuevo estado cumple la condición invariante si el estado anterior la
  cumplía.
- De lo anterior se concluye $\text{Inv}(s_f)$, es decir, el estado final
  cumple la condición invariante. Luego,
  $\text{Inv}(s_f) \rightarrow \text{respuesta}(s_f) == f(a)$.
- Finalmente, demostrar que siempre se llega al estado final $s_f$. Esto
  implica que
  $P_f(a) == \text{iter}(s_0) == \text{respuesta}(s_f) == f(a)$.

### Ejemplo: factorial iterativo

Considere el siguiente programa iterativo en Scala para calcular la función
factorial:

```scala
def Pf(n: Int): Int = { // Pf recibe n de tipo Int, y devuelve n! de tipo Int
  def Pf_iter(i: Int, n: Int, ac: Int): Int =
    if (i > n) ac else Pf_iter(i + 1, n, i * ac)
  Pf_iter(1, n, 1)
}
```

Este programa implementa el siguiente proceso iterativo:

- Un estado $s = (i, n, ac)$.
- El estado inicial es $s_0 = (1, n, 1)$.
- $(i, n, ac)$ es final si $i > n$, o lo que es lo mismo, si $i = n + 1$.
- La invariante de ciclo es
  $\text{Inv}(i, n, ac) \equiv i \leq n + 1 \land ac = (i-1)!$.
  La invariante de ciclo es una relación que SIEMPRE se cumple en el ciclo.
- $\text{transformar}((i, n, ac)) = (i+1, n, i \ast ac)$.

Ahora, demostramos los puntos mencionados:

**1.** $\text{Inv}(s_0)$: el estado inicial cumple la condición invariante.

```math
s_0 = (1, n, 1) \implies 1 \leq n + 1 \land 1 = 0!
```

**2.** La invariante se mantiene con la transformación de estados,
$(s_i \neq s_f \land \text{Inv}(s_i)) \rightarrow \text{Inv}(\text{transformar}(s_i))$:

1. Primer cambio, $i = i + 1$, lo que implica $ac = ((i+1) - 1)! = i!$.
2. Segundo cambio, $ac = i \ast ac$, entonces $ac = (i - 1)! \ast i = i!$.
3. Como se puede ver en ambos cambios indicados en la transformación, la
   invariante se mantiene.

**3.** $\text{Inv}(s_f) \rightarrow \text{respuesta}(s_f) == f(a)$

```math
(n + 1 \leq n + 1) \land ac = ((n+1)-1)! \rightarrow ac == n!
```

**4.** En cada paso, la componente $i$ del estado incrementa, acercándose a $n+1$.
Después de $n$ iteraciones, se alcanza $n+1$.

Esto implica que $P_f(n) == \text{iter}(1, n, 1) == n!$

### Ejemplo: el máximo de una lista

Se desea calcular el máximo de una lista de enteros positivos, no vacía. Sea
$f : \text{List}[\mathbb{N}] \to \mathbb{N}$ la función que calcula ese valor.
Y sea $P_f$ el siguiente programa en Scala:

```scala
def maxIt(l: List[Int]): Int = {
  def maxAux(max: Int, l: List[Int]): Int = {
    if (l.isEmpty) max
    else maxAux(math.max(max, l.head), l.tail)
  }
  maxAux(l.head, l.tail)
}
```

Este programa implementa el siguiente proceso iterativo:

- Un estado $s = (max, l)$ donde $l = \text{List}(a_i, a_{i+1}, \ldots, a_k)$
  es una cola de $L$.
- El estado inicial es
  $s_0 = (L.\text{head}, L.\text{tail}) = (a_1, \text{List}(a_2, \ldots, a_k))$.
- $s = (max, l)$ es final si $l$ es vacía.
- $\text{Inv}(max, l) \equiv l = \text{List}(a_i, a_{i+1}, \ldots, a_k) \land max = f(\text{List}(a_1, a_2, \ldots, a_{i-1}))$.
- $\text{transformar}((max, l)) = (nmax, l.\text{tail})$ donde $nmax = max$ si
  $max \geq l.\text{head}$, y $nmax = l.\text{head}$ si no.

Demostración de los puntos:

**1.** $\text{Inv}(s_0)$: el estado inicial cumple la condición invariante.

```math
s_0 = (a_1, \text{List}(a_2, \ldots, a_k)) \implies a_1 = f(\text{List}(a_1))
```

**2.** $(s_i \neq s_f \land \text{Inv}(s_i)) \rightarrow \text{Inv}(\text{transformar}(s_i))$

```math
\neg\, l.\text{isEmpty} \land l = \text{List}(a_i, a_{i+1}, \ldots, a_k) \land max = f(\text{List}(a_1, a_2, \ldots, a_{i-1}))
```

```math
\rightarrow l.\text{tail} = \text{List}(a_{i+1}, \ldots, a_k) \land nmax = f(\text{List}(a_1, \ldots, a_i))
```

**3.** $\text{Inv}(s_f) \rightarrow \text{respuesta}(s_f) == f(a)$

```math
\text{Inv}((max, \text{List}())) \rightarrow max = f(\text{List}(a_1, \ldots, a_k))
```

**4.** En cada paso, la lista $l$ se reduce, acercándose a ser vacía. Después de
$k$ iteraciones, $l = \text{List}()$.

Esto implica que $P_f(L) == \text{maxAux}(L.\text{head}, L.\text{tail}) == f(L)$



// PUNTO 1


// PUNTO 2


// PUNTO 3


// PUNTO 4


// PUNTO 5
# Informe de corrección — Punto 5: medición de la aceleración

**Asignatura:** Fundamentos de Programación Funcional y Concurrente  
**Proyecto:** Simulación de partículas  
**Punto:** 5 — Medición de la aceleración

## 1. Especificación

El punto 5 requiere implementar las funciones:

```scala
def aceleracion(tSecuencial: Double, tParalelo: Double): Double
def umbralConveniente(s: Sistema, dt: Double, umbrales: List[Int]): Int
```

Además, `App.scala` debe comparar `pasoSecuencial` y `pasoParalelo` sobre sistemas grandes, con varios umbrales y utilizando ScalaMeter. El caso con umbral igual al tamaño del sistema funciona como control: al no dividir el arreglo, su aceleración debe ser aproximadamente 1.

## 2. Corrección de `aceleracion`

La implementación es:

```scala
def aceleracion(tSecuencial: Double, tParalelo: Double): Double =
  tSecuencial / tParalelo
```

Sean \(T_s>0\) y \(T_p>0\) los tiempos secuencial y paralelo. La función devuelve \(A=T_s/T_p\), que es exactamente la razón definida en el enunciado.

- Si \(T_s=T_p\), entonces \(A=1\).
- Si \(T_s>T_p\), entonces \(A>1\), por lo que el tiempo paralelo fue menor.
- Si \(T_s<T_p\), entonces \(A<1\), por lo que el tiempo paralelo fue mayor.

La función realiza únicamente la división de los dos valores recibidos; no mide tiempos ni depende del tamaño del sistema.

## 3. Corrección de `umbralConveniente`

La función trata primero el caso de lista vacía:

```scala
if (umbrales.isEmpty) s.length
```

Esto satisface la especificación: si no hay candidatos, devuelve el tamaño del sistema, que equivale a no dividirlo.

Si la lista no está vacía, construye pares `(tiempo, umbral)` midiendo `pasoParalelo` para cada candidato. Luego aplica `minBy(_._1)` y devuelve el segundo componente del par mínimo. Por definición de `minBy`, el resultado corresponde a un candidato cuyo tiempo medido es mínimo.

La implementación actual utiliza cinco repeticiones para cada candidato:

```scala
val tiempos = umbrales.map(u => (medir(5)(pasoParalelo(s, dt, u)), u))
tiempos.minBy(_._1)._2
```

Si dos candidatos empatan en tiempo, puede devolver cualquiera de los candidatos mínimos. La función no garantiza que un umbral sea siempre el más rápido en cualquier ejecución: selecciona el menor tiempo observado en esa medición.

## 4. Corrección del control y equivalencia del trabajo

`pasoSecuencial` procesa el arreglo completo llamando a `pasoSegmento(s, dt, 0, s.length)`. `pasoParalelo`, cuando el umbral es igual o mayor que `s.length`, entra en el caso base de `repartir` y también llama a `pasoSegmento(s, dt, 0, s.length)`.

Por tanto, ambas versiones aplican la misma transformación a las mismas partículas, con los mismos límites y el mismo `dt`. La versión paralela evalúa además la condición que decide si debe dividir; no crea tareas ni concatena resultados en el caso de control.

En términos funcionales, el resultado del control debe ser el mismo arreglo de partículas que produce `pasoSecuencial`. En términos temporales, sus mediciones deberían ser próximas y la aceleración debería quedar cerca de 1, aunque pequeñas diferencias de medición pueden aparecer por el entorno de ejecución.

## 5. Pruebas de comportamiento

Las pruebas adecuadas para estas funciones verifican propiedades deterministas:

- `aceleracion(100.0, 25.0) == 4.0`.
- `aceleracion(100.0, 100.0) == 1.0`.
- Una aceleración menor que 1 aparece cuando el tiempo paralelo es mayor que el secuencial.
- `umbralConveniente` devuelve `s.length` cuando la lista está vacía.
- Con un único candidato, devuelve ese candidato.
- Con varios candidatos, devuelve uno de los que obtuvieron el menor tiempo medido.
- El caso de control de `pasoParalelo` produce el mismo resultado funcional que `pasoSecuencial`.

Las pruebas automatizadas no deben comparar duraciones ni exigir una aceleración mínima, porque los tiempos dependen del equipo, la JVM y la carga del sistema.

## 6. Observación experimental

En la última ejecución, las aceleraciones del control fueron 0,52 para un millón de partículas, 0,98 para dos millones y 2,68 para tres millones. La teoría y la estructura del programa indican que estos valores deberían estar cerca de 1. La diferencia no demuestra un error en la fórmula `aceleracion`; evidencia que las muestras de tiempo no son suficientemente consistentes para sostener una conclusión de rendimiento.

La función `umbralConveniente` seleccionó 500.000, 1.000.000 y 3.000.000 para los tres tamaños, respectivamente. Sus resultados coincidieron con el mejor de cada tabla en esta ejecución. Esa coincidencia es una observación de esta corrida, no una garantía para futuras ejecuciones.

## 7. Conclusión

Las funciones `aceleracion` y `umbralConveniente` cumplen la especificación: la primera calcula el cociente entre tiempos y la segunda selecciona el candidato con menor tiempo medido, incluyendo correctamente el caso de lista vacía. El control permite comprobar que el caso sin particiones es comparable con la versión secuencial. Las anomalías observadas en algunas mediciones deben tratarse como un problema de estabilidad experimental y no como una prueba de que el algoritmo sin particiones acelera el cálculo.
