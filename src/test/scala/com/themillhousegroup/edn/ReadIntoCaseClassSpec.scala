package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import com.themillhousegroup.edn.test.EDNParsing

class ReadIntoCaseClassSpec extends Specification with EDNParsing {

  class CaseClassScope[T <: Product](s:String, targetClass:Class[T]) extends ParserScope(s) {

    val readResult:T = p.readInto[T](values, targetClass)
  }


  "Reading EDN into case classes" should {

    case class AllStrings(bish:String, bash:String, bosh:String)

    "Support single-level mapping of simple strings" in new CaseClassScope(
      """ :bish "foo" :bash "bar" :bosh "baz" """, classOf[AllStrings]) {

      readResult must not beNull

      readResult.bish must beEqualTo("foo")
      readResult.bash must beEqualTo("bar")
      readResult.bosh must beEqualTo("baz")
    }
  }
}
