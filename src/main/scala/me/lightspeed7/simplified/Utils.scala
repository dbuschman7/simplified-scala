package me.lightspeed7.simplified

object CoolStrings {

  implicit class StringHandler(in: String) {

    def notNull: String = if (in == null) "" else in

    def notBlank: Option[String] = in.notEmpty.flatMap(_.trim.notEmpty)

    def notEmpty: Option[String] = in.notNull match {
      case "" => None
      case _  => Option(in)
    }
  }

}

object Time {

  def it[T](label: => String, output: String => Unit = (in) => println(in))(block: => T): T = {
    // run it
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()

    // dump the results
    val millis = (t1 - t0) / (1000 * 1000)
    val rawSecs: Double = millis.toDouble / 1000
    val mins: Int = rawSecs.floor.toInt / 60
    val secs: Double = rawSecs - (mins * 60)
    if (mins > 0) {
      output(s"$label - Elapsed Time: $mins mins $secs seconds")
    } else {
      output(s"$label - Elapsed Time: $secs seconds")
    }
    result
  }
}

object PrettyPrint {

  private val units = Array[String]("B", "K", "M", "G", "T")
  private val format = new java.text.DecimalFormat("#,##0.#")

  def fileSizing(input: Long): String = {
    if (input <= 0) return "0.0"
    val digitGroups = (Math.log10(input.toDouble) / Math.log10(1024)).toInt
    format.format(input / Math.pow(1024.0, digitGroups.toDouble)) + " " + units(digitGroups)
  }

}
