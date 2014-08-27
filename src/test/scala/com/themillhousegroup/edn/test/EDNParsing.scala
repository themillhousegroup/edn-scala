package com.themillhousegroup.edn.test

import us.bpsm.edn.parser.Parsers
import org.specs2.specification.Scope
import com.themillhousegroup.edn.EDNParser

trait EDNParsing {

  def parse(s: String) = Parsers.newParseable(s)

  //  object ParserScope {
  //    def apply(parseableSource:String) = new ParserScope(parseableSource)
  //  }

  class ParserScope(s: String) extends Scope {
    val p = EDNParser()
    val values = parse(s)
  }
}
