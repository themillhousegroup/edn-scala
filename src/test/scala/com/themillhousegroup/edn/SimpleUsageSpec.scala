package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import us.bpsm.edn.parser._
import us.bpsm.edn.parser.Parsers._
import us.bpsm.edn.Keyword._


class SimpleUsageSpec extends Specification {

  val pbr = Parsers.newParseable("{:x 1, :y 2}")

  "Using the EDN parser in Scala" should {

    // The exact example from the edn-java README.md file, just in Scala & Specs2
    "Work when going directly to the Java API (sanity-check)" in {
      val p = Parsers.newParser(defaultConfiguration)
      val m = p.nextValue(pbr).asInstanceOf[java.util.Map[_, _]]

      m.get(newKeyword(("x"))) must beEqualTo(1L)
      m.get(newKeyword(("y"))) must beEqualTo(2L)

      p.nextValue(pbr) must beEqualTo(Parser.END_OF_INPUT)
    }



  }
}
