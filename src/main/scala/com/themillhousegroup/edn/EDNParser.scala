package com.themillhousegroup.edn

import us.bpsm.edn.parser._
import us.bpsm.edn.parser.Parsers._
import us.bpsm.edn.Keyword._
import us.bpsm.edn.parser.Parser.Config
import us.bpsm.edn.Keyword
import org.slf4j.LoggerFactory

object EDNParser {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  def apply() = {
    new ScalaEDNParser(defaultConfiguration)
  }

  def apply(config: Config) = {
    new ScalaEDNParser(config)
  }

  implicit def str2keyword(s: String): Keyword = newKeyword(s)
  implicit def sym2keyword(s: Symbol): Keyword = newKeyword(s.name)

  implicit def keyword2str(k: Keyword): String = k.getName

  /**
   * EDN keys can have dashes and ?s in them (which are illegal for Scala/Java field names)
   * If the map is going to end up needing to be Scala-legal, instances of these can be
   * converted here into camelCase as Gosling intended :-)
   */
  def ensureLegalKeys(map: Map[String, Any]) = {
    import com.google.common.base.CaseFormat._
    map.map {
      case (k, v) =>
        val removedQuestionMarks = removeIllegalCharacters(k)
        val fixedDashes = LOWER_HYPHEN.to(LOWER_CAMEL, removedQuestionMarks)
        logger.trace(s"Checking/converting $k to $fixedDashes")
        fixedDashes -> v
    }.toMap
  }

  def removeIllegalCharacters(s: String) = s.replaceAll("[?]", "")
}

class ScalaEDNParser(config: Config) {
  val javaParser = Parsers.newParser(config)

  private def isFinished(o: AnyRef) = Parser.END_OF_INPUT.equals(o)

  private def immutableMap[K, V](m: Traversable[(K, V)]) = {
    Map[K, V]() ++ m
  }

  /**
   * A simple wrapper around the basic Java interface,
   * this method keeps returning Some(T) until all values are exhausted
   * @since 1.0.0
   */
  def nextValue[T](pbr: Parseable): Option[T] = {
    val v = javaParser.nextValue(pbr)

    if (isFinished(v)) {
      None
    } else {
      Option(v).asInstanceOf[Option[T]]
    }
  }

  /**
   * We recurse here wherever there is a chance
   * that a nested object could be another
   * collection that needs processing.
   * The termination case is when we find a simple object.
   */
  private def handleCollections(a: AnyRef)(mapHandler: Map[AnyRef, AnyRef] => Traversable[(String, AnyRef)])(traversableHandler: Traversable[AnyRef] => Traversable[AnyRef]): AnyRef = {

    import scala.collection.JavaConverters._
    a match {
      case m: java.util.Map[AnyRef, AnyRef] => { mapHandler(immutableMap(m.asScala)) }
      case m: java.util.List[AnyRef] => { traversableHandler(m.asScala.toList) }
      case m: java.util.Set[AnyRef] => { traversableHandler(m.asScala.toSet) }
      case o => o
    }
  }

  private def streamCollection(a: AnyRef): AnyRef = {

    handleCollections(a) {
      _.toStream.map {
        case (k: Keyword, v) => k.getName -> streamCollection(v)
        case (s: String, v) => s -> streamCollection(v)
      }
    }(_.toStream.map(convertCollection))
  }

  private def convertCollection(a: AnyRef): AnyRef = {

    handleCollections(a) {
      _.map {
        case (k: Keyword, v) => k.getName -> convertCollection(v)
        case (s: String, v) => s -> convertCollection(v)
      }
    }(_.map(convertCollection))
  }

  private def processParseable(pbr: Parseable)(valueMapper: AnyRef => AnyRef): Iterator[(String, AnyRef)] = {
    Stream.continually(
      javaParser.nextValue(pbr))
      .takeWhile(!isFinished(_)).sliding(2, 2).map { pair =>
        pair.head match {
          case k: Keyword => k.getName -> valueMapper(pair(1))
          case s: String => s -> valueMapper(pair(1))
          case _ => "" -> valueMapper(pair.head)
        }
      }
  }

  /**
   * Treat an EDN block as if it was a Stream of key-value tuples.
   * This may be suitable if you are dealing with an extremely large
   * Parseable instance and are worried about memory usage.
   * Nested collections will appear as a (String, Stream) tuple
   * within the parent stream.
   * If the entire EDN block is contained within {}, then
   * it will be treated as a Stream with one tuple, "" -> (the content)
   * @since 1.0.0
   */
  def asStream(pbr: Parseable): Stream[(String, AnyRef)] = {
    processParseable(pbr)(streamCollection).toStream
  }

  /**
   * Treat an EDN block as if it was an immutable Map.
   *
   * Simple key-value pairs will have appropriate value types.
   * Otherwise, there can be nested Map[String, AnyRef],
   * Set[AnyRef] or Seq[AnyRef] collections nested inside.
   *
   * If the entire EDN block is contained within {}, then
   * this "single-entry Map" will be dereferenced for convenience;
   * so for example:
   * { :x 1 :y 2 :z 3 } will result in a Map of size 3, rather
   * than a Map with one entry of "" -> (Map of size 3)
   * @since 1.0.0
   */
  def asMap(pbr: Parseable): Map[String, AnyRef] = {
    val m = immutableMap(processParseable(pbr)(convertCollection).toTraversable)

    // Special case for the "root" map (if it exists)
    if (m.size == 1 && (m.forall {
      case (s, a) =>
        s.isEmpty && a.isInstanceOf[Map[String, AnyRef]]
    })) {
      m.head._2.asInstanceOf[Map[String, AnyRef]]
    } else m
  }

  import scala.reflect.runtime.universe._
  import scala.reflect._
  /**
   * Reduces the amount of casting required when treating EDN files
   * as a Map[String, AnyRef]. This function will attempt to coerce
   * the contents of the provided Parseable into an instance of the
   * given case class (all case classes extend Product, hence the signature).
   *
   * Fields in the EDN not found in the target class will be ignored.
   * Fields in the target class MUST be present in the EDN, unless they
   * are Option types, in which case they will be set to None.
   *
   * Case classes of arbitrary complexity (e.g. with lists, sets, maps,
   * options, and other case classes nested inside) are supported.
   *
   * @since 2.0.0
   */
  def readInto[T <: Product: TypeTag](pbr: Parseable): scala.util.Try[T] = {
    scala.util.Try(EDNToProductConverter(asMap(pbr)))
  }
}
