package com.themillhousegroup.edn

import us.bpsm.edn.parser._
import us.bpsm.edn.parser.Parsers._
import us.bpsm.edn.Keyword._
import us.bpsm.edn.parser.Parser.Config
import us.bpsm.edn.Keyword
import scala.collection.IterableLike

object EDNParser {

  def apply() = {
    new ScalaEDNParser(defaultConfiguration)
  }

  def apply(config:Config) = {
    new ScalaEDNParser(config)
  }

  implicit def str2keyword(s:String):Keyword = newKeyword(s)
  implicit def sym2keyword(s:Symbol):Keyword = newKeyword(s.name)

  implicit def keyword2str(k:Keyword):String = k.getName
}

class ScalaEDNParser(config:Config) {
  val javaParser = Parsers.newParser(config)

  private def isFinished(o:AnyRef) = Parser.END_OF_INPUT.equals(o)

  private def immutableMap[K, V](m:Traversable[(K,V)]) = {
    Map[K, V]() ++ m
  }

  /** A simple wrapper around the basic Java interface,
    * this method keeps returning Some(T) until all values are exhausted
    */
  def nextValue[T](pbr: Parseable):Option[T] = {
    val v = javaParser.nextValue(pbr)

    if (isFinished(v)) {
      None
    } else {
      Option(v).asInstanceOf[Option[T]]
    }
  }

  /** We recurse here wherever there is a chance
    * that a nested object could be another
    * collection that needs processing.
    * The termination case is when we find a simple object.
    */
  private def handleCollections(a:AnyRef)
                               (mapHandler:Map[AnyRef, AnyRef]=>Traversable[(String, AnyRef)])
                               (traversableHandler:Traversable[AnyRef]=>Traversable[AnyRef]):AnyRef = {

    import scala.collection.JavaConverters._
    a match {
      case m:java.util.Map[AnyRef,AnyRef] => { mapHandler(immutableMap(m.asScala)) }
      case m:java.util.List[AnyRef] => { traversableHandler(m.asScala) }
      case m:java.util.Set[AnyRef] => { traversableHandler(m.asScala)}
      case o => o
    }
  }


  private def streamCollection(a:AnyRef):AnyRef = {

    handleCollections(a){ _.toStream.map {
      case (k:Keyword, v) => k.getName -> streamCollection(v)
      case (s:String, v) => s -> streamCollection(v)
    }}( _.toStream.map(convertCollection) )
  }

  private def convertCollection(a:AnyRef):AnyRef = {

    handleCollections(a){ _.map {
        case (k:Keyword, v) => k.getName -> convertCollection(v)
        case (s:String, v) => s -> convertCollection(v)
      }}( _.map(convertCollection) )
  }

  private def processParseable(pbr:Parseable)(valueMapper:AnyRef=>AnyRef):Iterator[(String, AnyRef)] = {
    Stream.continually(
      javaParser.nextValue(pbr))
      .takeWhile (!isFinished(_)).sliding(2,2).map { pair =>
        pair.head match {
          case k:Keyword => k.getName -> valueMapper(pair(1))
          case s:String => s -> valueMapper(pair(1))
          case _ => "" -> valueMapper(pair.head)
        }
    }
  }

  /** Treat an EDN block as if it was a Stream of key-value tuples.
    * This may be suitable if you are dealing with an extremely large
    * Parseable instance and are worried about memory usage.
    * Nested collections will appear as a (String, Stream) tuple
    * within the parent stream.
    * If the entire EDN block is contained within {}, then
    * it will be treated as a Stream with one tuple, "" -> (the content)
    */
  def asStream(pbr: Parseable):Stream[(String, AnyRef)] = {
     processParseable(pbr)(streamCollection).toStream
  }

  /** Treat an EDN block as if it was an immutable Map.
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
    */
  def asMap(pbr: Parseable):Map[String, AnyRef] = {
    val m = immutableMap(processParseable(pbr)(convertCollection).toTraversable)

    // Special case for the "root" map (if it exists)
    if (m.size == 1 && (m.forall { case (s, a) =>
      s.isEmpty && a.isInstanceOf[Map[String, AnyRef]]} )) {
      m.head._2.asInstanceOf[Map[String, AnyRef]]
    } else m
  }
}
