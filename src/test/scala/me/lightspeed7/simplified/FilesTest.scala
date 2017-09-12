package me.lightspeed7.simplified

import org.scalatest.{ FunSuite, Matchers }

class FilesTest extends FunSuite with Matchers {

  test("Read a file in memory") {

    import DataHelper._

    case class Line(line: String, number: Int)

    val lines = getFileData("shakespeare.txt")
      .zipWithIndex
      .map {
        case (l, i) => Line(l, i + 1)
      }

    lines.foreach { l =>
      println(s"${l.number}|${l.line}")
    }

  }
}
