import sbt._
import Keys._

object Version {

  val branch = "git rev-parse --abbrev-ref HEAD".!!.trim
  val commit = "git rev-parse --short HEAD".!!.trim
  val buildTime = (new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")).format(new java.util.Date())
  val newline = System.getProperty("line.separator")

  def makeVersion(major:String, minor:String):String = s"$major.$minor.$commit"

  def hasUnCommited:Boolean = {
    val modFiles = "git status --procelain".!!.trim.split(newline)
    val count = modFiles.size
    if (count > 0) { 
      println(s"Uncommitted changes found - $count") 
      modFiles.foreach{ f => println("  " + f) } 
    }

    count != 0 
  }
}
