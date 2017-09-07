package me.lightspeed7.simplified

import java.nio.file.Paths

import scala.io.Source

object DataHelper {

  case class Line(line: String, number: Int) extends DelimitedSerializable {
    override def toDelimitedString: String = number.toString + "|" + line
  }

  def getFileData(fileName: String): Seq[String] = {
    println(Files.cwd)

    // find the file in our source tree
    val testFile = Files
      .findAllFiles(in => in.getName == fileName)(Paths.get(".").toFile)
      .filterNot(_.getCanonicalPath.contains("target"))
      .headOption

    // print out the file and its size
    println
    testFile.foreach { file =>
      val size = PrettyPrint.fileSizing(file.length())
      println(s"File($size) - ${file.getCanonicalPath}")
    }
    println

    // read the lines from the file
    testFile
      .map(f => Source.fromFile(f).getLines().toSeq)
      .getOrElse(Seq.empty)
      .map(_.trim)
  }


}
