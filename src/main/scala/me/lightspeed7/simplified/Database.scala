package me.lightspeed7.simplified

import java.sql.{ Connection, DriverManager, ResultSet }

object Database {

  Class.forName("com.mysql.jdbc.Driver")

  /**
   * Connect to a MySQL database
   *
   * @param url - use jdbc:mysql://host:3306/database format here
   * @param user
   * @param password
   * @return Connected object wrapping a connection
   */
  def connect(url: String, user: String, password: String): Connected = {
    val database = url.substring(url.lastIndexOf("/") + 1)
    val conn = DriverManager.getConnection(url, user, password)
    new Connected(conn, database)
  }

  /**
   * Use for testing ONLY !!!!
   *
   * @param rootPassword
   * @param databaseName
   */
  def dropCreateLocalDatabase(rootPassword: String, databaseName: String): Unit = {
    for (conn <- AutoCloseable(connect("jdbc:mysql://localhost:3306/mysql", "root", rootPassword))) {
      conn.execute(s"DROP DATABASE IF EXISTS `$databaseName`")
      conn.execute(s"CREATE DATABASE IF NOT EXISTS `$databaseName`")
    }
    ()
  }
}

class Connected(connection: Connection, dbName: String, validTimeout: Int = 5000) extends java.lang.AutoCloseable {

  import java.sql.ResultSet._

  def database: String = this.dbName

  def isValid: Boolean = connection.isValid(validTimeout)

  def query[T](query: String, convert: ResultSet => T): List[T] = {
    for (stmt <- AutoCloseable(connection.createStatement())) {
      for (rs <- AutoCloseable(stmt.executeQuery(query))) {
        new Iterator[T] {
          def hasNext: Boolean = rs.next()

          def next(): T = convert(rs)
        }.toList
      }
    }
  }

  def stream(query: String, process: ResultSet => Unit): Unit = {
    for (stmt <- AutoCloseable(connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY))) {
      stmt.setFetchSize(Integer.MIN_VALUE)
      for (rs <- AutoCloseable(stmt.executeQuery(query))) {
        while (rs.next()) {
          process(rs)
        }
      }
    }
  }

  def execute(sql: String): Int = {
    for (stmt <- AutoCloseable(connection.createStatement())) {
      stmt.executeUpdate(sql)
    }
  }

  override def close() = connection.close()
}