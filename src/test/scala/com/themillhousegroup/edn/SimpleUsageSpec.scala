package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import us.bpsm.edn.parser._
import us.bpsm.edn.parser.Parsers._
import us.bpsm.edn.Keyword._
import us.bpsm.edn.Keyword
import com.themillhousegroup.edn.test.EDNParsing


class SimpleUsageSpec extends Specification with EDNParsing {

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

  "Using the Scala EDN parser's strongly-typed nextValue method on simple values" should {

    "Provide typed Some responses when values are found" in new ParserScope(" :a 1 :b 2 :c 3") {

      p.nextValue[Keyword](values) must beSome(newKeyword("a"))
      p.nextValue[Int](values) must beSome(1L)
      p.nextValue[Keyword](values) must beSome(newKeyword("b"))
      p.nextValue[Int](values) must beSome(2L)
      p.nextValue[Keyword](values) must beSome(newKeyword("c"))
      p.nextValue[Int](values) must beSome(3L)
    }

    "Support various forms of key identifiers and values" in new ParserScope(""" :a 1 :b? true :c "localhost" """) {

      p.nextValue[Keyword](values) must beSome(newKeyword("a"))
      p.nextValue[Int](values) must beSome(1L)
      p.nextValue[Keyword](values) must beSome(newKeyword("b?"))
      p.nextValue[Boolean](values) must beSome(true)
      p.nextValue[Keyword](values) must beSome(newKeyword("c"))
      p.nextValue[String](values) must beSome("localhost")
    }

    "Provide a None response for an empty Parseable" in new ParserScope("") {

      p.nextValue[Int](values) must beNone
    }

    "Provide a None response when running off the end of a Parseable" in new ParserScope(":f 99") {

      p.nextValue[Keyword](values) must beSome(newKeyword("f"))
      p.nextValue[Int](values) must beSome(99L)
      p.nextValue[Int](values) must beNone
    }
  }
}
