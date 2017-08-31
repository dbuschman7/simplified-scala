package me.lightspeed7.simplified

import java.io.{ BufferedWriter, File, FileWriter, Writer }
import java.nio.file.{ Path, Paths }

object Files {
  val cwd = Paths.get("").toAbsolutePath

  def mkdirs(in: Path): Boolean = in.getParent.toFile.mkdirs

  def findAllFiles(selector: File => Boolean)(baseDir: File): Seq[File] = {
    if (baseDir.isDirectory) {
      val local = baseDir.listFiles
      val currentDirFiles = local.filter(!_.isDirectory).filter(selector(_))
      val recursedFiles = local.filter(_.isDirectory).flatMap(findAllFiles(selector))
      currentDirFiles ++ recursedFiles
    } else {
      Seq(baseDir)
    }
  }
}

class DelimitedFile(path: Path, mkDirs: Boolean = false, delimiter: String = System.lineSeparator()) extends java.lang.AutoCloseable {

  private lazy val underlying: Writer = {
    if (mkDirs) {
      Files.mkdirs(path)
    }
    new BufferedWriter(new FileWriter(path.toFile))
  }

  def write(in: String): Unit = {
    underlying.write(in)
    underlying.write(System.lineSeparator())
  }

  def write(records: Seq[String]): Unit = Option(records).foreach { recs =>
    recs.foreach { in =>
      underlying.write(in)
      underlying.write(System.lineSeparator())
    }
  }

  override def close() = underlying.close()
}

trait DelimitedSerializable {
  def toDelimitedString: String
}

class FileSaver[T <: DelimitedSerializable](path: Path) {

  def persist(in: Seq[T]): Unit = {
    for (writer <- AutoCloseable(new DelimitedFile(path))) {
      in.map(_.toDelimitedString).foreach(writer.write)
    }
  }
}

class StreamSaver[T <: DelimitedSerializable](path: Path) extends java.lang.AutoCloseable {
  private val writer = new DelimitedFile(path)

  def push(in: T): Unit = writer.write(in.toDelimitedString)

  override def close() = writer.close()
}
