package com.github.karlchan.beatthequeue.merchants.cinema.vue

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

final case class VueCriteria(
    override val id: String = UUID.randomUUID.toString,
    override val merchant: String = Vue.Name,
    filmNames: Seq[String] = Seq.empty,
    startDate: Option[LocalDate] = None,
    endDate: Option[LocalDate] = None,
    startTime: Option[LocalTime] = None,
    endTime: Option[LocalTime] = None,
    daysOfWeek: Seq[DayOfWeek] = Seq.empty,
    venues: Seq[String] = Seq.empty,
    screenTypes: Seq[String] = Seq.empty
) extends Criteria[Vue]:
  def matches(event: Event[Vue]) =
    val VueEvent(_, name, time, venue, eventScreenTypes) =
      event.asInstanceOf[VueEvent]

    filmNames.any(name.containsIgnoreCase(_)) &&
    startDate.mapOrTrue(!_.isAfter(time.toLocalDate())) &&
    endDate.mapOrTrue(!_.isBefore(time.toLocalDate())) &&
    startTime.mapOrTrue(!_.isAfter(time.toLocalTime())) &&
    endTime.mapOrTrue(!_.isBefore(time.toLocalTime())) &&
    daysOfWeek.any(_ == time.getDayOfWeek()) &&
    venues.any(_ == venue) &&
    (screenTypes.isEmpty || !screenTypes.toSet
      .intersect(eventScreenTypes.toSet)
      .isEmpty)

given Encoder[DayOfWeek] = dayOfWeekEncoder
given Decoder[DayOfWeek] = dayOfWeekDecoder
