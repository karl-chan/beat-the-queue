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

final class CineworldCrawler(
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
          screenType = toScreenType(ev.attributeIds.toSet).toUpperCase
        )
      )

    def toScreenType(attributeIds: Set[String]): String =
      for
        format <- SpecialFormats
        if attributeIds.contains(format)
      do return s"${format} ${toScreenType(attributeIds - format)}"

      for
        format <- BaseFormats
        if attributeIds.contains(format)
      do return format
      throw IllegalArgumentException(s"Unknown screen type: ${attributeIds}")

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

  private[cineworld] def getNowPlaying(): IO[FeedResponse.Feed] =
    http.get[FeedResponse.Feed](
      "https://www.cineworld.co.uk/uk/data-api-service/v1/feed/10108/byName/now-playing"
    )(using jsonOf[IO, FeedResponse.Feed])

  private[cineworld] def getComingSoon(): IO[FeedResponse.Feed] =
    http.get[FeedResponse.Feed](
      "https://www.cineworld.co.uk/uk/data-api-service/v1/feed/10108/byName/coming-soon"
    )(using jsonOf[IO, FeedResponse.Feed])

private[cineworld] object CinemasResponse:
  final case class Cinemas(
      body: Body
  )
  final case class Body(
      cinemas: List[Cinema]
  )
  final case class Cinema(
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
  final case class AddressInfo(
      address1: Option[String],
      address2: Option[String],
      address3: Option[String],
      address4: Option[String],
      city: String,
      postalCode: String,
      state: Option[String]
  )

private[cineworld] object BookableDatesResponse:
  final case class Dates(
      body: Body
  )
  final case class Body(
      dates: List[String]
  )

private[cineworld] object FilmEventsResponse:
  final case class FilmEvents(
      body: Body
  )
  final case class Body(
      events: List[Event],
      films: List[Film]
  )
  final case class Event(
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

  final case class Film(
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

private[cineworld] object FeedResponse:
  final case class Feed(
      body: Body
  )

  final case class Body(
      posters: List[Poster]
  )

  final case class Poster(
      attributes: List[String],
      code: String,
      dateStarted: String,
      featureTitle: String,
      mediaList: List[Media],
      posterSrc: String,
      url: String,
      weight: Int
  )

  final case class Media(
      dimensionHeight: Option[Int],
      dimensionWidth: Option[Int],
      subType: String,
      `type`: String,
      url: String
  )

private[this] val BaseFormats = List("2d", "3d")
private[this] val SpecialFormats = List("imax", "4dx", "superscreen")
