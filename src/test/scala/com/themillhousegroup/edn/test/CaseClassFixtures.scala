package com.themillhousegroup.edn.test

object CaseClassFixtures {
  case class AllStrings(bish:String, bash:String, bosh:String)
  case class OptionalStrings(bish:String, bash:Option[String], bosh:String)
  case class AllLongs(bash:Option[Long], bosh:Long)
  case class IntsNotLongs(bash:Option[Int], bosh:Int)
  case class MixedBunch(bish:String, bash:Option[Int], bosh:Int)
}
