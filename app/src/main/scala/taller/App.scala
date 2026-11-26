package taller

/**
 * El punto 5 pide medir aquí, sobre un sistema de al menos un millón de
 * partículas, el tiempo de `pasoSecuencial` y el de `pasoParalelo` con varios
 * umbrales, y reportar la aceleración de cada uno.
 *
 * Lo que hay ahora es el arranque mínimo sobre dos partículas.
 */
object App {

  def main(args: Array[String]): Unit = {
    val sim = new SimulacionParticulas()
    import sim._

    val nucleos = Runtime.getRuntime.availableProcessors
    val dt = 0.5
    val repeticiones = 5
    val tamanios = if (args.isEmpty) List(1000000, 2000000, 3000000) else args.toList.map(_.toInt)

    def crearSistema(n: Int): Sistema = Array.tabulate(n)(i =>
      Particula(Vector2(i.toDouble, 2.0 * i), Vector2(1.0 + i % 7, 0.5 * (i % 11)), 1.0 + i % 5))

    // Cuántas veces se parte a la mitad antes de llegar al umbral.
    def particiones(n: Int, umbral: Int): Int =
      if (n <= math.max(umbral, 1)) 0 else 1 + particiones((n + 1) / 2, umbral)

    // Fracción del cálculo que no se reparte, según la ley de Amdahl.
    def fraccionSerial(aceleracionMedida: Double, p: Int): Double =
      (1.0 / aceleracionMedida - 1.0 / p) / (1.0 - 1.0 / p)

    println(s"Núcleos disponibles: $nucleos")
    println(s"Cada tiempo es la mediana de $repeticiones mediciones de ScalaMeter, en milisegundos.")

    tamanios.foreach { n =>
      val sistema = crearSistema(n)
      val tSecuencial = medir(repeticiones)(pasoSecuencial(sistema, dt))
      // El primer umbral es el tamaño del sistema: es la fila de control, no
      // reparte nada y su aceleración tiene que dar 1.
      val umbrales = List(n, n / 2, n / 8, 100000, 10000, 1000).distinct

      println()
      println(f"n = $n, secuencial = $tSecuencial%.1f ms")
      println()
      println("| Umbral | Particiones | Paralela | Aceleración | Aceleración estimada |")
      println("|---:|---:|---:|---:|---:|")

      val aceleraciones = umbrales.map { umbral =>
        val tParalelo = medir(repeticiones)(pasoParalelo(sistema, dt, umbral))
        val medida = aceleracion(tSecuencial, tParalelo)
        val estimada = trabajo(n).toDouble / tiempoEstimado(n, nucleos, umbral)
        println(f"| $umbral | ${particiones(n, umbral)} | $tParalelo%.1f | $medida%.2f | $estimada%.2f |")
        (umbral, medida)
      }

      val (mejorUmbral, mejorAceleracion) = aceleraciones.maxBy(_._2)
      val conveniente = umbralConveniente(sistema, dt, umbrales)
      println()
      println(f"Mejor umbral de la tabla: $mejorUmbral, con aceleración $mejorAceleracion%.2f.")
      println(s"umbralConveniente escoge: $conveniente.")
      if (nucleos > 1) {
        val serial = fraccionSerial(mejorAceleracion, nucleos)
        println(f"Fracción que no se reparte (Amdahl, $nucleos núcleos): $serial%.3f.")
      }
    }
  }
}
