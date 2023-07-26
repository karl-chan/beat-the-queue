package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.EventFinder
import com.github.karlchan.beatthequeue.util.Http
import com.github.karlchan.beatthequeue.util.Properties
import com.github.karlchan.beatthequeue.util.given_HttpConnection
import com.github.karlchan.beatthequeue.util.mapOrTrue
import com.github.karlchan.beatthequeue.util.shortFormat
import com.softwaremill.quicklens.modify
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client3._

final class CineworldCrawler(
    cinemaIds: Option[Seq[String]] = Some(
      Properties.getList("cineworld.cinemaIds")
    ),
    untilDate: LocalDate = LocalDate.now.plus(Period.ofYears(1))
) extends EventFinder[Cineworld]:
  private val http =
    Http(maxParallelism = Properties.getInt("cineworld.max.parallelism"))

  override def run(): Stream[IO, CineworldEvent] =
    def listCinemaDates(
        cinemas: Stream[IO, CinemasResponse.Cinema]
    ): Stream[IO, (CinemasResponse.Cinema, List[LocalDate])] =
      for {
        cinema <- cinemas
        datesResponse <- Stream.eval(getBookableDates(cinema.id))
        dates = datesResponse.body.dates.map(LocalDate.parse)
      } yield (cinema, dates)

    def listEvents(
        cinema: CinemasResponse.Cinema,
        dates: List[LocalDate]
    ): Stream[IO, (CinemasResponse.Cinema, FilmEventsResponse.FilmEvents)] =
      for {
        date <- Stream.emits(dates)
        filmEvents <- Stream.eval(getFilmEvents(cinema.id, date))
      } yield (cinema, filmEvents)

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
          screenType = toScreenType(ev.attributeIds.toSet)
        )
      )

    def toScreenType(attributeIds: Set[String]): String =
      val maybeSpecialFormat =
        SpecialFormats.find(format => attributeIds.contains(format.toLowerCase))
      val baseFormat = BaseFormats
        .find(format => attributeIds.contains(format.toLowerCase))
        .getOrElse(
          throw IllegalArgumentException(
            s"Unknown base format: ${attributeIds}"
          )
        )
      maybeSpecialFormat.map(_ + " ").getOrElse("") + baseFormat

    val cinemas = Stream.evalSeq(getCinemas())
    for {
      cinemaDates <- listCinemaDates(cinemas)
      cinemaEvents <- listEvents.tupled(cinemaDates)
      event <- Stream.emits(mergeCinemaEvents.tupled(cinemaEvents))
    } yield event

  final case class Info(
      names: Seq[String],
      venues: Seq[String],
      screenTypes: Seq[String]
  )
  def getInfo(): IO[Info] =
    for {
      res <- Seq(getNowPlaying(), getComingSoon()).parSequence
      Seq(nowPlayingRes, comingSoonRes) = res
      posters = nowPlayingRes.body.posters ::: comingSoonRes.body.posters
      names = posters.map(_.featureTitle)

      cinemas <- getCinemas()
      venues = cinemas.map(_.displayName)
    } yield Info(
      names = names,
      venues = venues,
      screenTypes = BaseFormats ::: SpecialFormats ::: (for {
        baseFormat <- BaseFormats
        specialFormat <- SpecialFormats
      } yield s"$specialFormat $baseFormat")
    )

  private[cineworld] def getCinemas(): IO[Seq[CinemasResponse.Cinema]] =
    for {
      allCinemas <- http.get[CinemasResponse.Cinemas](
        uri"https://www.cineworld.co.uk/uk/data-api-service/v1/quickbook/10108/cinemas/with-event/until/${untilDate.shortFormat}"
      )
    } yield allCinemas.body.cinemas.filter(cinema =>
      cinemaIds.mapOrTrue(_.contains(cinema.id))
    )

  private[cineworld] def getBookableDates(
      cinemaId: String
  ): IO[BookableDatesResponse.Dates] =
    http.get[BookableDatesResponse.Dates](
      uri"https://www.cineworld.co.uk/uk/data-api-service/v1/quickbook/10108/dates/in-cinema/${cinemaId}/until/${untilDate.shortFormat}"
    )

  private[cineworld] def getFilmEvents(
      cinemaId: String,
      date: LocalDate
  ): IO[FilmEventsResponse.FilmEvents] =
    http.get[FilmEventsResponse.FilmEvents](
      uri"https://www.cineworld.co.uk/uk/data-api-service/v1/quickbook/10108/film-events/in-cinema/${cinemaId}/at-date/${date.shortFormat}"
    )

  private[cineworld] def getNowPlaying(): IO[FeedResponse.Feed] =
    http.get[FeedResponse.Feed](
      uri"https://www.cineworld.co.uk/uk/data-api-service/v1/feed/10108/byName/now-playing"
    )

  private[cineworld] def getComingSoon(): IO[FeedResponse.Feed] =
    http.get[FeedResponse.Feed](
      uri"https://www.cineworld.co.uk/uk/data-api-service/v1/feed/10108/byName/coming-soon"
    )

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

private[this] val BaseFormats = List("3D", "2D")
private[this] val SpecialFormats =
  List("IMAX", "4DX", "ScreenX", "Superscreen", "VIP")
