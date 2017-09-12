package me.lightspeed7.simplified

import org.scalatest.{FunSuite, Matchers}

class UtilsTest extends FunSuite with Matchers {

  test("Cool Strings") {
    import CoolStrings._

    "foo".notEmpty should be(Some("foo"))

    val foo: String = null
    foo.notBlank should be(None)

    foo.notNull should be("")
    "foo".notNull should be("foo")
  }

  test("Time it ") {
    var result: String = null
    Time.it("Time me", (in) => result = in) {
      Thread.sleep(500)
    }
    println(result)
    result.contains("Elapsed") should be(true)
  }

  test("File sizing") {
    PrettyPrint.fileSizing(1234) should be("1.2 K")
    PrettyPrint.fileSizing(1234567) should be("1.2 M")
    PrettyPrint.fileSizing(1234567890) should be("1.1 G")
  }

}
