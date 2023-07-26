package com.github.karlchan.beatthequeue.merchants.cinema.vue

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter

import cats.effect.IO
import cats.syntax.all._
import cats.syntax.show
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

final class VueCrawler(
    cinemaIds: Option[Seq[String]] = Some(
      Properties.getList("vue.cinemaIds")
    ),
    untilDate: LocalDate = LocalDate.now.plus(Period.ofYears(1))
) extends EventFinder[Vue]:
  private val http =
    Http(maxParallelism = Properties.getInt("vue.max.parallelism"))

  override def run(): Stream[IO, VueEvent] =
    def listEvents(
        cinema: LocationsResponse.Cinema,
        screeningTypeMapping: Map[String, String]
    ): Stream[IO, VueEvent] =
      for {
        filmWithShowings <- Stream.evalSeq(getShowings(cinema.id))
        showing <- Stream.emits(filmWithShowings.showings)
        time <- Stream.emits(showing.times)
        screenTypes = time.screen_type +: time.tags.flatMap(tag =>
          screeningTypeMapping.get(tag.name)
        )

        event = VueEvent(
          name = filmWithShowings.title,
          time = LocalDateTime
            .of(
              LocalDate.parse(showing.date_time),
              LocalTime.parse(time.time, DateTimeFormatter.ofPattern("h:mm a"))
            ),
          venue = cinema.name,
          screenTypes = screenTypes
        )
      } yield event

    for {
      labels <- Stream.eval(getLabels())
      screeningTypeMapping = labels.screeningtype
        .map(t => (t.code, t.name))
        .toMap

      cinemas <- Stream.evalSeq(getCinemas())
      event <- listEvents(cinemas, screeningTypeMapping)
    } yield event

  final case class Info(
      names: Seq[String],
      venues: Seq[String],
      screenTypes: Seq[String]
  )
  def getInfo(): IO[Info] =
    for {
      films <- getFilms()
      cinemas <- getCinemas()
      labels <- getLabels()
    } yield Info(
      names = films.map(_.title),
      venues = cinemas.map(_.name),
      screenTypes = labels.screentype
        .map(_.name)
        .filter(_ != "All")
        .sorted ++ labels.screeningtype.map(_.name)
    )

  private[vue] def getCinemas(): IO[Seq[LocationsResponse.Cinema]] =
    for {
      body <- http.get[LocationsResponse.Body](
        uri"https://www.myvue.com/data/locations/"
      )
    } yield body.venues
      .flatMap(_.cinemas)
      .filter(cinema => cinemaIds.mapOrTrue(_.contains(cinema.id)))

  private[vue] def getShowings(
      cinemaId: String
  ): IO[Seq[FilmsWithShowingsResponse.Film]] =
    for {
      body <-
        http.get[FilmsWithShowingsResponse.Body](
          uri"https://www.myvue.com/data/filmswithshowings/${cinemaId}"
        )
    } yield body.films

  private[vue] def getFilms(): IO[Seq[FilmsResponse.Film]] =
    for {
      body <-
        http.get[FilmsResponse.Body](
          uri"https://www.myvue.com/data/films/"
        )
    } yield body.films

  private[vue] def getLabels(): IO[LabelsResponse.Body] =
    http.get[LabelsResponse.Body](uri"https://www.myvue.com/data/labels/")

private[vue] object LocationsResponse:
  final case class Body(
      venues: Seq[Venue]
  )
  final case class Venue(
      alpha: String,
      cinemas: Seq[Cinema]
  )
  final case class Cinema(
      id: String,
      name: String
  )

private[vue] object FilmsWithShowingsResponse:
  final case class Body(
      films: Seq[Film]
  )
  final case class Film(
      id: String,
      title: String,
      showings: Seq[Showing]
  )
  final case class Showing(
      date_time: String, // YYYY-MM-DD
      times: Seq[Time]
  )
  final case class Time(
      time: String, // h:mm a
      screen_type: String, // "2D" / "3D"
      tags: Seq[Tag]
  )
  final case class Tag(
      name: String
  )

private[vue] object FilmsResponse:
  final case class Body(
      films: Seq[Film]
  )
  final case class Film(
      id: String,
      title: String
  )

private[vue] object LabelsResponse:
  final case class Body(
      screeningtype: Seq[ScreeningType],
      screentype: Seq[ScreenType]
  )
  final case class ScreeningType(
      name: String,
      code: String
  )

  final case class ScreenType(
      name: String
  )
