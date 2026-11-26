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



// PARTICIPANTE 1


// PARTICIPANTE 2


// PARTICIPANTE 3
## Punto 3: corrección de energiaParalela y centroDeMasaParalelo

### Notación

Sea $s = (p_0, \ldots, p_{n-1})$ el sistema, con $p_i = (x_i, v_i, m_i)$ (posición, velocidad y masa). Para $0 \le a \le b \le n$ el **segmento** $[a, b)$ son los índices $a, a+1, \ldots, b-1$, y su longitud es $\ell = b - a$. Se definen las magnitudes de un segmento:

$$
E(a,b) = \sum_{i=a}^{b-1} \tfrac{1}{2}\, m_i \lVert v_i \rVert^2
\qquad
W(a,b) = \sum_{i=a}^{b-1} m_i\, x_i
\qquad
M(a,b) = \sum_{i=a}^{b-1} m_i
$$

Con la convención de que una suma vacía vale $0$. Las especificaciones del enunciado son $energiaSecuencial(s) = E(0,n)$ y $centroDeMasaSecuencial(s) = W(0,n)/M(0,n)$ si $M(0,n) \neq 0$, y el origen si $M(0,n) = 0$.

### 1. Partir el sistema en segmentos no pierde ni repite partículas

Sea $[a,b)$ un segmento que se parte, es decir, con $\ell > umbral$ y $\ell > 1$. El programa toma $medio = a + \lfloor \ell/2 \rfloor$ y llama a los segmentos $[a, medio)$ y $[medio, b)$.

- Como $\ell \ge 2$, se tiene $\lfloor \ell/2 \rfloor \ge 1$ y $\lfloor \ell/2 \rfloor < \ell$, luego $a < medio < b$.
- **No se pierde ninguna:** $[a, medio) \cup [medio, b) = [a, b)$, porque todo índice $i$ con $a \le i < b$ cumple $i < medio$ o $i \ge medio$.
- **No se repite ninguna:** $[a, medio) \cap [medio, b) = \emptyset$, porque ningún $i$ cumple a la vez $i < medio$ e $i \ge medio$.
- Las longitudes son $\lfloor \ell/2 \rfloor$ y $\lceil \ell/2 \rceil$, ambas estrictamente menores que $\ell$.
  Por lo tanto, cada segmento se descompone en una **partición** de sí mismo, y de la partición se sigue que cualquier suma sobre $[a,b)$ es la suma de las sumas sobre las dos partes:

$$
E(a,b) = E(a,medio) + E(medio,b)
\qquad
W(a,b) = W(a,medio) + W(medio,b)
\qquad
M(a,b) = M(a,medio) + M(medio,b)
$$

**Terminación.** Cada llamado recursivo trabaja sobre un segmento de longitud estrictamente menor, así que la longitud (un natural) decrece y se llega a $\ell \le 1$ o a $\ell \le umbral$, donde no se parte más. La condición $\ell > 1$ es la que garantiza esto cuando $umbral \le 0$: sin ella, un segmento de longitud $1$ se partiría en uno vacío y uno igual a sí mismo, y la recursión no terminaría.

### 2. Corrección de energiaParalela

**Lema (acumular).** Para $a \le i \le b$, acumular(i, acc) devuelve $acc + \sum_{k=i}^{b-1} \tfrac{1}{2} m_k \lVert v_k \rVert^2$.
*Demostración por inducción sobre $b - i$.* Si $b - i = 0$, entonces $i \ge b$, devuelve $acc$ y la suma vacía vale $0$. Si $b - i = r + 1$, el programa llama a acumular(i+1, acc + ½ mᵢ‖vᵢ‖²), que por hipótesis de inducción devuelve $acc + \tfrac{1}{2} m_i \lVert v_i \rVert^2 + \sum_{k=i+1}^{b-1}(\ldots) = acc + \sum_{k=i}^{b-1}(\ldots)$. $\square$

Luego acumular(a, 0.0) $= E(a,b)$.

**Teorema.** Para todo segmento $[a,b)$ y todo umbral, energiaTramo(a, b) $= E(a,b)$.
*Demostración por inducción fuerte sobre $\ell = b - a$.*

- **Caso sin partir** ($\ell \le umbral$ o $\ell \le 1$, incluye $\ell = 0$): el programa devuelve acumular(a, 0.0) $= E(a,b)$ por el lema.
- **Caso de partir** ($\ell > umbral$ y $\ell > 1$): parallel devuelve la pareja formada por energiaTramo(a, medio) y energiaTramo(medio, b); como ambos segmentos son más cortos que $\ell$, por hipótesis de inducción valen $E(a, medio)$ y $E(medio, b)$. El resultado es su suma, que por la sección 1 es $E(a,b)$. $\square$
  Como energiaParalela(s, umbral) es energiaTramo(0, n), se concluye:

$$
\forall s,\ \forall umbral:\quad energiaParalela(s, umbral) = E(0,n) = energiaSecuencial(s)
$$

### 3. Corrección de centroDeMasaParalelo

Se define la pareja parcial $P(a,b) = \big(W(a,b),\ M(a,b)\big)$ y la operación $\oplus$ sobre parejas:

$$
(W_1, M_1) \oplus (W_2, M_2) = (W_1 + W_2,\ M_1 + M_2)
$$

Igual que en la sección 2, por el lema de acumular y por inducción fuerte sobre $\ell$, parcial(a, b) $= P(a,b)$: en el caso de partir, por la sección 1, $P(a,b) = P(a,medio) \oplus P(medio,b)$, que es lo que combina el programa. Al final el programa divide **una sola vez**:

$$
centroDeMasaParalelo(s, umbral) =
\begin{cases}
\dfrac{W(0,n)}{M(0,n)} & \text{si } M(0,n) \neq 0 \\[2mm]
(0,0) & \text{si } M(0,n) = 0
\end{cases}
$$

que es exactamente la especificación de centroDeMasaSecuencial. Es decir, $\forall s,\ \forall umbral:\ centroDeMasaParalelo(s, umbral) = centroDeMasaSecuencial(s)$.

### 4. La operación que combina los resultados parciales es asociativa

- **Energía:** la operación es la suma de números reales, $(x + y) + z = x + (y + z)$.
- **Centro de masa:** $\oplus$ actúa componente a componente con sumas de reales. Para parejas $(W_i, M_i)$:
  $$
  \big((W_1,M_1) \oplus (W_2,M_2)\big) \oplus (W_3,M_3)
  = (W_1+W_2+W_3,\ M_1+M_2+M_3)
  = (W_1,M_1) \oplus \big((W_2,M_2) \oplus (W_3,M_3)\big)
  $$

Además $\oplus$ tiene elemento neutro $(0,0)$ (el resultado de un tramo vacío). Por eso da igual cómo se agrupen los segmentos: el árbol de llamados puede tener cualquier forma y el resultado es el mismo.

**Observación sobre los Double.** La asociatividad es exacta en $\mathbb{R}$. Con aritmética de punto flotante (IEEE 754) la suma no es exactamente asociativa, por lo que la versión paralela, que suma en otro orden que la secuencial, puede diferir en el último dígito. La diferencia es del orden del error de redondeo y por eso las pruebas comparan con una tolerancia pequeña y no con igualdad exacta.

### 5. Por qué la suma sirve para repartir y el promedio no

Sea $C(a,b) = W(a,b)/M(a,b)$ el centro de masa de un segmento, con $M_L = M(a,medio)$ y $M_R = M(medio,b)$. Entonces:

$$
C(a,b) = \frac{W_L + W_R}{M_L + M_R}
= \frac{M_L\, C_L + M_R\, C_R}{M_L + M_R}
$$

es un promedio **ponderado** por las masas de cada mitad. Solo coincide con el promedio simple $\tfrac{1}{2}(C_L + C_R)$ cuando $M_L = M_R$.

- **La suma sí sirve** porque las magnitudes $E$, $W$ y $M$ son sumas sobre los elementos, y una suma sobre una partición es la suma de las sumas de las partes (sección 1). Cada mitad se calcula sin saber nada de la otra y la combinación es exacta.
- **El promedio no sirve directamente** porque el promedio de un conjunto no se obtiene del promedio de sus partes sin saber cuánto pesa cada una. Además la operación $\mathrm{prom}(x, y) = \tfrac{x+y}{2}$ ni siquiera es asociativa: $\mathrm{prom}(\mathrm{prom}(0,0), 9) = 4.5$ pero $\mathrm{prom}(0, \mathrm{prom}(0, 9)) = 2.25$, así que el resultado dependería de cómo se parta el sistema.
  **Contraejemplo del sistema de tres partículas** (posiciones $(1,2)$, $(5,6)$, $(-3,0)$ y masas $1$, $3$, $4$): el centro de masa es $(0.5, 2.5)$, pero el promedio de los centros de las mitades $\{p_0\}$ y $\{p_1, p_2\}$ es $(\tfrac{5}{7}, \tfrac{16}{7}) \neq (0.5, 2.5)$. Llevando $(W, M)$ y dividiendo una sola vez al final, se obtiene $(4, 20)/8 = (0.5, 2.5)$, el valor correcto.


// PARTICIPANTE 4