package com.github.karlchan.beatthequeue.merchants.attraction.horizon22

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.EventFinder
import com.github.karlchan.beatthequeue.util.Http
import com.github.karlchan.beatthequeue.util.given_HttpConnection
import com.github.karlchan.beatthequeue.util.shortFormat
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client3._

final class Horizon22Crawler extends EventFinder[Horizon22]:
  private val http = Http()

  override def run(): Stream[IO, Horizon22Event] =
    for {
      calendar <- Stream.eval(getCalendar())
      availableDates = calendar
        .filter(_._2 == CalendarResponse.Status.Available)
        .keys
        .toSeq
        .sorted
      date <- Stream.emits(availableDates)
      session <- Stream.evals(getSessions(date))
      dateTime =
        LocalDateTime.ofInstant(
          Instant.from(
            DateTimeFormatter.ISO_INSTANT.parse(session.start_datetime)
          ),
          ZoneOffset.UTC
        )
    } yield Horizon22Event(
      time = dateTime
    )

  private[horizon22] def getCalendar()
      : IO[Map[LocalDate, CalendarResponse.Status.Value]] =
    for {
      body <- http.get[CalendarResponse.Body](
        uri"https://tickets.horizon22.co.uk/cached_api/events/d57407ef-05d2-b158-535f-9e6b87880b20/calendar?ticket_group_id._in=5fee1a25-2e37-d474-92af-f05c95251a69,9b8a7a50-bcd2-6079-c664-5e7cf2b87c05,496d768e-7729-903a-84a2-2d640a43530e"
      )
    } yield body.calendar.map((k, v) =>
      (
        LocalDate
          .parse(
            k,
            DateTimeFormatter.ISO_LOCAL_DATE
          ),
        CalendarResponse.Status.withName(v)
      )
    )

  private[horizon22] def getSessions(
      date: LocalDate
  ): IO[Seq[EventsResponse.Data]] =
    for {
      body <-
        http.get[EventsResponse.Body](
          uri"https://tickets.horizon22.co.uk/cached_api/events/d57407ef-05d2-b158-535f-9e6b87880b20/sessions?_ondate=${date.shortFormat}&ticket_group.id._in=5fee1a25-2e37-d474-92af-f05c95251a69,9b8a7a50-bcd2-6079-c664-5e7cf2b87c05,496d768e-7729-903a-84a2-2d640a43530e"
        )
    } yield body.event_session._data

private[horizon22] object CalendarResponse:
  final case class Body(
      calendar: Map[
        String,
        String
      ] // "YYYY-MM-DD" -> "available" / "sold_out" / "unreleased"
  )
  object Status extends Enumeration {
    type Status = Value
    val Available = Value("available")
    val SoldOut = Value("sold_out")
    val Unreleased = Value("unreleased")
  }

private[horizon22] object EventsResponse:
  final case class Body(
      event_session: EventSession
  )
  final case class EventSession(
      _data: Seq[Data]
  )
  final case class Data(
      start_datetime: String,
      sold_out: Boolean
  )
