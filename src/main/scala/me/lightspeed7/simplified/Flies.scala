package me.lightspeed7.simplified

import java.io.{BufferedWriter, File, FileWriter, Writer}
import java.nio.file.{Path, Paths}
import java.util.concurrent.atomic.AtomicLong

object Files {
  val cwd: Path = Paths.get("").toAbsolutePath

  def mkdirs(in: Path): Boolean = in.getParent.toFile.mkdirs

  def findAllFiles(selector: File => Boolean)(location: File): Seq[File] = {

    @annotation.tailrec
    def go(toCheck: List[File], results: List[File]): Seq[File] = toCheck match {
      case head :: tail =>
        val filesList: Seq[File] = head.listFiles.toSeq
        val updated: List[File] = results ++ filesList.filterNot(_.isDirectory).filter(selector(_))
        go(tail ++ filesList.filter(_.isDirectory), updated)
      case _ => results
    }

    go(location :: Nil, Nil)
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

  override def close(): Unit = underlying.close()
}

trait DelimitedSerializable {
  def toDelimitedString: String
}

class FileSaver[T <: DelimitedSerializable](path: Path, delimter: String = System.lineSeparator()) {

  def persist(in: Seq[T]): Unit = {
    for (writer <- AutoCloseable(new DelimitedFile(path))) {
      in.map(_.toDelimitedString).foreach(writer.write)
    }
  }
}

class StreamSaver[T <: DelimitedSerializable](path: Path, delimter: String = System.lineSeparator()) extends java.lang.AutoCloseable {
  private val writer = new DelimitedFile(path)
  private val counter = new AtomicLong(0L)

  def push(in: T): Unit = {
    writer.write(in.toDelimitedString)
    counter.incrementAndGet
    ()
  }

  def count: Long = counter.get

  override def close(): Unit = writer.close()
}
