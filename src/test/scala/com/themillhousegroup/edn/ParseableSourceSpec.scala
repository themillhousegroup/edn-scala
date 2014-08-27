package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import com.themillhousegroup.edn.test.{ StreamChecking, EDNParsing }
import java.io.FileNotFoundException

class ParseableSourceSpec extends Specification with EDNParsing with StreamChecking {

  "The ParseableSource object" should {

    "Allow me to supply a scala.io.Source as the EDN source" in {

      val p = EDNParser()

      val src = scala.io.Source.fromURL(
        getClass.getResource("/config.edn"))

      import com.themillhousegroup.edn.ParseableSource._

      val stream = p.asStream(src)

      val s = stream.toSeq

      s must haveSize(1)

      val innerStream = s.head._2.asInstanceOf[Stream[(String, AnyRef)]]

      keyStreamMustHave(
        innerStream,
        "http-server", "logging", "icarus", "environments", "private-file")

    }

    "Throw a FileNotFound if I ask for the impossible" in {
      import com.themillhousegroup.edn.ParseableSource._

      val p = EDNParser()

      p.asStream("nonexistent.edn") must throwA[FileNotFoundException]

    }

    "Allow me to supply a simple filename as the EDN source" in {
      import com.themillhousegroup.edn.ParseableSource._
      val p = EDNParser()

      val stream = p.asStream("/config.edn")

      val s = stream.toSeq

      s must haveSize(1)

      val innerStream = s.head._2.asInstanceOf[Stream[(String, AnyRef)]]

      keyStreamMustHave(
        innerStream,
        "http-server", "logging", "icarus", "environments", "private-file")
    }
  }
}
