package com.themillhousegroup.edn

import scala.reflect._
import scala.reflect.api._
import scala.reflect.runtime.universe._

case class Dummy(x:String, y:Int)

object EDNToProductConverter {


  def apply[T: TypeTag: ClassTag](map: Map[String, AnyRef]):T = {
//    typeOf[T].getDeclaredFields.map { field =>
//      val fieldName = field.getName
//      val fieldType = field.getType

//      if (fieldType.isInstanceOf[Option[_]]) {
//        matchOptionalField(fieldName, fieldType, map.get(fieldName))
//      } else {
 //       matchField(fieldName, fieldType, map.get(fieldName))
      //}
      val rm = runtimeMirror(typeOf[T].runtimeClass.getClassLoader)
      val classTest = typeOf[T].typeSymbol.asClass
      val classMirror = rm.reflectClass(classTest)
      val constructor = typeOf[T].decl(termNames.CONSTRUCTOR).asMethod
      val constructorMirror = classMirror.reflectConstructor(constructor)


    val constructorArgs = constructor.paramLists.flatten.map( (param: Symbol) => {
      val paramName = param.name.toString
      if(param.typeSignature <:< typeOf[Option[Any]])
        m.get(paramName)
      else
        m.get(paramName).getOrElse(throw new IllegalArgumentException("Map is missing required parameter named " + paramName))
    })

    constructorMirror(constructorArgs:_*)
  }

  private def matchOptionalField[F <: Option[Any]](fieldName:String, fieldType: Class[F], mapValue: Option[AnyRef]):Option[Any] = {
    mapValue.fold {
      None.asInstanceOf[Option[Any]]
    } { v =>
      // The case class declared it optional, so we'll populate a Some[T]
      Some(v.asInstanceOf[Any])
    }
  }

  private def matchField[F](fieldName:String, fieldType: Class[F], mapValue: Option[AnyRef]):String = {
    mapValue.fold[String] {
      // EDN does NOT contain a field with this keyword
      throw new IllegalArgumentException(s"Non-optional field '${fieldName}' was not found in the given EDN.")
    } { v =>
      // EDN contains a value for this keyword
      v.asInstanceOf[String]
    }
  }
}

class X {

  val d:Dummy = EDNToProductConverter(Map("x" -> "eks", "y" -> "wye"))
}
