package com.themillhousegroup.edn.test

import org.specs2.mutable.Specification

trait StreamChecking {

  this: Specification =>

  def valueStreamMustHave[T](stream:Stream[T], items:T*) = {
    val s = stream.toSeq
    s must haveSize(items.size)
    s must containTheSameElementsAs(items)
  }

  def keyStreamMustHave(stream:Stream[(String, AnyRef)], items:String*) = {
    val s = stream.toSeq
    s must haveSize(items.size)
    s.map { case (k, v) => k } must containTheSameElementsAs(items)
  }

  def keyValueStreamMustHave(stream:Stream[(String, AnyRef)], items:(String, _)*) = {
    val s = stream.toSeq
    s must haveSize(items.size)
    s must containTheSameElementsAs(items)
  }
}
