package me.lightspeed7.simplified

import java.io.{ BufferedWriter, FileWriter }
import java.nio.file.Paths

import org.scalatest.{ FunSuite, Matchers }

class FilesTest extends FunSuite with Matchers {

  test("Read a file in memory") {

    import DataHelper._

    case class Line(line: String, number: Int) {
      def toDelimitedString: String = s"$number|$line"
    }

    val lines = getFileData("shakespeare.txt")
      .zipWithIndex
      .map {
        case (l, i) => Line(l, i)
      }

    lines.foreach { l =>
      println(s"${l.number}|${l.line}")
    }

    // write a file out manually
    val outputFilePath = Paths.get(Files.cwd.toString, "target/shakespeare.delimited")
    Files.mkdirs(outputFilePath)

    val buf = new BufferedWriter(new FileWriter(outputFilePath.toFile))
    try {
      lines.foreach { line =>
        buf.write(line.toDelimitedString)
        buf.write(System.lineSeparator())
      }
    } finally {
      buf.close()
    }
  }
}
