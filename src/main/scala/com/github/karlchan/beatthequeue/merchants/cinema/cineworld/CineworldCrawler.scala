package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.EventFinder
import com.github.karlchan.beatthequeue.util.Http
import com.github.karlchan.beatthequeue.util.Properties
import com.github.karlchan.beatthequeue.util.shortFormat
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.EntityDecoder
import org.http4s.Uri
import org.http4s.circe.jsonOf
import org.http4s.implicits.uri

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

class CineworldCrawler(
    untilDate: LocalDate = LocalDate.now.plus(Period.ofYears(1))
) extends EventFinder[Cineworld]:
  private val http =
    Http(maxParallelism = Properties.getInt("cineworld.max.parallelism"))

  override def run(): IO[Seq[CineworldEvent]] =
    def listCinemaDates(): IO[Seq[(CinemasResponse.Cinema, List[LocalDate])]] =
      for {
        cinemas <- getCinemas().map(_.body.cinemas)
        dates <- cinemas
          .parTraverse(cinema => getBookableDates(cinema.id))
          .map(bookableDatesResponse =>
            bookableDatesResponse.map(_.body.dates.map(LocalDate.parse))
          )
      } yield cinemas zip dates

    def listEvents(
        cinema: CinemasResponse.Cinema,
        dates: List[LocalDate]
    ): IO[Seq[(CinemasResponse.Cinema, FilmEventsResponse.FilmEvents)]] =
      for {
        filmEvents <- dates.parTraverse(getFilmEvents(cinema.id, _))
      } yield filmEvents.map((cinema, _))

    def mergeCinemaEvents(
        cinema: CinemasResponse.Cinema,
        filmEvents: FilmEventsResponse.FilmEvents
    ): Seq[CineworldEvent] =
      val filmsById =
        filmEvents.body.films.collect(film => (film.id, film)).toMap
      filmEvents.body.events.map(ev =>
        CineworldEvent(
          name = filmsById(ev.filmId).name,
          time = LocalDateTime.parse(ev.eventDateTime),
          venue = cinema.displayName,
          screenType = ev.attributeIds.mkString(" ")
        )
      )

    for {
      cinemaDates <- listCinemaDates()
      cinemaEvents <- cinemaDates.parFlatTraverse(listEvents.tupled)
    } yield cinemaEvents.flatMap(mergeCinemaEvents.tupled)

  private[cineworld] def getCinemas(): IO[CinemasResponse.Cinemas] =
    http.get[CinemasResponse.Cinemas](
      s"https://www.cineworld.co.uk/uk/data-api-service/v1/quickbook/10108/cinemas/with-event/until/${untilDate.shortFormat}"
    )(using jsonOf[IO, CinemasResponse.Cinemas])

  private[cineworld] def getBookableDates(
      cinemaId: String
  ): IO[BookableDatesResponse.Dates] =
    http.get[BookableDatesResponse.Dates](
      s"https://www.cineworld.co.uk/uk/data-api-service/v1/quickbook/10108/dates/in-cinema/${cinemaId}/until/${untilDate.shortFormat}"
    )(using jsonOf[IO, BookableDatesResponse.Dates])

  private[cineworld] def getFilmEvents(
      cinemaId: String,
      date: LocalDate
  ): IO[FilmEventsResponse.FilmEvents] =
    http.get[FilmEventsResponse.FilmEvents](
      s"https://www.cineworld.co.uk/uk/data-api-service/v1/quickbook/10108/film-events/in-cinema/${cinemaId}/at-date/${date.shortFormat}"
    )(using jsonOf[IO, FilmEventsResponse.FilmEvents])

private[cineworld] object CinemasResponse:
  case class Cinemas(
      body: Body
  )
  case class Body(
      cinemas: List[Cinema]
  )
  case class Cinema(
      address: String,
      addressInfo: AddressInfo,
      blockOnlineSales: Boolean,
      displayName: String,
      groupId: String,
      id: String,
      imageUrl: String,
      latitude: Double,
      link: String,
      longitude: Double
  )
  case class AddressInfo(
      address1: Option[String],
      address2: Option[String],
      address3: Option[String],
      address4: Option[String],
      city: String,
      postalCode: String,
      state: Option[String]
  )

private[cineworld] object BookableDatesResponse:
  case class Dates(
      body: Body
  )
  case class Body(
      dates: List[String]
  )

private[cineworld] object FilmEventsResponse:
  case class FilmEvents(
      body: Body
  )
  case class Body(
      events: List[Event],
      films: List[Film]
  )
  case class Event(
      attributeIds: List[String],
      auditorium: String,
      auditoriumTinyName: String,
      bookingLink: String,
      businessDay: String,
      cinemaId: String,
      eventDateTime: String,
      filmId: String,
      id: String,
      soldOut: Boolean
  )

  case class Film(
      attributeIds: List[String],
      id: String,
      length: Int, // in minutes
      link: String,
      name: String,
      posterLink: String,
      releaseYear: String,
      videoLink: Option[String],
      weight: Int
  )
