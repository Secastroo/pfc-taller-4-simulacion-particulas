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
    val tamanios =
      if (args.isEmpty) List(1000000, 2000000, 3000000)
      else args.toList.map(_.toInt)

    def crearSistema(n: Int): Sistema =
      Array.tabulate(n)(i =>
        Particula(
          Vector2(i.toDouble, 2.0 * i),
          Vector2(1.0 + i % 7, 0.5 * (i % 11)),
          1.0 + i % 5
        )
      )

    def mediana(muestras: Seq[Double]): Double =
      muestras.sorted.apply(muestras.length / 2)

    /**
     * Mide secuencial y control sin particiones en pares.
     * El orden se alterna entre pares para reducir el efecto del orden
     * de ejecución sobre la JVM y la carga del equipo.
     */
    def medirControl(s: Sistema, dt: Double): (Double, Double) = {
      val pares = Vector.tabulate(repeticiones) { i =>
        if (i % 2 == 0) {
          val ts = medir(1)(pasoSecuencial(s, dt))
          val tp = medir(1)(pasoParalelo(s, dt, s.length))
          (ts, tp)
        } else {
          val tp = medir(1)(pasoParalelo(s, dt, s.length))
          val ts = medir(1)(pasoSecuencial(s, dt))
          (ts, tp)
        }
      }

      (
        mediana(pares.map(_._1)),
        mediana(pares.map(_._2))
      )
    }

    // Cuántas veces se parte a la mitad antes de llegar al umbral.
    def particiones(n: Int, umbral: Int): Int =
      if (n <= math.max(umbral, 1)) 0
      else 1 + particiones((n + 1) / 2, umbral)

    // Fracción serial estimada mediante la ley de Amdahl.
    def fraccionSerial(aceleracionMedida: Double, p: Int): Double =
      (1.0 / aceleracionMedida - 1.0 / p) / (1.0 - 1.0 / p)

    println(s"Procesadores lógicos disponibles: $nucleos")
    println(s"Cada tiempo es la mediana de $repeticiones mediciones de ScalaMeter, en milisegundos.")
    println("El control se mide alternando el orden secuencial/paralelo.")

    tamanios.foreach { n =>
      val sistema = crearSistema(n)

      // Control emparejado: el paralelo usa umbral n y no crea particiones.
      val (tSecuencial, tControl) = medirControl(sistema, dt)

      val umbrales =
        List(n, n / 2, n / 8, 100000, 10000, 1000).distinct

      println()
      println(f"n = $n, secuencial = $tSecuencial%.1f ms")
      println()
      println("| Umbral | Particiones | Paralela (ms) | Aceleración | Aceleración estimada |")
      println("|---:|---:|---:|---:|---:|")

      val aceleraciones = umbrales.map { umbral =>
        // Reutiliza el control emparejado para no volver a medir esa fila
        // en otro momento y hacer la comparación menos consistente.
        val tParalelo =
          if (umbral == n) tControl
          else medir(repeticiones)(pasoParalelo(sistema, dt, umbral))

        val medida = aceleracion(tSecuencial, tParalelo)
        val estimada =
          trabajo(n).toDouble / tiempoEstimado(n, nucleos, umbral)

        println(
          f"| $umbral | ${particiones(n, umbral)} | " +
            f"$tParalelo%.1f | $medida%.2f | $estimada%.2f |"
        )
        (umbral, medida)
      }

      val (mejorUmbral, mejorAceleracion) =
        aceleraciones.maxBy(_._2)

      // Esta función vuelve a medir los candidatos de forma independiente.
      val conveniente = umbralConveniente(sistema, dt, umbrales)

      println()
      println(
        f"Mejor umbral de la tabla: $mejorUmbral, " +
          f"con aceleración $mejorAceleracion%.2f."
      )
      println(s"umbralConveniente escoge (medición independiente): $conveniente.")

      if (nucleos > 1) {
        val serial = fraccionSerial(mejorAceleracion, nucleos)
        println(
          f"Fracción que no se reparte (Amdahl, $nucleos procesadores lógicos): $serial%.3f."
        )
      }
    }
  }
}
