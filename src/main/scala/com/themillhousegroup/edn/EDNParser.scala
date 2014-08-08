package com.themillhousegroup.edn

import us.bpsm.edn.parser._
import us.bpsm.edn.parser.Parsers._
import us.bpsm.edn.Keyword._
import us.bpsm.edn.parser.Parser.Config

object EDNParser {
  def apply(config:Config) = {
    new ScalaEDNParser(config)
  }
}

class ScalaEDNParser(config:Config) {
  val javaParser = Parsers.newParser(config)

  def nextValue[T](pbr: Parseable):Option[T] = {
    val v = javaParser.nextValue(pbr)

    if (Parser.END_OF_INPUT.equals(v)) {
      None
    } else {
      Option(v).asInstanceOf[Option[T]]
    }
  }
}
