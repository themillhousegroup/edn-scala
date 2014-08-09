package com.themillhousegroup.edn

import java.nio.CharBuffer
import scala.io.Source

object ReadableSource {
  implicit def source2Parseable(s:Source):Readable = {
    new ReadableSource(s)
  }
}
/** An adapter to allow a Scala Source to be a
  * java.lang.Readable so that it can be read
  * by Parsers.newParseable(Readable r)
  */
class ReadableSource(src:Source) extends Readable {
  def read(cb: CharBuffer): Int = {
    val requiredLength = cb.length


  }
}