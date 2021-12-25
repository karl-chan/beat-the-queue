package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.util.mapTruthy

import java.time.LocalDateTime
import java.util.UUID

final case class CineworldCriteria(
    override val id: String = UUID.randomUUID.toString,
    override val merchant: String = Cineworld.Name,
    filmNames: Option[Seq[String]] = None,
    startTime: Option[LocalDateTime] = None,
    endTime: Option[LocalDateTime] = None,
    venues: Option[Seq[String]] = None,
    screenTypes: Option[Seq[String]] = None
) extends Criteria[Cineworld]:
  def matches(event: Event[Cineworld]) =
    val CineworldEvent(name, time, venue, screenType) =
      event.asInstanceOf[CineworldEvent]

    filmNames.mapTruthy(_.contains(name)) &&
    startTime.mapTruthy(!_.isAfter(time)) &&
    endTime.mapTruthy(!_.isBefore(time)) &&
    venues.mapTruthy(_.contains(venue)) &&
    screenTypes.mapTruthy(_.contains(screenType))
