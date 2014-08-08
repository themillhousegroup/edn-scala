package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import us.bpsm.edn.parser.Parsers

class MapLikeUsageSpec extends Specification {




  "Treating the EDN file like a Map" should {

    "Allow clients to select individual keys from a simple flat structure" in {

      val nestedExample = Parsers.newParseable(
        """ { :x 1,
                :y 2
          } """
      )

      val p = EDNParser()
      val m = p.asMap(nestedExample)


      m.size must beEqualTo(2)
    }

//    "Allow clients to select individual keys" in {
//
//      val nestedExample = Parsers.newParseable(
//              """ { :x 1,
//                :y 2
//                :z { :foo 11 :bar 12 :baz 13}
//          } """
//      )
//
//      val p = EDNParser()
//      val m = p.asMap(nestedExample)
//      true must beTrue
//    }
  }

}
