package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import com.themillhousegroup.edn.test.EDNParsing
import scala.util.Try
import com.themillhousegroup.edn.test.CaseClassFixtures._


class ReadIntoFlatCaseClassSpec extends Specification with EDNParsing {

  class CaseClassScope[T <: Product](s:String, targetClass:Class[T]) extends ParserScope(s) {

    lazy val readInto:Try[T] = p.readInto(values, targetClass)
    lazy val readResult:T = readInto.get
  }

  case class CannotCreate(x:Int, y:String)

  "Reading EDN into case classes - flat structures -" should {

    "Reject a case class that won't be instantiable" in new CaseClassScope(
      """ :x "foo" :y "bar" """, classOf[CannotCreate]) {

      readInto must beAFailedTry[CannotCreate].withThrowable[UnsupportedOperationException]
    }

    "Support single-level mapping of simple strings" in new CaseClassScope(
      """ :bish "foo" :bash "bar" :bosh "baz" """, classOf[AllStrings]) {

      readResult must not beNull

      readResult.bish must beEqualTo("foo")
      readResult.bash must beEqualTo("bar")
      readResult.bosh must beEqualTo("baz")
    }

    "Return a failed Try: IllegalArgumentException if a field is missing" in new CaseClassScope(
      """ :bish "foo" :bash "bar"  """, classOf[AllStrings]) {

      readInto must beAFailedTry[AllStrings].withThrowable[IllegalArgumentException]
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


    "Support automatic mapping of Longs to Ints" in new CaseClassScope(
      """ :bash 6 :bosh 9 """, classOf[IntsNotLongs]) {

      readResult must not beNull

      readResult.bash must beSome(6)
      readResult.bosh must beEqualTo(9)
    }

    "Support Longs in case classes" in new CaseClassScope(
      """ :bash 6 :bosh 9 """, classOf[AllLongs]) {

      readResult must not beNull

      readResult.bash must beSome(6)
      readResult.bosh must beEqualTo(9)
    }

    "Support single-level mapping of mixed types" in new CaseClassScope(
      """ :bish "foo" :bash 6 :bosh 9 """, classOf[MixedBunch]) {

      readResult must not beNull

      readResult.bish must beEqualTo("foo")
      readResult.bash must beSome(6)
      readResult.bosh must beEqualTo(9)
    }
  }
}
