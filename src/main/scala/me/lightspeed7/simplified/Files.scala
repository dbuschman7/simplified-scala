package me.lightspeed7.simplified

import java.io.File
import java.nio.file.{ Path, Paths }

object Files {

  val cwd: Path = Paths.get("").toAbsolutePath

  def mkdirs(in: Path): Boolean = in.getParent.toFile.mkdirs

  def findAllFiles(selector: File => Boolean)(location: File): Seq[File] = {

    @annotation.tailrec
    def go(toCheck: List[File], previous: List[File]): Seq[File] = toCheck match {
      case head :: tail =>
        val filesList = head.listFiles
        val newFiles = filesList.filterNot(_.isDirectory).filter(selector(_))
        val newDirs = filesList.filter(_.isDirectory)
        go(tail ++ newDirs, previous ++ newFiles) // recurse
      case _ => previous
    }

    go(location :: Nil, Nil)
  }
}
