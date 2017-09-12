package me.lightspeed7.simplified

import java.sql.{Connection, DriverManager}

object Database {

  Class.forName("com.mysql.jdbc.Driver")

  def connect(url: String, user: String, password: String): Connected = {
    val database = url.substring(url.lastIndexOf("/") + 1)
    val conn = DriverManager.getConnection(url, user, password)
    new Connected(conn, database)
  }

  def connectLocal(rootPassword: String, databaseName: String) = {
    connect("jdbc:mysql://localhost:3306/" + databaseName, "root", rootPassword)
  }

  /**
    * WARNING - Use for testing ONLY !!!!
    */
  def dropCreateLocalDatabase(rootPassword: String, databaseName: String): Unit = {
    for (conn <- AutoCloseable(connectLocal(rootPassword, "mysql"))) {
      println(s"Dropping database - $databaseName")
      conn.execute(s"DROP DATABASE IF EXISTS `$databaseName`")
      println(s"Creating database - $databaseName")
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

  override def close(): Unit = connection.close()
}