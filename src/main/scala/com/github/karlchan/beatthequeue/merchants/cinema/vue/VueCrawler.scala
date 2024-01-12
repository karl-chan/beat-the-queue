package com.github.karlchan.beatthequeue.merchants.cinema.vue

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

import scala.concurrent.duration.DurationInt

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.EventFinder
import com.github.karlchan.beatthequeue.util.Http
import com.github.karlchan.beatthequeue.util.Properties
import com.github.karlchan.beatthequeue.util.given_HttpConnection
import com.github.karlchan.beatthequeue.util.mapOrTrue
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client3._
import sttp.model.Uri
import sttp.model.headers.CookieWithMeta

final class VueCrawler(
    cinemaIds: Option[Seq[String]] = Some(
      Properties.getList("vue.cinemaIds")
    ),
    untilDate: LocalDate = LocalDate.now.plus(Period.ofYears(1))
) extends EventFinder[Vue]:
  private val http = Http()

  private val cachedToken: IO[Token] =
    getToken().memoize.unsafeRunSync()

  override def run(): Stream[IO, VueEvent] =
    for {
      attributes <- Stream.eval(getAttributes())
      attributeNames = attributes.toSet

      cinema <- Stream.evals(getCinemas())
      filmShowings <- Stream.evals(getShowings(cinema.cinemaId))

      name = filmShowings.filmTitle
      showingGroup <- Stream.emits(filmShowings.showingGroups)
      session <- Stream.emits(showingGroup.sessions)
      time = LocalDateTime.parse(
        session.showTimeWithTimeZone,
        DateTimeFormatter.ISO_OFFSET_DATE_TIME
      )
      screenTypes = session.attributes
        .map(_.name)
        .filter(name => attributeNames.contains(name))
    } yield VueEvent(
      name = name,
      time = time,
      venue = cinema.cinemaName,
      screenTypes = screenTypes
    )

  final case class Info(
      names: Seq[String],
      venues: Seq[String],
      screenTypes: Seq[String]
  )
  def getInfo(): IO[Info] =
    for {
      films <- getFilms()
      cinemas <- getCinemas()
      attributes <- getAttributes()
    } yield Info(
      names = films.map(_.filmTitle),
      venues = cinemas.map(_.cinemaName),
      screenTypes = attributes.sorted.distinct
    )

  private[vue] def getCinemas(): IO[Seq[CinemasResponse.Cinema]] =
    for {
      token <- cachedToken
      body <- http.get[CinemasResponse.Body](
        uri"https://www.myvue.com/api/microservice/showings/cinemas",
        cookies = token.cookies
      )
    } yield body.result
      .flatMap(_.cinemas)
      .filter(cinema => cinemaIds.mapOrTrue(_.contains(cinema.cinemaId)))

  private[vue] def getFilms(): IO[Seq[FilmsResponse.Film]] =
    for {
      token <- cachedToken
      body <-
        http.get[FilmsResponse.Body](
          uri"https://www.myvue.com/api/microservice/showings/films",
          cookies = token.cookies
        )
    } yield body.result

  private[vue] def getShowings(
      cinemaId: String
  ): IO[Seq[ShowingsResponse.Result]] =
    for {
      token <- cachedToken
      body <-
        http.get[ShowingsResponse.Body](
          uri"https://www.myvue.com/api/microservice/showings/cinemas/${cinemaId}/films?minEmbargoLevel=3&includesSession=true&includeSessionAttributes=true",
          cookies = token.cookies
        )
    } yield body.result

  private[vue] def getAttributes(): IO[Seq[String]] =
    for {
      token <- cachedToken
      body <-
        http.get[AttributesResponse.Body](
          uri"https://www.myvue.com/api/microservice/showings/attributes/showingAttributeGroups",
          cookies = token.cookies
        )
    } yield body.result
      .find(_.name == "Filter By Screening Type")
      .get
      .showingAttributes
      .map(_.name)

  private[vue] def getToken(): IO[Token] = {
    for {
      body <- http.get[TokenResponse.Body](
        Uri.unsafeParse(Properties.get("vue.token.url"))
      )
      cookies = body.cookies.map(c => CookieWithMeta(c.name, c.value))
    } yield Token(cookies)
  }

  final private[vue] case class Token(
      cookies: Seq[CookieWithMeta]
  )

private[vue] object CinemasResponse:
  final case class Body(
      result: Seq[Result]
  )
  final case class Result(
      alpha: String,
      cinemas: Seq[Cinema]
  )
  final case class Cinema(
      cinemaId: String,
      cinemaName: String
  )

private[vue] object FilmsResponse:
  final case class Body(
      result: Seq[Film]
  )
  final case class Film(
      filmId: String,
      filmTitle: String
  )

private[vue] object ShowingsResponse:
  final case class Body(
      result: Seq[Result]
  )
  final case class Result(
      showingGroups: Seq[ShowingGroup],
      filmId: String,
      filmTitle: String
  )
  final case class ShowingGroup(
      date: String, // YYYY-MM-DDTHH:mm:ss
      sessions: Seq[Session]
  )
  final case class Session(
      attributes: Seq[Attribute],
      showTimeWithTimeZone: String // YYYY-MM-DDTHH:mm:ssZ
  )
  final case class Attribute(
      name: String
  )

private[vue] object AttributesResponse:
  final case class Body(
      result: Seq[Result]
  )
  final case class Result(
      name: String,
      showingAttributes: Seq[Attribute]
  )

  final case class Attribute(
      name: String
  )

private[vue] object TokenResponse:
  final case class Body(
      cookies: Seq[NameValuePair]
  )
  final case class NameValuePair(
      name: String,
      value: String
  )
