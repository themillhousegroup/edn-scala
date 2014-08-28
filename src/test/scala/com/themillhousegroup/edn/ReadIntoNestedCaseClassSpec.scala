package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import com.themillhousegroup.edn.test.EDNParsing
import scala.util.Try
import com.themillhousegroup.edn.test.CaseClassFixtures._

class ReadIntoNestedCaseClassSpec extends Specification with EDNParsing {

  class CaseClassScope[T <: Product](s: String, targetClass: Class[T]) extends ParserScope(s) {

    lazy val readInto: Try[T] = p.readInto(values)
    lazy val readResult: T = readInto.get
  }

  case class CannotCreate(x: Int, y: String)

  "Reading EDN into case classes - nested structures -" should {

    "Support nested mapping of case classes" in new CaseClassScope(
      """ :contents { :bish "foo" :bash "bar" :bosh "baz" } """, classOf[NestedJustOnce]) {

      readResult must not beNull

      readResult.contents must not beNull

      readResult.contents.bish must beEqualTo("foo")
      readResult.contents.bash must beEqualTo("bar")
      readResult.contents.bosh must beEqualTo("baz")
    }

    "Support nested mapping of case classes together with simple fields" in new CaseClassScope(
      """ :contents { :bish "foo" :bash "bar" :bosh "baz" } :a 1 :b 2""", classOf[NestedWithFields]) {

      readResult must not beNull

      readResult.contents must not beNull

      readResult.contents.bish must beEqualTo("foo")
      readResult.contents.bash must beEqualTo("bar")
      readResult.contents.bosh must beEqualTo("baz")
      readResult.a must beEqualTo(1)
      readResult.b must beEqualTo(2)
    }

    "Support nested optional case classes - positive case" in new CaseClassScope(
      """ :contents { :bish "foo" :bash "bar" :bosh "baz" } """, classOf[NestedOptionally]) {

      readResult must not beNull

      readResult.contents must beSome[AllStrings]

      val c = readResult.contents.get

      c must beEqualTo("foo")
      readResult.contents.get.bash must beEqualTo("bar")
      readResult.contents.get.bosh must beEqualTo("baz")
    }

    "Support nested optional case classes - negative case" in new CaseClassScope(
      """  """, classOf[NestedOptionally]) {

      readResult must not beNull

      readResult.contents must beNone
    }
  }
}
