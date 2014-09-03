package com.themillhousegroup.edn

import scala.io.Source
import scala.io.Source._
import us.bpsm.edn.parser.Parseable
import java.io.FileNotFoundException

object ParseableSource {
  implicit def source2Parseable(s: Source): Parseable = {
    new ParseableSource(s)
  }

  implicit def filename2Parseable(filename: String) = {
    val url = getClass.getResource(filename)

    if (url == null) throw new FileNotFoundException(filename)

    val src = fromURL(url)

    source2Parseable(src)
  }
}
/** An adapter to allow a Scala Source to be a Parseable */
class ParseableSource(src: Source) extends Parseable {

  var unreadBuffer: Option[Int] = None

  def read(): Int = {

    unreadBuffer.fold {
      if (src.hasNext) {
        src.next().toInt
      } else {
        Parseable.END_OF_INPUT
      }
    } { unread =>
      unreadBuffer = None
      unread
    }
  }

  def close = src.close

  def unread(ch: Int) = {
    if (unreadBuffer.isDefined) {
      throw new UnsupportedOperationException("Can't unread after unread.")
    }
    unreadBuffer = Some(ch)
  }

  override def toString = {
    s"Parseable [${src.descr}]"
  }
}