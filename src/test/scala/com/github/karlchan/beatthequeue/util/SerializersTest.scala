package com.github.karlchan.beatthequeue.util

import java.time.DayOfWeek

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.prop.TableDrivenPropertyChecks

final class SerializersTest
    extends AnyFlatSpec
    with TableDrivenPropertyChecks
    with should.Matchers:
  val daysOfWeeks =
    Table(
      ("dayOfWeek"),
      DayOfWeek.values()*
    )

  "dayOfWeek codecs" should "be lossless round trip" in {
    forAll(daysOfWeeks) { (dayOfWeek: DayOfWeek) =>
      val result = dayOfWeekDecoder
        .decodeJson(dayOfWeekEncoder.apply(dayOfWeek))
        .getOrElse(fail())
      result should be(dayOfWeek)
    }
  }
