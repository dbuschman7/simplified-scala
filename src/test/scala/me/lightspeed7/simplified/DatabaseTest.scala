package me.lightspeed7.simplified

import org.scalatest.{ FunSuite, Matchers }

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
  }
}
