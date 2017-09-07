package me.lightspeed7.simplified

import java.sql.ResultSet

import com.typesafe.scalalogging.StrictLogging
import me.lightspeed7.simplified.Table._
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

class DatabaseTest extends FunSuite with Matchers with BeforeAndAfterAll with StrictLogging {

  val dbName = "shakespeare"
  val tableName = "detail"

  override def beforeAll(): Unit = {

  }

  test("Read data file and write dat to detail and summary tables") {

    import DataHelper._

    // reset the database to virgin
    Database.dropCreateLocalDatabase("password", dbName)

    // create the table to insert data into
    val columns = Seq( //
      Column("line", "varchar(255)"), //
      Column("num", "integer") //
    )

    val keys = Seq(Key("num_idx", Seq("num")))

    for (conn <- AutoCloseable(Database.connectLocal("password", dbName))) {
      val sql = Table.makeTable(dbName, tableName, columns, keys)
      println(sql)
      conn.execute(sql)
      ()
    }

    // read the data from the file
    val lines = getFileData("shakespeare.txt")
      .zipWithIndex
      .map {
        case (l, i) => Line(l, i)
      }

    // connect to the database
    for (conn <- AutoCloseable(Database.connectLocal("password", dbName))) {

      // write data to local table
      Time.it(s"Inserting data into the database - ${lines.size} records") {
        for (group <- lines.grouped(100)) {
          val lines = group.map(line => s"(${line.number}, '${line.line.replace("'", "''")}' )")
          conn.execute(s"INSERT INTO `$dbName`.`$tableName` (num, line) VALUES ${lines.mkString(",")}")
        }
      }

      // read a subset back and print it out, 1185 - 1198
      println
      Time.it("Getting a sonnet") {
        val sql = s"SELECT line from `$dbName`.`$tableName` WHERE (num >= 1185 && num <= 1198)"
        println(sql)
        println
        conn.query(sql) { rs: ResultSet =>
          println(rs.getString(1))
        }
      }

      println
      val sonnetLines: Seq[Line] = Time.it("Getting a sonnet lines") {
        val sql = s"SELECT line, num from `$dbName`.`$tableName` WHERE (num >= 1185 && num <= 1198)"
        println(sql)
        println
        conn.query(sql) { rs: ResultSet => Line(rs.getString(1), rs.getInt(2)) }
      }

      sonnetLines.foreach(println)
    }


  }

}
