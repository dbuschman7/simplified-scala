package me.lightspeed7.simplified

object Table {

  def tableFooter: String = s"ENGINE=InnoDB DEFAULT CHARSET=utf8"

  case class Column(name: String, dataType: String, nullable: Boolean = false, autoIncrement: Boolean = false, default: Option[String] = None) {
    def toDDL: String = {
      val nullPart = if (nullable) "" else " NOT NULL "
      val autoPart = if (autoIncrement) " AUTO_INCREMENT " else ""
      val defPart = default.map(i => s"DEFAULT $i").getOrElse("")

      Seq("`" + name + "`", dataType, nullPart, autoPart, defPart).mkString(" ")
    }
  }

  case class Key(name: String, columns: Seq[String]) {
    def toDDL: String = {
      val cols = columns.map { c => s"`$c`" }
      s"KEY `$name` (${cols.mkString(",")}) "
    }
  }

  def column(name: String, dataType: String, nullable: Boolean = false, autoIncrement: Boolean = false, default: Option[String] = None): Column = {
    Column(name, dataType, nullable, autoIncrement, default)
  }

  def key(name: String, columns: String*): Key = Key(name, columns.toSeq)

  def makeTable(database: String, table: String, columns: Seq[Column], keys: Seq[Key]): String = {
    val sep = System.lineSeparator()
    val cols = (columns.map(_.toDDL) ++ keys.map(_.toDDL)).mkString(sep, s",$sep", sep)
    s"CREATE TABLE `$database`.`$table` ($cols) $tableFooter"
  }

}
