package com.themillhousegroup.edn.test

object CaseClassFixtures {

  // Basic case classes:
  case class AllStrings(bish: String, bash: String, bosh: String)
  case class OptionalStrings(bish: String, bash: Option[String], bosh: String)
  case class AllLongs(bash: Option[Long], bosh: Long)
  case class IntsNotLongs(bash: Option[Int], bosh: Int)
  case class MixedBunch(bish: String, bash: Option[Int], bosh: Int)

  // Basic case classes with collections as members:
  case class BasicWithList(bish: String, bash: List[String], bosh: Int)
  case class BasicWithSet(bish: String, bash: Set[String], bosh: Int)
  case class BasicWithMap(bish: String, bash: Map[String, Int], bosh: Int)

  // Case classes with another level of case classes within
  case class NestedJustOnce(contents: AllStrings)
  case class NestedWithFields(contents: AllStrings, a: Int, b: Int)
  case class NestedOptionally(contents: Option[AllStrings])

  // Multiply-nested case class
  case class StringsAllTheWayDown(first: AllStrings, second: Option[AllStrings])
  case class ThreeLevelsDeep(x: Int, y: Int, nest: Option[StringsAllTheWayDown])

}
