package me.lightspeed7.simplified

import java.sql.ResultSet

import me.lightspeed7.simplified.Files.DelimitedSerializable
import org.scalatest.{FunSuite, Matchers}

class DatabaseTest extends FunSuite with Matchers {

  test("Database Test") {

    import Database._
    import Table._

    val dbName = "shakespeare"
    val tableName = "lines"

    // reset the database to virgin
    dropCreateLocalDatabase("password", dbName)

    // create the table to insert data into
    val columns = Seq( //
      Column("line", "varchar(255)"), //
      Column("num", "integer") //
    )

    val keys = Seq(Key("num_idx", Seq("num")))

    for (conn <- AutoCloseable(connectLocal("password", dbName))) {
      val sql = makeTable(dbName, tableName, columns, keys)
      println(sql)
      conn.execute(sql)
      ()
    }

    // read the data from the file
    case class Line(number: Int, line: String) extends DelimitedSerializable {
      def toDelimitedString: String = s"$number|$line"
    }

    val lines = DataHelper.getFileData("shakespeare.txt")
      .zipWithIndex
      .map {
        case (l, i) => Line(i + 1, l)
      }

    // connect to the database
    for (conn <- AutoCloseable(Database.connectLocal("password", dbName))) {

      // write data to local table
      println("#########################################################")
      println("## Insert")
      println("#########################################################")

      Time.it(s"Inserting data into the database - ${lines.size} records") {
        for (group <- lines.grouped(100)) {
          val lines = group.map(line => s"( ${line.number}, '${line.line.replace("'", "''")}' )")
          conn.execute(s"INSERT INTO `$dbName`.`$tableName` (num, line) VALUES ${lines.mkString(",")}")
        }
      }

      // read a subset back and print it out, 1185 - 1198
      //
      // Two ways -
      // 1. Stream interactively - large result sets
      // 2. Return a List of results - small result sets
      println("#########################################################")
      println("## Stream")
      println("#########################################################")

      Time.it("Streaming a sonnet") {
        val sql = s"SELECT line from `$dbName`.`$tableName` WHERE (num >= 1185 && num <= 1198)  ORDER BY num"
        println(sql)
        println
        conn.stream({ rs: ResultSet => println(rs.getString(1)) })(sql)
      }

      println("#########################################################")
      println("## Query for domain objects")
      println("#########################################################")

      def convertToCaseClass(rs: ResultSet): Line = {
        val num = rs.getInt(2)
        val line = rs.getString(1)
        Line(num, line)
      }

      val sonnetLines: Seq[Line] = Time.it("Querying a sonnet for domain objects") {
        val sql = s"SELECT line, num from `$dbName`.`$tableName` WHERE (num >= 1185 && num <= 1198) ORDER BY num"
        println(sql)
        println
        conn.query(sql)(convertToCaseClass)
      }
      sonnetLines.foreach(println)


      println("#########################################################")
      println("## Domain object performance")
      println("#########################################################")
      val _ = Time.it(s"Time to create ${lines.length} case classes") {
        conn.query(s"SELECT line, num from `$dbName`.`$tableName`")(convertToCaseClass)
      }.sortBy(_.number)
    }

  }

}
