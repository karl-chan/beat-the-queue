package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event

import java.time.LocalDateTime

case class CineworldCriteria(
    names: Option[Seq[String]],
    timeRange: Option[Seq[TimeRange]],
    venues: Option[Seq[String]],
    screenTypes: Option[Seq[String]]
) extends Criteria[Cineworld]

private case class TimeRange(
    startTime: Option[LocalDateTime],
    endTime: Option[LocalDateTime]
)
