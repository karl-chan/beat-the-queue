package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.util.mapTruthy

import java.time.LocalDateTime
import java.util.UUID

final case class CineworldCriteria(
    override val id: String = UUID.randomUUID.toString,
    override val merchant: String = Cineworld.Name,
    filmNames: Seq[String] = Seq.empty,
    startTime: Option[LocalDateTime] = None,
    endTime: Option[LocalDateTime] = None,
    venues: Seq[String] = Seq.empty,
    screenTypes: Seq[String] = Seq.empty
) extends Criteria[Cineworld]:
  def matches(event: Event[Cineworld]) =
    val CineworldEvent(_, name, time, venue, screenType) =
      event.asInstanceOf[CineworldEvent]

    filmNames.mapTruthy(_.contains(name)) &&
    startTime.mapTruthy(!_.isAfter(time)) &&
    endTime.mapTruthy(!_.isBefore(time)) &&
    venues.mapTruthy(_.contains(venue)) &&
    screenTypes.mapTruthy(_.contains(screenType))
