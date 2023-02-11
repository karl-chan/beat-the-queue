package com.github.karlchan.beatthequeue.merchants.cinema.thecinema

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.util.any
import com.github.karlchan.beatthequeue.util.containsIgnoreCase
import com.github.karlchan.beatthequeue.util.dayOfWeekDecoder
import com.github.karlchan.beatthequeue.util.dayOfWeekEncoder
import com.github.karlchan.beatthequeue.util.mapOrTrue
import io.circe.Decoder
import io.circe.Encoder

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

final case class TheCinemaCriteria(
    override val id: String = UUID.randomUUID.toString,
    override val merchant: String = TheCinema.Name,
    filmNames: Seq[String] = Seq.empty,
    startDate: Option[LocalDate] = None,
    endDate: Option[LocalDate] = None,
    startTime: Option[LocalTime] = None,
    endTime: Option[LocalTime] = None,
    daysOfWeek: Seq[DayOfWeek] = Seq.empty,
    venues: Seq[String] = Seq.empty,
    screenTypes: Seq[String] = Seq.empty
) extends Criteria[TheCinema]:
  def matches(event: Event[TheCinema]) =
    val TheCinemaEvent(_, name, time, venue, screenType) =
      event.asInstanceOf[TheCinemaEvent]

    filmNames.any(name.containsIgnoreCase(_)) &&
    startDate.mapOrTrue(!_.isAfter(time.toLocalDate())) &&
    endDate.mapOrTrue(!_.isBefore(time.toLocalDate())) &&
    startTime.mapOrTrue(!_.isAfter(time.toLocalTime())) &&
    endTime.mapOrTrue(!_.isBefore(time.toLocalTime())) &&
    daysOfWeek.any(_ == time.getDayOfWeek()) &&
    venues.any(_ == venue) &&
    screenTypes.any(_ == screenType)

given Encoder[DayOfWeek] = dayOfWeekEncoder
given Decoder[DayOfWeek] = dayOfWeekDecoder
