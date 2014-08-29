package com.themillhousegroup.edn.test

import us.bpsm.edn.parser.Parsers
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import scala.util.Try
import scala.reflect.runtime.universe._

import com.themillhousegroup.edn.EDNParser

trait EDNParsing {
  this: Specification =>

  def parse(s: String) = Parsers.newParseable(s)

  class ParserScope(s: String) extends Scope {
    val p = EDNParser()
    val values = parse(s)
  }

  class CaseClassScope(s: String) extends ParserScope(s) {

    def readInto[T <: Product: TypeTag]: Try[T] = {
      p.readInto(values)
    }

    def readIntoResult[T <: Product: TypeTag]: T = {
      readInto[T].get
    }

  }

}
