package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event

import java.time.LocalDateTime

final case class CineworldCriteria(
    names: Option[Seq[String]],
    startTime: Option[LocalDateTime],
    endTime: Option[LocalDateTime],
    venues: Option[Seq[String]],
    screenTypes: Option[Seq[String]]
) extends Criteria[Cineworld]:
  def matches(event: Event[Cineworld]) = ???
