package me.lightspeed7.simplified

import scala.io.Source

object DataHelper {

  def getFileData(fileName: String): Seq[String] = {

    println("CWD -> " + Files.cwd)
    println

    // find the file in our source tree
    val testFile = Files
      .findAllFiles(in => in.getName == fileName)(Files.cwd.toFile)
      .filterNot(_.getCanonicalPath.contains("target"))
      .headOption

    // print out the file and its size
    testFile.foreach { file =>
      val size = PrettyPrint.fileSizing(file.length())
      println(s"File($size) - ${file.getCanonicalPath}")
    }
    println

    // read the lines from the file
    testFile
      .map(f => Source.fromFile(f).getLines().map(_.trim).toSeq)
      .getOrElse(Seq.empty)
  }

}
