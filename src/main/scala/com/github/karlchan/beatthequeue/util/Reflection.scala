package com.github.karlchan.beatthequeue.util

import scala.quoted._

object Reflection:
  def extractFields[T](obj: T): Seq[Field] =
    obj.getClass.getDeclaredFields
      .map(field =>
        field.setAccessible(true)
        Field(
          name = field.getName,
          descriptor = field.getType.descriptorString,
          value = field.get(obj)
        )
      )
      .toSeq

  case class Field(name: String, descriptor: String, value: Any)
