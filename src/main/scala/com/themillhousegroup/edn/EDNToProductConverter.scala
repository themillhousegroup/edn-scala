package com.themillhousegroup.edn

import scala.reflect.runtime.universe._
import scala.collection.GenTraversableOnce
import org.slf4j.LoggerFactory

object EDNToProductConverter {

  lazy val logger = LoggerFactory.getLogger(getClass)
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

    val camelCasedMap = camelCaseKeys(map)

    val constructor = t.declarations.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get

    val constructorArgs = constructor.paramss.head

    val args = constructorArgs.map { field =>
      val fieldName = field.name.decoded
      val fieldType = field.typeSignature

      if (isOption(fieldType)) {
        matchOptionalField(fieldName, fieldType, camelCasedMap.get(fieldName))
      } else {
        matchRequiredField(fieldName, fieldType, camelCasedMap.get(fieldName))
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

  // EDN keys can have dashes and ?s in them (which are illegal for scala field names)
  // So instances of these are converted into camelCase as Gosling intended :-)
  private[this] def camelCaseKeys(map: Map[String, AnyRef]) = {
    import com.google.common.base.CaseFormat._
    map.map {
      case (k, v) =>
        val removedQuestionMarks = k.replaceAll("[?]", "")
        val fixedDashes = LOWER_HYPHEN.to(LOWER_CAMEL, removedQuestionMarks)
        logger.trace(s"Checking/converting $k to $fixedDashes")
        fixedDashes -> v
    }.toMap
  }

  private[this] def hasClass(t: Type, desired: Symbol): Boolean = t.baseClasses.exists(_ == desired)

  private[this] def isCaseClass(t: Type): Boolean =
    t.baseClasses.exists {
      case cs: ClassSymbol => cs.isCaseClass
    }

  private[this] def isOption(t: Type) = hasClass(t, optionType)

  private[this] def isInt(t: Type) = hasClass(t, intType)

  private[this] def isJLong(fieldType: Class[_]) = {
    fieldType.isAssignableFrom(classOf[java.lang.Long])
  }

  private[this] def findOptionTarget(t: Type) = {
    t.typeArgs.head
  }

  private def matchOptionalField[F](fieldName: String, fieldType: Type, mapValue: Option[AnyRef]): Option[F] = {
    mapValue.fold {
      None.asInstanceOf[Option[F]]
    } { v =>
      // given that fieldType is an Option[X], find what X is...
      val optionTargetType = findOptionTarget(fieldType)
      if (isCaseClass(optionTargetType)) {
        Some(buildCaseClass(optionTargetType, v.asInstanceOf[Map[String, AnyRef]]))
      } else {
        Some(v.asInstanceOf[F])
      }
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
          v.asInstanceOf[F]
        }
      }
    }
  }
}
