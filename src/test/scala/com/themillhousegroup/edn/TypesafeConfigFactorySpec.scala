package com.themillhousegroup.edn

import org.specs2.mutable.Specification

class TypesafeConfigFactorySpec extends Specification {

  "Using the EDNConfigFactory" should {

    "Allow a String from an EDN file (from a Source) to be accessed as a typesafe.Config object" in {

      val src = scala.io.Source.fromURL(getClass.getResource("/simple-config.edn"))

      val cfg = EDNConfigFactory.parseSource(src)

      cfg must not beNull

      val simpleKeyResult = cfg.getString("simple-key")

      simpleKeyResult must beEqualTo("simple-value")

    }

    "Allow a nested Config from an EDN file (from a Source) to be accessed as a typesafe.Config object" in {

      val p = ParseableSource.filename2Parseable("/simple-config.edn")

      val cfg = EDNConfigFactory.parseParseable(p)

      cfg must not beNull

      val httpServerCfg = cfg.getConfig("http-server")

      httpServerCfg must not beNull
    }

    "Allow a file called application.edn to be automatically loaded as a typesafe.Config object" in {

      val cfg = EDNConfigFactory.load

      cfg must not beNull

      val httpServerCfg = cfg.getConfig("http-server")

      httpServerCfg must not beNull
    }

    "Allow a reasonably-complicated config to be automatically loaded by basename as a typesafe.Config object" in {

      val cfg = EDNConfigFactory.load("config")

      cfg must not beNull

      val httpServerCfg = cfg.getConfig("http-server")

      httpServerCfg must not beNull
    }
  }
}
