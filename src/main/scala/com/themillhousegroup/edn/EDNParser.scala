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
}

class ScalaEDNParser(config:Config) {
  val javaParser = Parsers.newParser(config)

  private def isFinished(o:AnyRef) = Parser.END_OF_INPUT.equals(o)

  def nextValue[T](pbr: Parseable):Option[T] = {
    val v = javaParser.nextValue(pbr)

    if (isFinished(v)) {
      None
    } else {
      Option(v).asInstanceOf[Option[T]]
    }
  }

  def asMap(pbr: Parseable):Map[String, AnyRef] = {

    //var lastKeyword:Option[String] = None

    val n = javaParser.nextValue(pbr)

    if (nextValue(pbr).isDefined) {
      scala.collection.immutable.Map[String, AnyRef]()
    } else  {
      import scala.collection.JavaConverters._
      n match {
        case m:java.util.Map[Keyword,AnyRef] => m.asScala.toMap.map { case(k,v) =>
          k.getName -> v
        }
      }
    }





    // Not dealing with nested maps just yet
//    Stream.continually(
//      javaParser.nextValue(pbr))
//      .takeWhile (!isFinished(_)).flatMap { parseResult =>
//
//
//        parseResult match {
//          case k:Keyword => lastKeyword = Some(k.getName); None
//          case m:Map =>
//          case z =>  println(s"Got a ${z} which is a ${z.getClass}"); Some(lastKeyword, z)
//        }
//    }.toMap


  }
}
