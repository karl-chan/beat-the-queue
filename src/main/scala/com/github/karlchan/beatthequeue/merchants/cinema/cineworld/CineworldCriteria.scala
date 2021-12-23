package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event

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
  def matches(event: Event[Cineworld]) = ???
