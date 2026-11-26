package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import scala.util.Random

/**
 * Las pruebas no miden tiempos: un tiempo depende de la máquina y de lo que
 * esté corriendo al lado, así que una prueba que lo compare fallaría sin que
 * nada esté mal.
 */
@RunWith(classOf[JUnitRunner])
class SimulacionParticulasTest extends AnyFunSuite {

  val sim = new SimulacionParticulas()

  import sim._

  def sistemaPA(n: Int): Sistema = Array.tabulate(n)(i =>
    Particula(Vector2(i, 2.0 * i), Vector2(i + 1.0, 0.5 * i), 1.0 + i))

  val eps = 1e-9

  val p = Particula(Vector2(0, 0), Vector2(1, 2), 1.0)

  val s: Sistema = Array(
    Particula(Vector2(0, 0), Vector2(3, 4), 2.0), // norma 5
    Particula(Vector2(4, 0), Vector2(0, 0), 2.0))

  val vacio: Sistema = Array()

  val azar = new Random(7)
  val grande: Sistema = Array.fill(1000)(Particula(
    Vector2(azar.nextDouble() * 10, azar.nextDouble() * 10),
    Vector2(azar.nextDouble() * 2, azar.nextDouble() * 2),
    azar.nextDouble() * 5 + 0.1))

  def cerca(a: Double, b: Double): Boolean = Math.abs(a - b) < 1e-6

  def cerca(a: Vector2, b: Vector2): Boolean =
    Math.abs(a.x - b.x) < 1e-6 && Math.abs(a.y - b.y) < 1e-6

  // Punto 1 -------------------------------------------------------------------

  test("pasoSecuencial: la posición avanza velocidad por dt") {
    assert(pasoSecuencial(Array(p), 2.0).head.pos == Vector2(2.0, 4.0))
  }

  test("pasoSecuencial: con dt cero nada se mueve") {
    assert(pasoSecuencial(Array(p), 0.0).head.pos == Vector2(0.0, 0.0))
  }

  test("pasoSecuencial: la velocidad y la masa no cambian") {
    val despues = pasoSecuencial(Array(p), 3.0).head
    assert(despues.vel == p.vel && despues.masa == p.masa)
  }

  test("pasoSecuencial: sobre el sistema vacío no hay nada que mover") {
    assert(pasoSecuencial(vacio, 1.0).isEmpty)
  }

  test("pasoSegmento: solo calcula el tramo pedido") {
    val tramo = pasoSegmento(s, 1.0, 1, 2)
    assert(tramo.length == 1 && tramo.head.pos == Vector2(4.0, 0.0))
  }

  test("pasoSegmento: el tramo vacío da un sistema vacío") {
    assert(pasoSegmento(s, 1.0, 1, 1).isEmpty)
  }

  test("pasoParalelo: da lo mismo que el secuencial en un caso simple") {
    assert(pasoParalelo(Array(p), 2.0, 1).head.pos == Vector2(2.0, 4.0))
  }

  test("pasoParalelo: coincide con el secuencial para varios umbrales") {
    val umbrales = List(1, 2, 7, 64, 1000, 5000)
    assert(umbrales.forall { u =>
      pasoParalelo(grande, 0.5, u).zip(pasoSecuencial(grande, 0.5))
        .forall { case (a, b) => cerca(a.pos, b.pos) }
    })
  }

  test("pasoParalelo: conserva el orden y el tamaño del sistema") {
    assert(pasoParalelo(grande, 0.5, 32).length == grande.length)
  }

  test("pasoParalelo: con umbral cero no se queda sin fondo") {
    assert(pasoParalelo(Array(p), 1.0, 0).length == 1)
  }

  // Punto 2 -------------------------------------------------------------------

  test("energiaSecuencial: media masa por velocidad al cuadrado") {
    assert(cerca(energiaSecuencial(s), 25.0))
  }

  test("energiaSecuencial: el sistema vacío no tiene energía") {
    assert(energiaSecuencial(vacio) == 0.0)
  }

  test("energiaSecuencial: una partícula quieta no aporta energía") {
    assert(cerca(energiaSecuencial(Array(s(1))), 0.0))
  }

  test("masaTotal: suma las masas") {
    assert(cerca(masaTotal(s), 4.0))
  }

  test("masaTotal: el sistema vacío no tiene masa") {
    assert(masaTotal(vacio) == 0.0)
  }

  test("centroDeMasaSecuencial: el punto medio de dos masas iguales") {
    assert(cerca(centroDeMasaSecuencial(s), Vector2(2.0, 0.0)))
  }

  test("centroDeMasaSecuencial: sin masa el centro es el origen") {
    assert(centroDeMasaSecuencial(vacio) == Vector2(0, 0))
  }

  test("centroDeMasaSecuencial: la masa mayor arrastra el centro") {
    val pesado: Sistema = Array(
      Particula(Vector2(0, 0), Vector2(0, 0), 3.0),
      Particula(Vector2(4, 0), Vector2(0, 0), 1.0))
    assert(cerca(centroDeMasaSecuencial(pesado), Vector2(1.0, 0.0)))
  }

  // Punto 3 -------------------------------------------------------------------

  test("energiaParalela: coincide con la secuencial en el ejemplo") {
    assert(cerca(energiaParalela(s, 1), 25.0))
  }

  test("energiaParalela: coincide con la secuencial para varios umbrales") {
    val esperado = energiaSecuencial(grande)
    assert(List(1, 3, 64, 999, 5000).forall(u => cerca(energiaParalela(grande, u), esperado)))
  }

  test("energiaParalela: sobre el sistema vacío da cero") {
    assert(energiaParalela(vacio, 1) == 0.0)
  }

  test("centroDeMasaParalelo: coincide con el secuencial en el ejemplo") {
    assert(cerca(centroDeMasaParalelo(s, 1), Vector2(2.0, 0.0)))
  }

  test("centroDeMasaParalelo: coincide con el secuencial para varios umbrales") {
    val esperado = centroDeMasaSecuencial(grande)
    assert(List(1, 3, 64, 999, 5000).forall(u => cerca(centroDeMasaParalelo(grande, u), esperado)))
  }

  test("centroDeMasaParalelo: promediar promedios daría otra cosa") {
    // Tres partículas: dos en el origen y una lejos. El promedio de los
    // promedios de las dos mitades no es el centro de masa del conjunto.
    val tres: Sistema = Array(
      Particula(Vector2(0, 0), Vector2(0, 0), 1.0),
      Particula(Vector2(0, 0), Vector2(0, 0), 1.0),
      Particula(Vector2(9, 0), Vector2(0, 0), 1.0))
    assert(cerca(centroDeMasaParalelo(tres, 1), Vector2(3.0, 0.0)))
  }

  test("centroDeMasaParalelo: sin masa el centro es el origen") {
    assert(centroDeMasaParalelo(vacio, 1) == Vector2(0, 0))
  }

  // Punto 4 -------------------------------------------------------------------

  test("trabajo: una operación por partícula") {
    assert(trabajo(1024) == 1024)
  }

  test("profundidad: si el umbral cubre todo, no se parte") {
    assert(profundidad(1024, 1024) == 1024)
  }

  test("profundidad: una partición y el tramo que queda") {
    assert(profundidad(1024, 512) == 513)
  }

  test("profundidad: diez particiones y un elemento") {
    assert(profundidad(1024, 1) == 11)
  }

  test("tiempoEstimado: profundidad más trabajo entre procesadores") {
    assert(cerca(tiempoEstimado(1024, 4, 1), 267.0))
  }

  test("tiempoEstimado: con un procesador no hay ganancia") {
    assert(cerca(tiempoEstimado(1024, 1, 1), 11.0 + 1024.0))
  }

  test("profundidad: nunca supera el trabajo") {
    val casos = for (n <- List(1, 7, 64, 1024); u <- List(1, 8, 64)) yield (n, u)
    assert(casos.forall { case (n, u) => profundidad(n, u) <= trabajo(n) })
  }

  // Punto 5 -------------------------------------------------------------------

  test("aceleracion: cuatro veces más rápido da 4.0") {
    assert(cerca(aceleracion(100.0, 25.0), 4.0))
  }

  test("aceleracion: el mismo tiempo da 1.0") {
    assert(cerca(aceleracion(100.0, 100.0), 1.0))
  }

  test("umbralConveniente: sin candidatos devuelve el tamaño del sistema") {
    assert(umbralConveniente(grande, 0.5, Nil) == grande.length)
  }

  test("umbralConveniente: devuelve uno de los candidatos") {
    val candidatos = List(16, 128, 1024)
    assert(candidatos.contains(umbralConveniente(grande, 0.5, candidatos)))
  }

  // PRUEBAS ESTUDIANTES
  // Punto 1  ------------------------------------------------------------------

  test("propia pasoSegmento: avanza solo desde incluido hasta excluido") {
    val sistema = sistemaPA(6)
    val tramo = pasoSegmento(sistema, 2.0, 2, 5)
    assert(tramo.map(q => q.pos).toList ==
      List(Vector2(8.0, 6.0), Vector2(11.0, 9.0), Vector2(14.0, 12.0)))
    assert(tramo.map(q => q.masa).toList == List(3.0, 4.0, 5.0))
    assert(tramo.map(q => q.vel).toList == sistema.slice(2, 5).map(q => q.vel).toList)
  }

  test("propia pasoParalelo: da lo mismo que pasoSecuencial con umbrales distintos") {
    val sistema = sistemaPA(100)
    val esperado = pasoSecuencial(sistema, 0.25).toList
    val umbrales = List(1, 2, 3, 10, 33, 99, 100, 101, 1000)
    assert(umbrales.forall(u => pasoParalelo(sistema, 0.25, u).toList == esperado))
  }

  test("propia pasoParalelo: el sistema vacío da un sistema vacío con cualquier umbral") {
    assert(List(0, 1, 5, 100).forall(u => pasoParalelo(vacio, 1.5, u).isEmpty))
    assert(pasoSegmento(vacio, 1.5, 0, 0).isEmpty)
  }

  test("propia pasoParalelo: con dt cero el sistema queda igual con cualquier umbral") {
    val sistema = sistemaPA(37)
    assert(List(1, 4, 8, 50).forall(u =>
      pasoParalelo(sistema, 0.0, u).toList == sistema.toList))
  }

  test("propia pasoParalelo: con tamaños impares no pierde ni repite ninguna partícula") {
    val tamanios = List(1, 3, 5, 7, 9, 15, 31, 101)
    assert(tamanios.forall { n =>
      val sistema = sistemaPA(n)
      val resultado = pasoParalelo(sistema, 2.0, 2)
      // La masa de cada partícula es 1 más su posición: sirve para reconocerla.
      resultado.map(q => q.masa).toList == (1 to n).map(i => i.toDouble).toList &&
        resultado.toList == pasoSecuencial(sistema, 2.0).toList
    })
  }
  // Punto 2  ------------------------------------------------------------------
  test("propia energiaSecuencial: suma la energía de partículas con masas distintas") {
    val dos: Sistema = Array(
      Particula(Vector2(0, 0), Vector2(3, 4), 1.0), // 0.5 * 1 * 25 = 12.5
      Particula(Vector2(1, 1), Vector2(0, -1), 4.0)) // 0.5 * 4 * 1  = 2.0
    assert(cerca(energiaSecuencial(dos), 14.5))
  }

  test("propia energiaSecuencial: el sentido de la velocidad no cambia la energía") {
    val adelante: Sistema = Array(Particula(Vector2(0, 0), Vector2(3, 4), 2.0))
    val atras: Sistema = Array(Particula(Vector2(0, 0), Vector2(-3, -4), 2.0))
    assert(cerca(energiaSecuencial(adelante), 25.0))
    assert(cerca(energiaSecuencial(atras), 25.0))
  }

  test("propia masaTotal: suma tres masas con decimales") {
    val tresMasas: Sistema = Array(
      Particula(Vector2(0, 0), Vector2(0, 0), 1.5),
      Particula(Vector2(1, 0), Vector2(0, 0), 2.5),
      Particula(Vector2(2, 0), Vector2(0, 0), 6.0))
    assert(cerca(masaTotal(tresMasas), 10.0))
  }

  test("propia centroDeMasaSecuencial: tres masas distintas en dos dimensiones") {
    val tresMasas: Sistema = Array(
      Particula(Vector2(0, 0), Vector2(0, 0), 1.0),
      Particula(Vector2(6, 0), Vector2(0, 0), 2.0),
      Particula(Vector2(0, 3), Vector2(0, 0), 3.0))
    // x = (0*1 + 6*2 + 0*3) / 6 = 2.0    y = (0*1 + 0*2 + 3*3) / 6 = 1.5
    assert(cerca(centroDeMasaSecuencial(tresMasas), Vector2(2.0, 1.5)))
  }

  test("propia centroDeMasaSecuencial: con partículas de masa cero el centro es el origen") {
    val sinMasa: Sistema = Array(
      Particula(Vector2(5, 5), Vector2(1, 1), 0.0),
      Particula(Vector2(-2, 7), Vector2(0, 0), 0.0))
    assert(centroDeMasaSecuencial(sinMasa) == Vector2(0, 0))
  }
 // Punto 3  ------------------------------------------------------------------
  test("energiaParalela: coincide con la secuencial en el ejemplo") {
    assert(cerca(energiaParalela(s, 1), 25.0))
  }

  test("energiaParalela: coincide con la secuencial para varios umbrales") {
    val esperado = energiaSecuencial(grande)
    assert(List(1, 3, 64, 999, 5000).forall(u => cerca(energiaParalela(grande, u), esperado)))
  }

  test("energiaParalela: sobre el sistema vacío da cero") {
    assert(energiaParalela(vacio, 1) == 0.0)
  }

  test("centroDeMasaParalelo: coincide con el secuencial en el ejemplo") {
    assert(cerca(centroDeMasaParalelo(s, 1), Vector2(2.0, 0.0)))
  }

  test("centroDeMasaParalelo: coincide con el secuencial para varios umbrales") {
    val esperado = centroDeMasaSecuencial(grande)
    assert(List(1, 3, 64, 999, 5000).forall(u => cerca(centroDeMasaParalelo(grande, u), esperado)))
  }

  test("centroDeMasaParalelo: promediar promedios daría otra cosa") {
    // Tres partículas: dos en el origen y una lejos. El promedio de los
    // promedios de las dos mitades no es el centro de masa del conjunto.
    val tres: Sistema = Array(
      Particula(Vector2(0, 0), Vector2(0, 0), 1.0),
      Particula(Vector2(0, 0), Vector2(0, 0), 1.0),
      Particula(Vector2(9, 0), Vector2(0, 0), 1.0))
    assert(cerca(centroDeMasaParalelo(tres, 1), Vector2(3.0, 0.0)))
  }

  test("centroDeMasaParalelo: sin masa el centro es el origen") {
    assert(centroDeMasaParalelo(vacio, 1) == Vector2(0, 0))
  }
  // Punto 4  ------------------------------------------------------------------
  test("propia profundidad: mil partículas con umbral 250") {
    // 1000 -> 500 -> 250: dos particiones y un tramo de 250
    assert(profundidad(1000, 250) == 252)
  }

  test("propia profundidad: con tamaño impar sigue la mitad mayor") {
    // 100 -> 50 -> 25 -> 13 -> 7 -> 4 -> 2: seis particiones y un tramo de 2
    assert(profundidad(100, 3) == 8)
  }

  test("propia profundidad: si el umbral cubre todo no se parte, y sin partículas no hay trabajo") {
    assert(profundidad(10, 50) == 10)
    assert(profundidad(0, 5) == 0)
  }

  test("propia tiempoEstimado: profundidad más trabajo entre procesadores") {
    assert(cerca(tiempoEstimado(1000, 8, 250), 377.0)) // 252 + 1000/8
    assert(cerca(tiempoEstimado(100, 4, 3), 33.0)) // 8 + 100/4
  }

  test("propia tiempoEstimado: agregar procesadores nunca baja de la profundidad") {
    val piso = profundidad(1000, 250).toDouble
    assert(List(1, 2, 16, 1000, 1000000).forall(procesadores =>
      tiempoEstimado(1000, procesadores, 250) >= piso))
  }
// Punto 5  ------------------------------------------------------------------
  test("propia aceleracion: si el reparto cuesta más de lo que ahorra, da menos de uno") {
    assert(cerca(aceleracion(50.0, 200.0), 0.25))
    assert(aceleracion(50.0, 200.0) < 1.0)
  }

  test("propia aceleracion: con tiempos decimales, intercambiarlos da el inverso") {
    assert(cerca(aceleracion(7.5, 2.5), 3.0))
    assert(cerca(aceleracion(2.5, 7.5), 1.0 / 3.0))
    assert(cerca(aceleracion(7.5, 2.5) * aceleracion(2.5, 7.5), 1.0))
  }

  test("propia umbralConveniente: sin candidatos devuelve el tamaño de cada sistema") {
    assert(List(0, 1, 37, 500).forall(n => umbralConveniente(sistemaPA(n), 0.5, Nil) == n))
  }

  test("propia umbralConveniente: con un solo candidato devuelve ese candidato") {
    val sistema = sistemaPA(200)
    assert(List(1, 50, 200, 5000).forall(u => umbralConveniente(sistema, 0.5, List(u)) == u))
  }

  test("propia umbralConveniente: el umbral escogido es un candidato y reproduce el paso secuencial") {
    val sistema = sistemaPA(2000)
    val candidatos = List(2000, 500, 100, 10)
    val escogido = umbralConveniente(sistema, 0.5, candidatos)
    assert(candidatos.contains(escogido))
    assert(pasoParalelo(sistema, 0.5, escogido).toList == pasoSecuencial(sistema, 0.5).toList)
  }
}


