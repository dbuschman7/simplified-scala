package me.lightspeed7.simplified

import java.sql.{Connection, DriverManager, ResultSet}

object Database {

  Class.forName("com.mysql.jdbc.Driver")

  def connect(url: String, user: String, password: String): Connected = {
    val database = url.substring(url.lastIndexOf("/") + 1)
    val conn = DriverManager.getConnection(url, user, password)
    new Connected(conn, database)
  }

  def connectLocal(rootPassword: String, databaseName: String): Connected = {
    connect("jdbc:mysql://localhost:3306/" + databaseName, "root", rootPassword)
  }

  /**
    * WARNING - Use for testing ONLY !!!!
    */
  def dropCreateLocalDatabase(rootPassword: String, databaseName: String): Unit = {
    for (conn <- AutoCloseable(connectLocal(rootPassword, "mysql"))) {
      conn.execute(s"DROP DATABASE IF EXISTS `$databaseName`")
      conn.execute(s"CREATE DATABASE IF NOT EXISTS `$databaseName`")
    }
    ()
  }
}

class Connected(connection: Connection, dbName: String, validTimeout: Int = 5000) extends java.lang.AutoCloseable {

  def database: String = this.dbName

  def isValid: Boolean = connection.isValid(validTimeout)

  def execute(sql: => String): Int = {
    for (stmt <- AutoCloseable(connection.createStatement())) {
      stmt.executeUpdate(sql)
    }
  }

  def query[T](query: => String)(convert: ResultSet => T): List[T] = {
    for (stmt <- AutoCloseable(connection.createStatement())) {
      for (rs <- AutoCloseable(stmt.executeQuery(query))) {
        new Iterator[T] {
          def hasNext: Boolean = rs.next()

          def next(): T = convert(rs)
        }.toList
      }
    }
  }

  def stream(process: ResultSet => Unit)(query: => String): Unit = {
    import java.sql.ResultSet._
    for (stmt <- AutoCloseable(connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY))) {
      stmt.setFetchSize(Integer.MIN_VALUE)
      for (rs <- AutoCloseable(stmt.executeQuery(query))) {
        while (rs.next()) {
          process(rs)
        }
      }
    }
  }

  override def close(): Unit = connection.close()
}