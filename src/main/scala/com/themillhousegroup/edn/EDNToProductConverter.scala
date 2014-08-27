package com.themillhousegroup.edn

object EDNToProductConverter {

  def apply[T <: Product](map: Map[String, AnyRef], targetClass: Class[T]): T = {
    buildCaseClass(map, targetClass)
  }

  private[this] def buildCaseClass[T](map: Map[String, AnyRef], targetClass: Class[T]): T = {
    rejectIfScoped(targetClass)
    val args = targetClass.getDeclaredFields.map { field =>
      val fieldName = field.getName
      val fieldType = field.getType

      if (isOption(fieldType)) {
        matchOptionalField(fieldName, fieldType, map.get(fieldName))
      } else {
        matchRequiredField(fieldName, fieldType, map.get(fieldName))
      }
    }
    targetClass.getConstructors.head.newInstance(args.asInstanceOf[Array[Object]]: _*).asInstanceOf[T]
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

  private[this] def isInt(fieldType: Class[_]) = {
    fieldType.isAssignableFrom(classOf[Int])
  }

  private[this] def isJLong(fieldType: Class[_]) = {
    fieldType.isAssignableFrom(classOf[java.lang.Long])
  }

  private def matchOptionalField[F](fieldName: String, fieldType: Class[F], mapValue: Option[AnyRef]): Option[F] = {
    mapValue.fold {
      None.asInstanceOf[Option[F]]
    } { v =>
      // The case class declared it optional, so we'll populate a Some[T]
      if (isProduct(fieldType)) {
        println(
          s"Optional product: $fieldName $fieldType")

        // TODO: This is NOT WORKING as the type of the Option is being erased
        Some(buildCaseClass(v.asInstanceOf[Map[String, AnyRef]], fieldType))
      } else {
        Some(v.asInstanceOf[F])
      }
    }
  }

  private def matchRequiredField[F](fieldName: String, fieldType: Class[F], mapValue: Option[AnyRef]): F = {
    mapValue.fold[F] {
      // EDN does NOT contain a field with this keyword
      throw new IllegalArgumentException(s"Non-optional field '${fieldName}' was not found in the given EDN.")
    } { v =>
      if (isProduct(fieldType)) {
        buildCaseClass(v.asInstanceOf[Map[String, AnyRef]], fieldType)
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
