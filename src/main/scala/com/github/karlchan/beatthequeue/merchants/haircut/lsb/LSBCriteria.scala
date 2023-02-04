package com.github.karlchan.beatthequeue.merchants.haircut.lsb

import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.util.any
import com.github.karlchan.beatthequeue.util.containsIgnoreCase
import com.github.karlchan.beatthequeue.util.mapOrTrue
import io.circe.Decoder
import io.circe.Encoder

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import scala.util.Try
final case class LSBCriteria(
    override val id: String = UUID.randomUUID.toString,
    override val merchant: String = LSB.Name,
    startDate: Option[LocalDate] = None,
    endDate: Option[LocalDate] = None,
    startTime: Option[LocalTime] = None,
    endTime: Option[LocalTime] = None,
    daysOfWeek: Seq[DayOfWeek] = Seq.empty,
    categories: Seq[String] = Seq.empty,
    services: Seq[String] = Seq.empty
) extends Criteria[LSB]:
  def matches(event: Event[LSB]) =
    val LSBEvent(_, service, time, category) =
      event.asInstanceOf[LSBEvent]

    startDate.mapOrTrue(!_.isAfter(time.toLocalDate())) &&
    endDate.mapOrTrue(!_.isBefore(time.toLocalDate())) &&
    startTime.mapOrTrue(!_.isAfter(time.toLocalTime())) &&
    endTime.mapOrTrue(!_.isBefore(time.toLocalTime())) &&
    daysOfWeek.any(_ == time.getDayOfWeek()) &&
    categories.any(_ == category) &&
    services.any(_ == service)

given [M]: Encoder[DayOfWeek] =
  Encoder.encodeInt.contramap[DayOfWeek](_.getValue() % 7)

given Decoder[DayOfWeek] = Decoder.decodeInt.emapTry { i =>
  Try(i match
    case 0 => DayOfWeek.SUNDAY
    case _ => DayOfWeek.of(i)
  )
}
