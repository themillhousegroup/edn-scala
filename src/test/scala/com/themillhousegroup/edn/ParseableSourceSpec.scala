package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import us.bpsm.edn.parser.{Parsers, Parseable}
import com.themillhousegroup.edn.EDNParser
import com.themillhousegroup.edn.test.EDNParsing

class ParseableSourceSpec extends Specification with EDNParsing {




  "Using the Streaming API with a real EDN file" should {

    "Allow me to supply a scala.io.Source as the EDN source" in {

      val p = EDNParser()

      val src = scala.io.Source.fromURL(
          getClass.getResource("/config.edn"))

      import com.themillhousegroup.edn.ParseableSource._

      val stream = p.asStream(src)

      val s = stream.toSeq

      s must haveSize(10)
    }
  }
}
