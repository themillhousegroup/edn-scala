package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import us.bpsm.edn.parser.Parsers

class MapLikeUsageSpec extends Specification {

  val nestedExample = Parsers.newParseable(
    """ { :x 1,
          :y 2
          :z { :foo 11 :bar 12 :baz 13}"""
  )


  "Treating the EDN file like a Map" should {

    "Allow clients to select individual keys" in {

      true must beTrue
    }
  }

}
