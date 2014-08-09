package com.themillhousegroup.edn

import us.bpsm.edn.parser._
import us.bpsm.edn.parser.Parsers._
import us.bpsm.edn.Keyword._
import us.bpsm.edn.parser.Parser.Config
import us.bpsm.edn.Keyword

object EDNParser {

  def apply() = {
    new ScalaEDNParser(defaultConfiguration)
  }

  def apply(config:Config) = {
    new ScalaEDNParser(config)
  }


  implicit def str2keyword(s:String):Keyword = newKeyword(s)
  implicit def sym2keyword(s:Symbol):Keyword = newKeyword(s.name)

  implicit def keyword2str(k:Keyword):String = k.getName
}

class ScalaEDNParser(config:Config) {
  val javaParser = Parsers.newParser(config)

  private def isFinished(o:AnyRef) = Parser.END_OF_INPUT.equals(o)

  /** A simple wrapper around the basic Java interface,
    * this method keeps returning Some(T) until all values are exhausted
    */
  def nextValue[T](pbr: Parseable):Option[T] = {
    val v = javaParser.nextValue(pbr)

    if (isFinished(v)) {
      None
    } else {
      Option(v).asInstanceOf[Option[T]]
    }
  }

  /** We recurse here wherever there is a chance
    * that a nested object could be another
    * collection that needs streaming.
    * The termination case is when we find a simple object.
    */
  private def streamCollection(a:AnyRef):AnyRef = {
    import scala.collection.JavaConverters._
    a match {
      case m:java.util.Map[AnyRef,AnyRef] => {
        m.asScala.toStream.map {
          case (k:Keyword, v) => k.getName -> streamCollection(v)
          case (s:String, v) => s -> streamCollection(v)
        }
      }
      case m:java.util.List[AnyRef] => {
        m.asScala.toStream.map(streamCollection)
      }
      case m:java.util.Set[AnyRef] => {
        m.asScala.toStream.map(streamCollection)
      }
      case o => o
    }
  }

  private def immutableMap(m:Traversable[(String,AnyRef)]) = {
    Map[String, AnyRef]() ++ m
  }

  private def convertCollection(a:AnyRef):AnyRef = {
    import scala.collection.JavaConverters._
    a match {
      case m:java.util.Map[AnyRef,AnyRef] => {
        immutableMap(m.asScala.map {
          case (k:Keyword, v) => k.getName -> convertCollection(v)
          case (s:String, v) => s -> convertCollection(v)
        })
      }
      case m:java.util.List[AnyRef] => {
        m.asScala.map(convertCollection)
      }
      case m:java.util.Set[AnyRef] => {
        m.asScala.map(convertCollection)
      }
      case o => o
    }
  }

  def processParseable(pbr:Parseable)(valueMapper:AnyRef=>AnyRef):Iterator[(String, AnyRef)] = {
    Stream.continually(
      javaParser.nextValue(pbr))
      .takeWhile (!isFinished(_)).sliding(2,2).map { pair =>
        pair.head match {
          case k:Keyword => k.getName -> valueMapper(pair(1))
          case s:String => s -> valueMapper(pair(1))
          case _ => "" -> valueMapper(pair.head)
        }
    }
  }

  /** Treat an EDN block as if it was a Stream of key-value tuples.
    * This may be suitable if you are dealing with an extremely large
    * Parseable instance and are worried about memory usage.
    * Nested collections will appear as a (String, Stream) tuple
    * within the parent stream.
    * If the entire EDN block is contained within {}, then
    * it will be treated as a Stream with one tuple, "" -> (the content)
    */
  def asStream(pbr: Parseable):Stream[(String, AnyRef)] = {
     processParseable(pbr)(streamCollection).toStream
  }

  /** Treat an EDN block as if it was an immutable Map.
    *
    * Simple key-value pairs will have appropriate value types.
    * Otherwise, there can be nested Map[String, AnyRef],
    * Set[AnyRef] or Seq[AnyRef] collections nested inside.
    *
    */
  def asMap(pbr: Parseable):Map[String, AnyRef] = {
    immutableMap(processParseable(pbr)(convertCollection).toTraversable)
  }
}
