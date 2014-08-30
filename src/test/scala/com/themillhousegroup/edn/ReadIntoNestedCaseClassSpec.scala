package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import com.themillhousegroup.edn.test.EDNParsing
import scala.util.Try
import scala.reflect.runtime.universe._

import com.themillhousegroup.edn.test.CaseClassFixtures._

class ReadIntoNestedCaseClassSpec extends Specification with EDNParsing {

  //  "Reading EDN into case classes - nested structures -" should {
  //
  //    "Support nested mapping of case classes" in new CaseClassScope(
  //      """ :contents { :bish "foo" :bash "bar" :bosh "baz" } """) {
  //
  //      val readResult = readIntoResult[NestedJustOnce]
  //      readResult must not beNull
  //
  //      readResult.contents must not beNull
  //
  //      readResult.contents.bish must beEqualTo("foo")
  //      readResult.contents.bash must beEqualTo("bar")
  //      readResult.contents.bosh must beEqualTo("baz")
  //    }
  //
  //    "Support nested mapping of case classes together with simple fields" in new CaseClassScope(
  //      """ :contents { :bish "foo" :bash "bar" :bosh "baz" } :a 1 :b 2""") {
  //
  //      val readResult = readIntoResult[NestedWithFields]
  //      readResult must not beNull
  //
  //      readResult.contents must not beNull
  //
  //      readResult.contents.bish must beEqualTo("foo")
  //      readResult.contents.bash must beEqualTo("bar")
  //      readResult.contents.bosh must beEqualTo("baz")
  //      readResult.a must beEqualTo(1)
  //      readResult.b must beEqualTo(2)
  //    }
  //
  //    "Support nested optional case classes - positive case" in new CaseClassScope(
  //      """ :contents { :bish "foo" :bash "bar" :bosh "baz" } """) {
  //
  //      val readResult = readIntoResult[NestedOptionally]
  //
  //      readResult must not beNull
  //
  //      readResult.contents must beSome[AllStrings]
  //
  //      val c = readResult.contents.get
  //
  //      c must beEqualTo("foo")
  //      readResult.contents.get.bash must beEqualTo("bar")
  //      readResult.contents.get.bosh must beEqualTo("baz")
  //    }
  //
  //    "Support nested optional case classes - negative case" in new CaseClassScope(
  //      """  """) {
  //
  //      val readResult = readIntoResult[NestedOptionally]
  //
  //      readResult must not beNull
  //
  //      readResult.contents must beNone
  //    }
  //  }
}
