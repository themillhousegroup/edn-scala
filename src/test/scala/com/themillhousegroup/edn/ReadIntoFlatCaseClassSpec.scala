package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import com.themillhousegroup.edn.test.EDNParsing
import scala.util.Try
import com.themillhousegroup.edn.test.CaseClassFixtures._
import scala.reflect.runtime.universe._

class ReadIntoFlatCaseClassSpec extends Specification with EDNParsing {

  class CaseClassScope[T <: Product: TypeTag](s: String) extends ParserScope(s) {

    println(s"CCS: ${typeOf[T]}")
    lazy val readInto: Try[T] = p.readInto(values)

    lazy val readResult: T = readInto.get
  }

  case class CannotCreate(x: Int, y: String)

  "Reading EDN into case classes - flat structures -" should {

    class AllStringsScope(s: String) extends CaseClassScope[AllStrings](s) {}

    //    "Reject a case class that won't be instantiable" in new CaseClassScope(
    //      """ :x "foo" :y "bar" """, classOf[CannotCreate]) {
    //
    //      readInto must beAFailedTry[CannotCreate].withThrowable[UnsupportedOperationException]
    //    }
    //
    "Support single-level mapping of simple strings" in new AllStringsScope(
      """ :bish "foo" :bash "bar" :bosh "baz" """) {

      readResult must not beNull

      readResult.bish must beEqualTo("foo")
      readResult.bash must beEqualTo("bar")
      readResult.bosh must beEqualTo("baz")
    }

    "Return a failed Try: IllegalArgumentException if a field is missing" in new AllStringsScope(
      """ :bish "foo" :bash "bar"  """) {

      readInto must beAFailedTry[AllStrings].withThrowable[IllegalArgumentException]
    }
  }

  "Reading EDN into case classes - flat structures with options -" should {
    class OptionalStringsScope(s: String) extends CaseClassScope[OptionalStrings](s) {}

    //    "Support single-level mapping of optional strings - present" in new CaseClassScope[OptionalStrings](
    //      """ :bish "foo" :bash "bar" :bosh "baz" """) {
    //
    //      readResult must not beNull
    //
    //      readResult.bish must beEqualTo("foo")
    //      readResult.bash must beSome("bar")
    //      readResult.bosh must beEqualTo("baz")
    //    }

    "Support single-level mapping of optional strings - absent" in new OptionalStringsScope(
      """ :bish "foo" :bosh "baz" """) {

      readResult must not beNull

      readResult.bish must beEqualTo("foo")
      readResult.bash must beNone
      readResult.bosh must beEqualTo("baz")
    }

    //    "Support automatic mapping of Longs to Ints" in new CaseClassScope(
    //      """ :bash 6 :bosh 9 """, classOf[IntsNotLongs]) {
    //
    //      readResult must not beNull
    //
    //      readResult.bash must beSome(6)
    //      readResult.bosh must beEqualTo(9)
    //    }
    //
    //    "Support Longs in case classes" in new CaseClassScope[AllLongs](
    //      """ :bash 6 :bosh 9 """) {
    //
    //      readResult must not beNull
    //
    //      readResult.bash must beSome(6)
    //      readResult.bosh must beEqualTo(9)
    //    }
    //
    //    "Support single-level mapping of mixed types" in new CaseClassScope(
    //      """ :bish "foo" :bash 6 :bosh 9 """, classOf[MixedBunch]) {
    //
    //      readResult must not beNull
    //
    //      readResult.bish must beEqualTo("foo")
    //      readResult.bash must beSome(6)
    //      readResult.bosh must beEqualTo(9)
    //    }
  }
}
