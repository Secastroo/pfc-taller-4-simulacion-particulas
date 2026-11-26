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

## Punto 5: medir la aceleración

Sea $n$ el tamaño del sistema $s$, y sea $T(u)$ el tiempo que mide `medir` para
`pasoParalelo(s, dt, u)`.

### Especificación

```scala
def aceleracion(tSecuencial: Double, tParalelo: Double): Double
def umbralConveniente(s: Sistema, dt: Double, umbrales: List[Int]): Int
```

`aceleracion` es el cociente entre los dos tiempos. `umbralConveniente` devuelve
el umbral de la lista que menos tarda y, con la lista vacía, el tamaño del
sistema. Además, `App.scala` usa como control el umbral igual a $n$.

### Corrección de `aceleracion`

```scala
def aceleracion(tSecuencial: Double, tParalelo: Double): Double =
  tSecuencial / tParalelo
```

Para $t_s, t_p$ con $t_p > 0$, la especificación es

```math
S(t_s, t_p) = \frac{t_s}{t_p}
```

El cuerpo de la función es esa misma expresión, así que
$\text{aceleracion}(t_s, t_p) = S(t_s, t_p)$ por sustitución. De la definición
se siguen los casos que usa el informe de paralelización:

```math
S(t, t) = 1
\qquad
S\!\left(t, \tfrac{t}{k}\right) = k
\qquad
S(a, b) \cdot S(b, a) = \frac{a}{b} \cdot \frac{b}{a} = 1
```

- Si $t_s = t_p$, entonces $S = 1$.
- Si $t_s > t_p$, entonces $S > 1$: el tiempo paralelo fue menor.
- Si $t_s < t_p$, entonces $S < 1$: el tiempo paralelo fue mayor.

La función solo divide los dos valores que recibe: no mide tiempos ni depende
del tamaño del sistema. Los tiempos de ScalaMeter son positivos, así que el
caso $t_p = 0$, que en punto flotante daría infinito, no aparece.

### Corrección de `umbralConveniente`

```scala
def umbralConveniente(s: Sistema, dt: Double, umbrales: List[Int]): Int =
  if (umbrales.isEmpty) s.length
  else {
    val tiempos = umbrales.map(u => (medir(5)(pasoParalelo(s, dt, u)), u))
    tiempos.minBy(_._1)._2
  }
```

Sea $U = [u_1, \ldots, u_k]$ la lista de candidatos. El resultado $u^*$ debe
cumplir:

- Si $U = \emptyset$, entonces $u^* = n$.
- Si $U \neq \emptyset$, entonces $u^* \in U$ y $\forall u \in U : T(u^*) \le T(u)$.

*Caso $U = \emptyset$.* La condición `umbrales.isEmpty` es verdadera y la
función devuelve `s.length`, que es $n$.

*Caso $U \neq \emptyset$.* La función construye la lista de parejas

```math
L = [(T(u_1), u_1), \ldots, (T(u_k), u_k)]
```

`map` conserva el número de elementos y cada pareja lleva su candidato como
segunda componente. `minBy(_._1)` devuelve una pareja $(t^*, u^*)$ de $L$ cuyo
primer componente es el menor. Se comprueba por inducción sobre la longitud
$j$ de la lista:

- **Caso base**, $j = 1$: la lista tiene una sola pareja, y esa es la menor.
- **Paso**: sea $m$ la pareja que `minBy` escoge entre las primeras $j$, y por
  hipótesis de inducción $t_m \le t_i$ para esas $j$. Al llegar la pareja
  $j + 1$ se compara con $m$. Si $t_{j+1} < t_m$ se queda con ella, y entonces
  $t_{j+1} < t_m \le t_i$ para todas las anteriores. Si no, se queda con $m$,
  que ya era menor o igual que todas las anteriores y que la nueva.

Por lo tanto $t^* \le T(u)$ para todo $u \in U$, y como $u^*$ es la segunda
componente de una pareja de $L$, pertenece a $U$. Si dos candidatos empatan,
`minBy` conserva el primero de la lista.

**Alcance.** $T$ es una medición y fluctúa de una corrida a otra. Lo que se
demuestra es que el resultado es el mejor de los tiempos medidos dentro de esa
llamada, una vez por candidato. No se afirma que vuelva a serlo en otra
ejecución. Por eso `umbralConveniente` y el mejor umbral de la tabla de
`App.scala` pueden diferir, porque cada uno hace su propia medición.

### La combinación de resultados parciales es asociativa

**Escoger el menor.** `minBy` pliega la lista con la operación $\sqcap$ que, de
dos parejas, deja la de menor tiempo y, si empatan, la de más a la izquierda.
Es el mínimo según el orden total $(t, \text{posición})$, y el mínimo de un
orden total es asociativo:

```math
(a \sqcap b) \sqcap c = a \sqcap (b \sqcap c)
```

Los tiempos son números finitos, así que el orden es total.

**Unir los segmentos.** `pasoParalelo`, la función que se mide, combina los
resultados de las dos mitades con `++`. Concatenar es asociativo: los dos
lados son los elementos de $A$, seguidos de los de $B$ y de los de $C$:

```math
(A \mathbin{+\!\!+} B) \mathbin{+\!\!+} C = A \mathbin{+\!\!+} (B \mathbin{+\!\!+} C)
```

Por eso da igual cómo se agrupen los tramos que terminan primero: el arreglo
final es el mismo.

### Partir el sistema no pierde ni repite partículas

La medición solo tiene sentido si `pasoParalelo` calcula lo mismo que
`pasoSecuencial`. Sea $\text{lim} = \max(u, 1)$. Se demuestra, por inducción
sobre la longitud $h - d$ del tramo, que para $0 \le d \le h \le n$:

```math
\text{repartir}(d, h) = [\,\text{mover}(s_d, dt), \ldots, \text{mover}(s_{h-1}, dt)\,]
```

- **Caso base**, $h - d \le \text{lim}$: `repartir` llama a
  `pasoSegmento(s, dt, d, h)`, que construye el arreglo con una posición por
  cada índice entre $d$ y $h - 1$, en ese orden.
- **Paso**, $h - d > \text{lim} \ge 1$: la longitud es al menos 2 y el punto
  medio $m = d + \lfloor (h - d)/2 \rfloor$ cumple $d < m < h$. Los dos
  tramos $[d, m)$ y $[m, h)$ son más cortos que $[d, h)$, así que se aplica la
  hipótesis de inducción a cada uno. Se cumple

  ```math
  [d, m) \cup [m, h) = [d, h)
  \qquad
  [d, m) \cap [m, h) = \emptyset
  ```

  y `++` los pega en ese orden, de modo que cada índice aparece una sola vez.
  Que $d < m < h$ también garantiza que la recursión termina, y
  `limite = max(umbral, 1)` evita que un tramo de un elemento se parta sin fin.

Como `pasoSecuencial(s, dt)` es `pasoSegmento(s, dt, 0, n)`, que es el caso
base con $d = 0$ y $h = n$, se concluye que para todo umbral

```math
\text{pasoParalelo}(s, dt, u) = \text{pasoSecuencial}(s, dt)
```

`mover` no modifica nada y las dos ramas de `parallel` no comparten estado que
cambie, así que el orden en que terminen no altera el resultado.

### El control y la equivalencia del trabajo

Si $u \ge n$, entonces $n \le \text{lim}$ y `repartir(0, n)` cae directamente
en el caso base: llama a `pasoSegmento(s, dt, 0, n)`, la misma expresión que
`pasoSecuencial`. Las dos versiones aplican la misma transformación a las
mismas partículas, con los mismos límites y el mismo `dt`; en el control no se
crean tareas ni se concatena nada. El resultado es el mismo arreglo y, en
tiempo, la aceleración debería quedar cerca de 1, con las diferencias propias
del ruido de medición.

### Las pruebas

Las pruebas verifican propiedades deterministas y no comparan tiempos, porque
estos dependen del equipo, de la JVM y de la carga del sistema:

- `aceleracion` da menos de 1 cuando el paralelo tarda más, y 3,0 con 7,5 y 2,5.
- Al intercambiar los tiempos el resultado es el inverso, y el producto es 1.
- `umbralConveniente` devuelve el tamaño del sistema con la lista vacía, para
  tamaños 0, 1, 37 y 500.
- Con un solo candidato devuelve ese candidato.
- El umbral escogido pertenece a la lista y, con él, `pasoParalelo` da el mismo
  arreglo que `pasoSecuencial`.

Los tiempos que se midieron, y cómo se interpretan, están en
`paralelizacion.md`.