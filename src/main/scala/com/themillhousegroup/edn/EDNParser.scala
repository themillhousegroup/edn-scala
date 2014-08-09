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
      case m:java.util.Map[Keyword,AnyRef] => {
        m.asScala.toStream.map { case (k, v) => k.getName -> streamCollection(v)}
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


  /** Treat an EDN block as if it was a Stream of key-value tuples.
    * This may be suitable if you are dealing with an extremely large
    * Parseable instance and are worried about memory usage.
    * Nested collections will appear as a (String, Stream) tuple
    * within the parent stream.
    * If the entire EDN block is contained within {}, then
    * it will be treated as a Stream with one tuple, "" -> (the content) */
  def asStream(pbr: Parseable):Stream[(String, AnyRef)] = {

    Stream.continually(
      javaParser.nextValue(pbr))
          .takeWhile (!isFinished(_)).sliding(2,2).map { pair =>
      if (pair.head.isInstanceOf[Keyword]) {
        val k = pair.head.asInstanceOf[Keyword]
        val v = streamCollection(pair(1))
        k.getName -> v
      } else {
        "" -> streamCollection(pair.head)
      }
    }.toStream
  }

//  def asMap(pbr: Parseable):Map[String, AnyRef] = {
//
//    //var lastKeyword:Option[String] = None
//
//    val n = javaParser.nextValue(pbr)
//
//    if (nextValue(pbr).isDefined) {
//      scala.collection.immutable.Map[String, AnyRef]()
//    } else  {
//      import scala.collection.JavaConverters._
//      n match {
//        case m:java.util.Map[Keyword,AnyRef] => m.asScala.toMap.map { case(k,v) =>
//          k.getName -> v
//        }
//      }
//    }
//
//
//
//
//
//    // Not dealing with nested maps just yet
////    Stream.continually(
////      javaParser.nextValue(pbr))
////      .takeWhile (!isFinished(_)).flatMap { parseResult =>
////
////
////        parseResult match {
////          case k:Keyword => lastKeyword = Some(k.getName); None
////          case m:Map =>
////          case z =>  println(s"Got a ${z} which is a ${z.getClass}"); Some(lastKeyword, z)
////        }
////    }.toMap
//
//
//  }
}
