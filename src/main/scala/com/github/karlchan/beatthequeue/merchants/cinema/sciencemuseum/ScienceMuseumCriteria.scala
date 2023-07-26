package com.github.karlchan.beatthequeue.merchants.cinema.sciencemuseum

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

final case class ScienceMuseumCriteria(
    override val id: String = UUID.randomUUID.toString,
    override val merchant: String = ScienceMuseum.Name,
    filmNames: Seq[String] = Seq.empty,
    startDate: Option[LocalDate] = None,
    endDate: Option[LocalDate] = None,
    startTime: Option[LocalTime] = None,
    endTime: Option[LocalTime] = None,
    daysOfWeek: Seq[DayOfWeek] = Seq.empty,
    productTypeIds: Seq[String] = Seq.empty
) extends Criteria[ScienceMuseum]:
  def matches(event: Event[ScienceMuseum]) =
    val ScienceMuseumEvent(_, name, time, productTypeId) =
      event.asInstanceOf[ScienceMuseumEvent]

    filmNames.any(name.containsIgnoreCase(_)) &&
    startDate.mapOrTrue(!_.isAfter(time.toLocalDate())) &&
    endDate.mapOrTrue(!_.isBefore(time.toLocalDate())) &&
    startTime.mapOrTrue(!_.isAfter(time.toLocalTime())) &&
    endTime.mapOrTrue(!_.isBefore(time.toLocalTime())) &&
    daysOfWeek.any(_ == time.getDayOfWeek()) &&
    productTypeIds.any(_ == productTypeId)

given Encoder[DayOfWeek] = dayOfWeekEncoder
given Decoder[DayOfWeek] = dayOfWeekDecoder
