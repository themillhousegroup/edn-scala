package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import com.themillhousegroup.edn.test.EDNParsing
import scala.util.Try
import scala.reflect.runtime.universe._

import com.themillhousegroup.edn.test.CaseClassFixtures._

class ReadIntoNestedCaseClassSpec extends Specification with EDNParsing {

  "Reading EDN into case classes - nested structures -" should {

    "Support nested mapping of case classes" in new CaseClassScope(
      """ :contents { :bish "foo" :bash "bar" :bosh "baz" } """) {

      val readResult = readIntoResult[NestedJustOnce]
      readResult must not beNull

      readResult.contents must not beNull

      readResult.contents.bish must beEqualTo("foo")
      readResult.contents.bash must beEqualTo("bar")
      readResult.contents.bosh must beEqualTo("baz")
    }

    "Support nested mapping of case classes together with simple fields" in new CaseClassScope(
      """ :contents { :bish "foo" :bash "bar" :bosh "baz" } :a 1 :b 2""") {

      val readResult = readIntoResult[NestedWithFields]
      readResult must not beNull

      readResult.contents must not beNull

      readResult.contents.bish must beEqualTo("foo")
      readResult.contents.bash must beEqualTo("bar")
      readResult.contents.bosh must beEqualTo("baz")
      readResult.a must beEqualTo(1)
      readResult.b must beEqualTo(2)
    }

    "Support nested optional case classes - positive case" in new CaseClassScope(
      """ :contents { :bish "foo" :bash "bar" :bosh "baz" } """) {

      val readResult = readIntoResult[NestedOptionally]

      readResult must not beNull

      readResult.contents must beSome[AllStrings]

      val c = readResult.contents.get

      c.bish must beEqualTo("foo")
      c.bash must beEqualTo("bar")
      c.bosh must beEqualTo("baz")
    }

    "Support nested optional case classes - negative case" in new CaseClassScope(
      """  """) {

      val readResult = readIntoResult[NestedOptionally]

      readResult must not beNull

      readResult.contents must beNone
    }
  }

  "Support double-nested optional case classes" in new CaseClassScope(
    """ {
      :first { :bish "foo" :bash "bar" :bosh "baz" }
      :second { :bish "curly" :bash "larry" :bosh "moe" }
    } """) {

    val readResult = readIntoResult[StringsAllTheWayDown]

    readResult must not beNull

    readResult.first must beAnInstanceOf[AllStrings]
    readResult.second must beSome[AllStrings]

    val first = readResult.first

    first.bish must beEqualTo("foo")
    first.bash must beEqualTo("bar")
    first.bosh must beEqualTo("baz")

    val second = readResult.second.get

    second.bish must beEqualTo("curly")
    second.bash must beEqualTo("larry")
    second.bosh must beEqualTo("moe")
  }

  "Support deeply-nested optional case classes - positive case" in new CaseClassScope(
    """ {
            :x 7 :y 11
            :nest {
              :first { :bish "foo" :bash "bar" :bosh "baz" }
              :second { :bish "curly" :bash "larry" :bosh "moe" }
           } } """) {

    val readResult = readIntoResult[ThreeLevelsDeep]

    readResult must not beNull

    readResult.x must beEqualTo(7)
    readResult.y must beEqualTo(11)
    readResult.nest must beSome[StringsAllTheWayDown]

    val nest = readResult.nest.get
    nest.first must beAnInstanceOf[AllStrings]
    nest.second must beSome[AllStrings]

    val first = nest.first

    first.bish must beEqualTo("foo")
    first.bash must beEqualTo("bar")
    first.bosh must beEqualTo("baz")

    val second = nest.second.get

    second.bish must beEqualTo("curly")
    second.bash must beEqualTo("larry")
    second.bosh must beEqualTo("moe")
  }
}
