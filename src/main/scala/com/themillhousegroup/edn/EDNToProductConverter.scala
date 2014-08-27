package com.themillhousegroup.edn

import scala.reflect._
import scala.reflect.api._
import scala.reflect.runtime.universe._

object EDNToProductConverter {


  def apply[T <: Product](map: Map[String, AnyRef],  targetClass:Class[T]):T = {

    val dfs = targetClass.getDeclaredFields
    //println(s"DFs: ${dfs.mkString}")

    val args = targetClass.getDeclaredFields.map { field =>
      val fieldName = field.getName
      val fieldType = field.getType

      if (isOption(fieldType)) {
        matchOptionalField(fieldName, fieldType, map.get(fieldName))
      } else {
        matchRequiredField(fieldName, fieldType, map.get(fieldName))
      }
    }

    println(s"args: ${args.mkString}")
    targetClass.getConstructors.head.newInstance(args:_*).asInstanceOf[T]
  }

  private[this] def isOption(fieldType:Class[_]) = {
    fieldType.isAssignableFrom(classOf[Option[_]])
  }

  private def matchOptionalField[F](fieldName:String, fieldType: Class[F], mapValue: Option[AnyRef]):Option[F] = {
    mapValue.fold {
      None.asInstanceOf[Option[F]]
    } { v =>
      // The case class declared it optional, so we'll populate a Some[T]
      Some(v.asInstanceOf[F])
    }
  }

  private def matchRequiredField[F](fieldName:String, fieldType: Class[F], mapValue: Option[AnyRef]):String = {
    mapValue.fold[String] {
      // EDN does NOT contain a field with this keyword
      throw new IllegalArgumentException(s"Non-optional field '${fieldName}' was not found in the given EDN.")
    } { v =>
      // EDN contains a value for this keyword
      v.asInstanceOf[String]
    }
  }
}
