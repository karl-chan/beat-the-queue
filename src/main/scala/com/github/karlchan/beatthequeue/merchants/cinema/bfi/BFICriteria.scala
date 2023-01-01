package com.github.karlchan.beatthequeue.merchants.cinema.bfi

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.util.any
import com.github.karlchan.beatthequeue.util.containsIgnoreCase
import com.github.karlchan.beatthequeue.util.mapOrTrue

import java.time.LocalDateTime
import java.util.UUID

final case class BFICriteria(
    override val id: String = UUID.randomUUID.toString,
    override val merchant: String = BFI.Name,
    filmNames: Seq[String] = Seq.empty,
    startTime: Option[LocalDateTime] = None,
    endTime: Option[LocalDateTime] = None,
    venues: Seq[String] = Seq.empty,
    screenTypes: Seq[String] = Seq.empty
) extends Criteria[BFI]:
  def matches(event: Event[BFI]) =
    val BFIEvent(_, name, time, venue, screenType) =
      event.asInstanceOf[BFIEvent]

    filmNames.any(name.containsIgnoreCase(_)) &&
    startTime.mapOrTrue(!_.isAfter(time)) &&
    endTime.mapOrTrue(!_.isBefore(time)) &&
    venues.any(_ == venue) &&
    screenTypes.forall(screenType.containsIgnoreCase(_))
