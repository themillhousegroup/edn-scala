package com.themillhousegroup.edn

import org.specs2.mutable.Specification
import com.themillhousegroup.edn.ParseableSource._

case class HttpServerSettings(port: Int, contextPath: String, join: Boolean)
case class AppenderSettings(enabled: Boolean)
case class LoggingSettings(
  timestampPattern: Option[String],
  //appenders: Map[String, AppenderSettings],  // Not working yet
  sharedAppenderConfig: Map[String, Map[String, AnyRef]])
case class ConnectionSettings(keyspace_host: String, keyspace_port: String, keyspace_username: String)
case class IcarusSettings(models: String, pooling: String, connection: ConnectionSettings, hsm: String)

case class ConfigurationSettings(
  httpServer: HttpServerSettings,
  logging: LoggingSettings,
  icarus: IcarusSettings)

class ActualCaseClassParsingSpec extends Specification {

  "Parsing a real-life EDN file into a family of case classes" should {

    val result: ConfigurationSettings = EDNParser().readInto[ConfigurationSettings]("/config.edn").get

    "handle fields with dashes properly" in {
      result.httpServer.contextPath must beEqualTo("/end_point")
    }

    "handle fields with question marks properly" in {
      result.httpServer.join must beFalse
    }

    "handle maps with nested maps properly" in {

      result.logging.sharedAppenderConfig must not beEmpty

      val rollingConfig = result.logging.sharedAppenderConfig("rolling")
      rollingConfig must not beEmpty

      // Note some of the quirks here because this is a map that has not
      // had the conversion into java-compatible key names

      rollingConfig("enabled?").asInstanceOf[Boolean] must beTrue
      rollingConfig("path") must beEqualTo("logs/shared-appender.log")
      rollingConfig("pattern") must beEqualTo(us.bpsm.edn.Keyword.newKeyword("daily"))
    }

    "handle fields with underscores properly" in {

      result.icarus.models must beEqualTo("com.example.domain.model")
      result.icarus.connection.keyspace_host must beEqualTo("localhost")
    }
  }
}
