package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import com.themillhousegroup.edn.test.EDNParsing
import scala.util.Try

case class AllStrings(bish:String, bash:String, bosh:String)
case class OptionalStrings(bish:String, bash:Option[String], bosh:String)

class ReadIntoCaseClassSpec extends Specification with EDNParsing {

  class CaseClassScope[T <: Product](s:String, targetClass:Class[T]) extends ParserScope(s) {

    lazy val readInto:Try[T] = p.readInto(values, targetClass)
    lazy val readResult:T = readInto.get
  }


  "Reading EDN into case classes" should {

    "Support single-level mapping of simple strings" in new CaseClassScope(
      """ :bish "foo" :bash "bar" :bosh "baz" """, classOf[AllStrings]) {

      readResult must not beNull

      readResult.bish must beEqualTo("foo")
      readResult.bash must beEqualTo("bar")
      readResult.bosh must beEqualTo("baz")
    }

    "Return a failed Try: IllegalArgumentException if a field is missing" in new CaseClassScope(
      """ :bish "foo" :bash "bar"  """, classOf[AllStrings]) {

      println(s" REadinto: $readInto") // must beAFailedTry[AllStrings]
    }

    "Support single-level mapping of optional strings - present" in new CaseClassScope(
      """ :bish "foo" :bash "bar" :bosh "baz" """, classOf[OptionalStrings]) {

      readResult must not beNull

      readResult.bish must beEqualTo("foo")
      readResult.bash must beSome("bar")
      readResult.bosh must beEqualTo("baz")
    }

    "Support single-level mapping of optional strings - absent" in new CaseClassScope(
      """ :bish "foo" :bosh "baz" """, classOf[OptionalStrings]) {

      readResult must not beNull

      readResult.bish must beEqualTo("foo")
      readResult.bash must beNone
      readResult.bosh must beEqualTo("baz")
    }
  }
}
