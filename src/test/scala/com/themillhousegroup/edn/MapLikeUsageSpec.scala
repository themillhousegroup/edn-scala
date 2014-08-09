package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import com.themillhousegroup.edn.test.EDNParsing

class MapLikeUsageSpec extends Specification with EDNParsing {


  "Treating the EDN file like a Map" should {

    "Allow clients to select individual keys from a simple flat structure" in new ParserScope(
      """  :x 1, :y 2  """ ) {

      val m = p.asMap(values)

      m must haveSize(2)

      m must havePairs("x" -> 1, "y" -> 2)
    }

    "Allow clients to select nested keys from a simple flat structure" in new ParserScope(
      """  :x 1, :y 2  :z { :za true :zb "enabled" } """  ) {

      val m = p.asMap(values)

      m must haveSize(3)

      m must havePairs("x" -> 1, "y" -> 2)

      println(m("z").getClass)

      val nested = m("z").asInstanceOf[Map[String, AnyRef]]
      nested must haveSize(2)

      nested must havePairs("za" -> true, "zb" -> "enabled")
    }

    "Allow clients to select lists from a simple flat structure" in new ParserScope(
      """  :x 1, :y 2  :z [ 2 4 6 8 ] """  ) {

      val m = p.asMap(values)

      m must haveSize(3)

      m must havePairs("x" -> 1, "y" -> 2)

      println(m("z").getClass)

      val list = m("z").asInstanceOf[Seq[Int]]
      list must haveSize(4)

      list must containTheSameElementsAs(Seq(2, 4, 6, 8))
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
