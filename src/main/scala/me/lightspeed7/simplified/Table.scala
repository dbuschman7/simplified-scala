package me.lightspeed7.simplified

object Table {

  object Partitioned {
    def createPartitionStatement(partition: String): String = s"PARTITION p$partition VALUES LESS THAN ($partition)"

    def makePartitionParts(column: String, partitionNames: Seq[String]): String = {
      partitionNames
        .map(createPartitionStatement)
        .mkString( //
          s"${System.lineSeparator()}/*!50100 PARTITION BY RANGE (`$column`) (${System.lineSeparator()}",
          s",${System.lineSeparator()}",
          ") */")
    }

    def createPartitionStatement(database: String, table: String, partition: String): String = {
      s"""ALTER TABLE `$database`.`$table` ADD PARTITION (${createPartitionStatement(partition)})"""
    }

  }

  def tableFooter: String = s"ENGINE=InnoDB DEFAULT CHARSET=utf8 ${System.lineSeparator()}"

  case class Column(name: String, dataType: String, nullable: Boolean = false, autoIncrement: Boolean = false, default: Option[String] = None) {
    def toLine: String = {
      val nullPart = if (nullable) "" else " NOT NULL "
      val autoPart = if (autoIncrement) " AUTO_INCREMENT " else ""
      val defPart = default.map(i => s"DEFAULT $i").getOrElse("")

      Seq(name, dataType, nullPart, autoPart, defPart).mkString(" ")
    }
  }

  case class Key(name: String, columns: Seq[String]) {
    def toLine: String = {
      val cols = columns.toSeq.map { c => s"`${c}`" }
      s"KEY `$name` (${cols.mkString(",")}) "
    }
  }

  def column(name: String, dataType: String, nullable: Boolean = false, autoIncrement: Boolean = false, default: Option[String] = None): Column = {
    Column(name, dataType, nullable, autoIncrement, default)
  }

  def key(name: String, columns: String*): Key = Key(name, columns.toSeq)

  def makeTable(database: String, table: String, columns: Seq[Column], keys: Seq[Key]): String = {
    s"""
       |CREATE TABLE `$database`.`$table` (
       |${columns.map(_.toLine).mkString(s",${System.lineSeparator()}")}
       |-- keys
       |${keys.map(_.toLine).mkString(s",${System.lineSeparator()}")}
       |)
       """.stripMargin

  }

}
