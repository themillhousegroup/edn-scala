package com.themillhousegroup.edn

import us.bpsm.edn.parser.Parseable
import com.typesafe.config._
import scala.io.Source

/**
 * Provides an EDN-compatible alternative to a Typesafe ConfigFactory,
 * allowing EDN files to be treated as sources of a Typesafe Config object.
 *
 * The ConfigFactory itself is declared final, so provided here is a subset
 * of the myriad ways to obtain a Config object, plus a couple of additions:
 * - parseSource() if you have a scala.io.Source
 * - parseParseable() if you have a us.bpsm.edn.parser.Parseable
 *
 * Please contribute a pull-request if you add additional factory methods here,
 * it would be much appreciated!
 */
object EDNConfigFactory {

  val description = "EDN Configuration"

  val defaultResourceBaseName = "application"
  val defaultResourceExtension = "edn"

  /** As per Typesafe Config convention, expects to find "application[.edn]" as a classpath resource */
  def load(): Config = load(defaultResourceBaseName)

  def load(resourceBasename: String): Config = {
    val src = scala.io.Source.fromURL(getClass.getResource(s"/$resourceBasename.$defaultResourceExtension"))
    parseSource(src)
  }

  def parseFile(file: java.io.File): Config = {
    parseSource(Source.fromFile(file))
  }

  def parseParseable(parseable: Parseable): Config = {
    val map = EDNParser().asMap(parseable)
    generate(map, parseable.toString)
  }

  def parseSource(source: scala.io.Source): Config = {
    val p = ParseableSource.source2Parseable(source)
    parseParseable(p)
  }

  private[this] def generate(map: Map[String, Any], sourceDescription: String): Config = {
    val jMap = buildRecursiveJMap(map)
    val configObject = ConfigValueFactory.fromMap(jMap, s"$description from $sourceDescription")
    configObject.toConfig
  }

  private[this] def buildRecursiveJMap(scalaMap: Map[String, Any]): java.util.Map[String, Any] = {
    import scala.collection.JavaConverters._

    scalaMap.map {
      case (k, v: Map[String, Any]) => k -> buildRecursiveJMap(v)
      case (k, v: us.bpsm.edn.Keyword) => k -> v.getName // Avoid leading-colon problems
      case (k, v) => k -> v

    }.toMap.asJava
  }

}
