package com.github.karlchan.beatthequeue.util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.time.LocalDate

final class ReflectionTest extends AnyFlatSpec with should.Matchers:
  "extractFields" should "work extract case class fields correctly" in {
    val fooImpl = FooImpl(1, "foo", LocalDate.now)
    Reflection.extractFields(fooImpl) should contain theSameElementsAs Seq(
      Reflection.Field(name = "intField", descriptor = "I", value = 1),
      Reflection.Field(
        name = "stringField",
        descriptor = "Ljava/lang/String;",
        value = "foo"
      ),
      Reflection.Field(
        name = "dateField",
        descriptor = "Ljava/time/LocalDate;",
        value = LocalDate.now
      )
    )
  }

  "extractFields" should "work for inherited hierarchy" in {
    val foo: Foo = FooImpl(1, "foo", LocalDate.now)
    Reflection.extractFields(foo) should contain theSameElementsAs Seq(
      Reflection.Field(name = "intField", descriptor = "I", value = 1),
      Reflection.Field(
        name = "stringField",
        descriptor = "Ljava/lang/String;",
        value = "foo"
      ),
      Reflection.Field(
        name = "dateField",
        descriptor = "Ljava/time/LocalDate;",
        value = LocalDate.now
      )
    )
  }

  "extractFields" should "work within collection of mixed objects" in {
    val fooImpl = FooImpl(1, "foo", LocalDate.now)
    val barImpl = BarImpl(1L)
    val collection: Seq[Any] = Seq(fooImpl, barImpl)

    val actual = collection.map(Reflection.extractFields)
    actual(0) should contain theSameElementsAs Seq(
      Reflection.Field(name = "intField", descriptor = "I", value = 1),
      Reflection.Field(
        name = "stringField",
        descriptor = "Ljava/lang/String;",
        value = "foo"
      ),
      Reflection.Field(
        name = "dateField",
        descriptor = "Ljava/time/LocalDate;",
        value = LocalDate.now
      )
    )
    actual(1) should contain only
      Reflection.Field(name = "longField", descriptor = "J", value = 1L)
  }

private trait Foo
private case class FooImpl(
    intField: Int,
    stringField: String,
    dateField: LocalDate
) extends Foo:
  def unrelatedMethod = ???

private case class BarImpl(
    longField: Long
)
