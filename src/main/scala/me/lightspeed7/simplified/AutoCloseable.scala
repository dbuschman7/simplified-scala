package me.lightspeed7.simplified

class AutoCloseable[A <: java.lang.AutoCloseable](protected val c: A) {

  // make sure the resource is closed
  def map[B](f: (A) => B): B = try {
    f(c)
  } finally {
    c.close()
  }

  def foreach[B](f: (A) => B): B = map(f)

}

object AutoCloseable {
  def apply[A <: java.lang.AutoCloseable](c: A) = new AutoCloseable(c)
}

