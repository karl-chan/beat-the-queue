package com.github.karlchan.beatthequeue.merchants.food.toogoodtogo

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.util.any
import com.github.karlchan.beatthequeue.util.containsIgnoreCase
import com.github.karlchan.beatthequeue.util.dayOfWeekDecoder
import com.github.karlchan.beatthequeue.util.dayOfWeekEncoder
import com.github.karlchan.beatthequeue.util.mapOrTrue
import io.circe.Decoder
import io.circe.Encoder

final case class TooGoodToGoCriteria(
    override val id: String = UUID.randomUUID.toString,
    override val merchant: String = TooGoodToGo.Name,
    names: Seq[String] = Seq.empty,
    startDate: Option[LocalDate] = None,
    endDate: Option[LocalDate] = None,
    startTime: Option[LocalTime] = None,
    endTime: Option[LocalTime] = None,
    daysOfWeek: Seq[DayOfWeek] = Seq.empty
) extends Criteria[TooGoodToGo]:
  def matches(event: Event[TooGoodToGo]) =
    val TooGoodToGoEvent(_, name, time) =
      event.asInstanceOf[TooGoodToGoEvent]

    names.any(name.containsIgnoreCase(_)) &&
    startDate.mapOrTrue(!_.isAfter(time.toLocalDate())) &&
    endDate.mapOrTrue(!_.isBefore(time.toLocalDate())) &&
    startTime.mapOrTrue(!_.isAfter(time.toLocalTime())) &&
    endTime.mapOrTrue(!_.isBefore(time.toLocalTime())) &&
    daysOfWeek.any(_ == time.getDayOfWeek())

given Encoder[DayOfWeek] = dayOfWeekEncoder
given Decoder[DayOfWeek] = dayOfWeekDecoder
