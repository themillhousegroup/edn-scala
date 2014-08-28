package com.themillhousegroup.edn

import scala.reflect.runtime.universe._

object EDNToProductConverter {

  lazy val m = runtimeMirror(getClass.getClassLoader)
  lazy val optionType = typeOf[Option[_]].typeSymbol

  def apply[T <: Product: TypeTag](map: Map[String, AnyRef]): T = {
    buildCaseClass[T](map)
  }

  private[this] def buildCaseClass[T: TypeTag](map: Map[String, AnyRef]): T = {
    // rejectIfScoped(targetClass)

    val t = typeOf[T]

    val constructor = t.declarations.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get

    val constructorArgs = constructor.paramss.head

    val args = constructorArgs.map { field =>
      val fieldName = field.name.decoded
      println(s"${field.name} ${field.typeSignature}")

      if (isOption(field.typeSignature)) {
        matchOptionalField(fieldName, map.get(fieldName))
      } else {
        matchRequiredField(fieldName, map.get(fieldName))
      }.asInstanceOf[Object]
    }.toArray

    val c = m.runtimeClass(t.typeSymbol.asClass)
    c.getConstructors.head.newInstance(args: _*).asInstanceOf[T]
  }

  private[this] def rejectIfScoped(targetClass: Class[_]) = {
    if (targetClass.getDeclaredFields.exists(_.isSynthetic)) {
      throw new UnsupportedOperationException(
        s"Can't create an instance of ${targetClass.getName} - it's in the wrong scope")
    }
  }

  private[this] def isProduct(fieldType: Class[_]) = {
    fieldType.getInterfaces.exists(classOf[Product] == _)
  }

  private[this] def isOption(fieldType: Class[_]) = {
    fieldType.isAssignableFrom(classOf[Option[_]])
  }

  private[this] def isOption(ts: Type) = {
    ts.baseClasses.exists(_ == optionType)
  }

  private[this] def isInt(fieldType: Class[_]) = {
    fieldType.isAssignableFrom(classOf[Int])
  }

  private[this] def isJLong(fieldType: Class[_]) = {
    fieldType.isAssignableFrom(classOf[java.lang.Long])
  }

  private[this] def findOptionTarget[O: TypeTag] = {
    println("******:" + typeOf[Option[Int]])
    println("***: " + typeOf[O])
  }

  private def matchOptionalField[F: TypeTag](fieldName: String, mapValue: Option[AnyRef]): Option[F] = {
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

  private def matchRequiredField[F: TypeTag](fieldName: String, mapValue: Option[AnyRef]): F = {
    mapValue.fold[F] {
      // EDN does NOT contain a field with this keyword
      throw new IllegalArgumentException(s"Non-optional field '${fieldName}' was not found in the given EDN.")
    } { v =>
      val fieldType = typeOf[F]
      //      if (isProduct(fieldType)) {
      //        buildCaseClass(v.asInstanceOf[Map[String, AnyRef]])
      //      } else {
      // EDN-Java tends to favour java.lang.Long where a case class would use an Int;
      // make the conversion transparent:
      //        if (isInt(fieldType) && isJLong(v.getClass)) {
      //          v.asInstanceOf[Long].toInt.asInstanceOf[F]
      //        } else {
      v.asInstanceOf[F]
      //        }
      //      }
    }
  }
}
