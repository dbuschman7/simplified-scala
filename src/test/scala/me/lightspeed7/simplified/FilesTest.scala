package me.lightspeed7.simplified

import java.nio.file.Paths

import org.scalatest.FunSuite

class FilesTest extends FunSuite {


  test("Read shakespeare text, then write a test file") {

    import DataHelper._

    val lines = getFileData("shakespeare.txt")
      .zipWithIndex
      .map {
        case (l, i) => Line(l, i)
      }

    // write the file out in delimited form
    for (stream <- AutoCloseable(new StreamSaver[Line](Paths.get(Files.cwd.toString, "target/shakespeare.delimited")))) {
      var count = 0L
      Time.it(s"Wrote $count lines to file") {
        lines.foreach(stream.push)
        count = stream.count
      }
    }


  }

}
