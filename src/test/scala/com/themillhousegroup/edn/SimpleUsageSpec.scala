package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import us.bpsm.edn.parser._
import us.bpsm.edn.parser.Parsers._
import us.bpsm.edn.Keyword._
import us.bpsm.edn.Keyword


class SimpleUsageSpec extends Specification {

  def parse(s:String) = Parsers.newParseable(s)

  val pbr = parse("{:x 1, :y 2}")

  "Using the Java EDN parser in Scala" should {

    // The exact example from the edn-java README.md file, just in Scala & Specs2
    "Work when going directly to the Java API (sanity-check)" in {
      val p = Parsers.newParser(defaultConfiguration)
      val m = p.nextValue(pbr).asInstanceOf[java.util.Map[_, _]]

      m.get(newKeyword(("x"))) must beEqualTo(1L)
      m.get(newKeyword(("y"))) must beEqualTo(2L)

      p.nextValue(pbr) must beEqualTo(Parser.END_OF_INPUT)
    }
  }

  "Using the Scala parser's strongly-typed nextValue method" should {

    "Provide typed Some responses when values are found" in {
      val p = EDNParser(defaultConfiguration)

      val simpleValues = parse(" :a 1 :b 2 :c 3")

      p.nextValue[Keyword](simpleValues) must beSome(newKeyword("a"))
      p.nextValue[Int](simpleValues) must beSome(1L)
      p.nextValue[Keyword](simpleValues) must beSome(newKeyword("b"))
      p.nextValue[Int](simpleValues) must beSome(2L)
      p.nextValue[Keyword](simpleValues) must beSome(newKeyword("c"))
      p.nextValue[Int](simpleValues) must beSome(3L)
    }

    "Provide a None response for an empty Parseable" in {
      val p = EDNParser(defaultConfiguration)

      val empty = parse("")

      p.nextValue[Int](empty) must beNone
    }

    "Provide a None response when running off the end of a Parseable" in {
      val p = EDNParser(defaultConfiguration)

      val values = parse(":f 99")

      p.nextValue[Keyword](values) must beSome(newKeyword("f"))
      p.nextValue[Int](values) must beSome(99L)
      p.nextValue[Int](values) must beNone
    }


  }
}
