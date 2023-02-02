package com.github.karlchan.beatthequeue.merchants.haircut.lsb

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.util.any
import com.github.karlchan.beatthequeue.util.containsIgnoreCase
import com.github.karlchan.beatthequeue.util.mapOrTrue

import java.time.LocalDateTime
import java.util.UUID

final case class LSBCriteria(
    override val id: String = UUID.randomUUID.toString,
    override val merchant: String = LSB.Name,
    startTime: Option[LocalDateTime] = None,
    endTime: Option[LocalDateTime] = None,
    categories: Seq[String] = Seq.empty,
    services: Seq[String] = Seq.empty
) extends Criteria[LSB]:
  def matches(event: Event[LSB]) =
    val LSBEvent(_, service, time, category) =
      event.asInstanceOf[LSBEvent]

    startTime.mapOrTrue(!_.isAfter(time)) &&
    endTime.mapOrTrue(!_.isBefore(time)) &&
    categories.any(_ == category) &&
    services.any(_ == service)
