package com.themillhousegroup.edn

import scala.reflect.runtime.universe._
import scala.collection.GenTraversableOnce

object EDNToProductConverter {
  val m = runtimeMirror(getClass.getClassLoader)

  lazy val productTrait = typeOf[Product].typeSymbol
  lazy val genTraversableOnceTrait = typeOf[GenTraversableOnce[_]].typeSymbol
  lazy val optionType = typeOf[Option[_]].typeSymbol
  lazy val intType = typeOf[Int].typeSymbol

  def apply[T <: Product: TypeTag](map: Map[String, AnyRef]): T = {
    buildCaseClass[T](typeOf[T], map)
  }

  private[this] def buildCaseClass[T: TypeTag](t: Type, map: Map[String, AnyRef]): T = {
    rejectIfScoped(t)

    val constructor = t.declarations.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get

    val constructorArgs = constructor.paramss.head

    val args = constructorArgs.map { field =>
      val fieldName = field.name.decoded
      val fieldType = field.typeSignature

      if (isOption(fieldType)) {
        matchOptionalField(fieldName, fieldType, map.get(fieldName))
      } else {
        matchRequiredField(fieldName, fieldType, map.get(fieldName))
      }.asInstanceOf[Object]
    }.toArray

    val c = m.runtimeClass(t.typeSymbol.asClass)
    c.getConstructors.head.newInstance(args: _*).asInstanceOf[T]
  }

  private[this] def rejectIfScoped(t: Type) = {
    val TypeRef(pre, sym, _) = t
    if (pre.toString.contains("this")) { // FIXME OMFG what? Gotta be a better detection mechanism than this...
      throw new UnsupportedOperationException(
        s"Can't create an instance of ${sym} - is it an inner class?")
    }
  }

  private[this] def hasClass(t: Type, desired: Symbol): Boolean = t.baseClasses.exists(_ == desired)

  private[this] def isCaseClass(t: Type) =
    t.baseClasses.exists {
      case cs: ClassSymbol => cs.isCaseClass
    }

  private[this] def isOption(fieldType: Class[_]) = {
    fieldType.isAssignableFrom(classOf[Option[_]])
  }

  private[this] def isOption(t: Type) = hasClass(t, optionType)

  private[this] def isInt(t: Type) = hasClass(t, intType)

  private[this] def isJLong(fieldType: Class[_]) = {
    fieldType.isAssignableFrom(classOf[java.lang.Long])
  }

  private[this] def findOptionTarget[O: TypeTag] = {
    println("******:" + typeOf[Option[Int]])
    println("***: " + typeOf[O])
  }

  private def matchOptionalField[F: TypeTag](fieldName: String, fieldType: Type, mapValue: Option[AnyRef]): Option[F] = {
    mapValue.fold {
      None.asInstanceOf[Option[F]]
    } { v =>
      // given that fieldType is an Option[X], find what X is...
      //val optionTargetType =
      //          println("Find ot of " + fieldType)
      //          findOptionTarget[Option[F]]
      //          val optionTargetType = fieldType
      //          if (isProduct(optionTargetType)) {
      //            println(s"Optional product: $fieldName $optionTargetType")
      //            Some(buildCaseClass(v.asInstanceOf[Map[String, AnyRef]], optionTargetType))
      //          } else {
      Some(v.asInstanceOf[F])
      //          }
    }
  }

  private def matchRequiredField[F](fieldName: String, fieldType: Type, mapValue: Option[AnyRef]): F = {
    mapValue.fold[F] {
      // EDN does NOT contain a field with this keyword
      throw new IllegalArgumentException(s"Non-optional field '${fieldName}' was not found in the given EDN.")
    } { v =>
      if (isCaseClass(fieldType)) {
        buildCaseClass(fieldType, v.asInstanceOf[Map[String, AnyRef]])
      } else {
        // EDN-Java tends to favour java.lang.Long where a case class would use an Int;
        // make the conversion transparent:
        if (isInt(fieldType) && isJLong(v.getClass)) {
          v.asInstanceOf[Long].toInt.asInstanceOf[F]
        } else {
          println(s"Matching ${fieldType.baseClasses} to ${v.getClass}")
          //      if (fieldType =:= )

          val i = v.asInstanceOf[F]
          println(s"Returning ${v.getClass} as ${i.getClass} that needs to be a $fieldType")
          i
        }
      }
    }
  }
}
