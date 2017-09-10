package me.lightspeed7.simplified

import java.nio.file.Paths

import me.lightspeed7.simplified.Files.{ DelimitedSerializable, FileSaver, StreamSaver }
import org.scalatest.{ FunSuite, Matchers }

class FilesTest extends FunSuite with Matchers {

  test("Read a file in memory") {

    import DataHelper._

    case class Line(line: String, number: Int) extends DelimitedSerializable {
      def toDelimitedString: String = s"$number|$line"
    }

    val lines = getFileData("shakespeare.txt")
      .zipWithIndex // zero based
      .map {
        case (l, i) => Line(l, i + 1)
      }

    lines.foreach { l =>
      println(s"${l.number}|${l.line}")
    }

    // write a file out all at once
    Time.it(s"Wrote ${lines.length} lines to file") {
      new FileSaver[Line](Paths.get(Files.cwd.toString, "target/shakespeare.delimited.at.once")).persist(lines)
    }

    // stream the file out in delimited form
    for (stream <- AutoCloseable(new StreamSaver[Line](Paths.get(Files.cwd.toString, "target/shakespeare.delimited.streamed")))) {
      var count = 0L
      Time.it(s"Streamed $count lines to file") {
        lines.foreach(stream.push)
        count = stream.count
      }
    }
  }
}
