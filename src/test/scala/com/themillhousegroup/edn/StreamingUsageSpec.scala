package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import com.themillhousegroup.edn.test.{StreamChecking, EDNParsing}

class StreamingUsageSpec extends Specification with EDNParsing with StreamChecking {

  "Using the Streaming Scala EDN parser" should {

    "Allow iteration over a flat keyspace" in new ParserScope(
      """:a 1 :b "foo" :c? true """) {

      val s = p.asStream(values)

      keyValueStreamMustHave(s, "a" -> 1, "b" -> "foo" , "c?" -> true)
    }

    "Allow iteration over a flat keyspace with a map within" in new ParserScope(
      """ :a 1 :b "foo" :c? true :d { :da "bar" :db "baz" } """) {

      val s = p.asStream(values).toSeq

      s must haveSize(4)

      s(0) must beEqualTo("a" -> 1)
      s(1) must beEqualTo("b" -> "foo")
      s(2) must beEqualTo("c?" -> true)

      s(3)._1 must beEqualTo("d")
      val nestedStream = s(3)._2.asInstanceOf[Stream[(String, AnyRef)]]
      keyValueStreamMustHave(nestedStream, "da" -> "bar", "db" -> "baz")
    }

    "Allow iteration over a flat keyspace with a vector within" in new ParserScope(
      """ :a 1 :b "foo" :c? true :d [ 5 6 7 8 ] """) {

      val s = p.asStream(values).toSeq

      s must haveSize(4)

      s(0) must beEqualTo("a" -> 1)
      s(1) must beEqualTo("b" -> "foo")
      s(2) must beEqualTo("c?" -> true)

      s(3)._1 must beEqualTo("d")
      val nestedStream = s(3)._2.asInstanceOf[Stream[AnyRef]]
      valueStreamMustHave(nestedStream, 5, 6, 7, 8)
    }

    "Allow iteration over a flat keyspace with a list within" in new ParserScope(
      """ :a 1 :b "foo" :c? true :d ( 5 6 7 8 ) """) {

      val s = p.asStream(values).toSeq

      s must haveSize(4)

      s(0) must beEqualTo("a" -> 1)
      s(1) must beEqualTo("b" -> "foo")
      s(2) must beEqualTo("c?" -> true)

      s(3)._1 must beEqualTo("d")
      val nestedStream = s(3)._2.asInstanceOf[Stream[AnyRef]]
      valueStreamMustHave(nestedStream, 5, 6, 7, 8)
    }

    "Allow iteration over a flat keyspace with a set within" in new ParserScope(
      """ :a 1 :b "foo" :c? true :d #{ 5 6 7 8 } """) {

      val s = p.asStream(values).toSeq

      s must haveSize(4)

      s(0) must beEqualTo("a" -> 1)
      s(1) must beEqualTo("b" -> "foo")
      s(2) must beEqualTo("c?" -> true)

      s(3)._1 must beEqualTo("d")
      val nestedStream = s(3)._2.asInstanceOf[Stream[AnyRef]]
      valueStreamMustHave(nestedStream, 5, 6, 7, 8)
    }

    "Allow iteration over a flat keyspace with a nested map within" in new ParserScope(
      """ :a 1 :b "foo" :c? true
        :d {
             :da "bar"
             :db "baz"
             :dc {
               :dc1 "inner"
               :dc2 "most"
               :dc3 "values"
             }
           } """) {

      val s = p.asStream(values)

      s must haveSize(4)

      keyStreamMustHave(s, "a", "b", "c?", "d")
      val nestedStream = s(3)._2.asInstanceOf[Stream[(String, AnyRef)]]

      keyStreamMustHave(nestedStream, "da", "db", "dc")
      val mostNestedStream = nestedStream.find { case (k, v) =>
        "dc" == (k) }.get._2.asInstanceOf[Stream[(String, AnyRef)]]

      keyValueStreamMustHave(mostNestedStream,
        "dc1" -> "inner",
        "dc2" -> "most",
        "dc3" -> "values"
      )
    }

    "Allow iteration over an EDN expressed as a map" in new ParserScope(""" {:a 1 :b "foo" :c? true }""") {

      val s = p.asStream(values).toSeq
      // As per comments on asStream - we view this as a map that happens to have an empty label
      s must haveSize(1)
      s.head._1 must beEqualTo("")
      val nestedStream = s.head._2.asInstanceOf[Stream[(String, AnyRef)]]
      keyValueStreamMustHave(nestedStream, "a" -> 1, "b" -> "foo" , "c?" -> true)
    }
  }

}
