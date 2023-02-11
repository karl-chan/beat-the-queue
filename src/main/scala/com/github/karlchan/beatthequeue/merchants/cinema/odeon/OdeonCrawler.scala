package com.github.karlchan.beatthequeue.merchants.cinema.odeon

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.EventFinder
import com.github.karlchan.beatthequeue.util.Http
import com.github.karlchan.beatthequeue.util.Properties
import com.github.karlchan.beatthequeue.util.given_HttpConnection
import com.github.karlchan.beatthequeue.util.shortFormat
import fs2.Stream
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client3._
import sttp.model.Uri

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

final class OdeonCrawler(
    siteIds: Option[Seq[String]] = Some(Properties.getList("odeon.siteIds")),
    untilDate: LocalDate = LocalDate.now.plus(Period.ofYears(1))
) extends EventFinder[Odeon]:
  private val http =
    Http(maxParallelism = Properties.getInt("odeon.max.parallelism"))

  private val cachedToken: IO[Token] =
    getToken().memoize.unsafeRunSync()

  override def run(): Stream[IO, OdeonEvent] =
    for {
      sites <- Stream.eval(getSites())
      availability <- Stream.eval(getAvailability())
      date <- Stream.evalSeq(
        getScreeningDates()
          .map(_.filmScreeningDates.map(_.businessDate).map(LocalDate.parse))
      )
      showtimesRes <- Stream.eval(getShowtimes(date))
      showtime <- Stream.emits(showtimesRes.showtimes)

      siteIdsToNamesLookup = sites.sites
        .map(site => (site.id, site.name.text))
        .toMap
      filmIdsToNamesLookup = showtimesRes.relatedData.films
        .map(film => (film.id, film.title.text))
        .toMap
      attributeIdsToNamesLookup = availability.relatedData.attributes
        .filter(_.displayPriority == 1)
        .map(attribute => (attribute.id, attribute.shortName.text))
        .toMap
    } yield OdeonEvent(
      name = filmIdsToNamesLookup(showtime.filmId),
      time = LocalDateTime
        .parse(
          showtime.schedule.startsAt,
          DateTimeFormatter.ISO_OFFSET_DATE_TIME
        ),
      venue = siteIdsToNamesLookup(showtime.siteId),
      screenType = showtime.attributeIds
        .flatMap(attributeIdsToNamesLookup.get)
        .headOption
        .getOrElse("2D")
    )

  final case class Info(
      names: Seq[String],
      venues: Seq[String],
      screenTypes: Seq[String]
  )
  def getInfo(): IO[Info] = for {
    sites <- getSites()
    films <- getFilms()
    availability <- getAvailability()
  } yield Info(
    names = films.films.map(_.title.text).distinct,
    venues =
      if siteIds.isEmpty then sites.sites.map(_.name.text)
      else
        sites.sites
          .filter(site => siteIds.get.contains(site.id))
          .map(_.name.text)
    ,
    screenTypes = availability.relatedData.attributes
      .filter(_.displayPriority == 1)
      .map(_.shortName.text)
      .sorted
  )

  private[odeon] def getSites(): IO[SitesResponse.Body] =
    get(
      uri"https://vwc.odeon.co.uk/WSVistaWebClient/ocapi/v1/sites"
    )

  private[odeon] def getFilms(): IO[FilmsResponse.Body] =
    get(
      uri"https://vwc.odeon.co.uk/WSVistaWebClient/ocapi/v1/films"
    )

  private[odeon] def getAvailability(): IO[AvailabilityResponse.Body] =
    get(
      uri"https://vwc.odeon.co.uk/WSVistaWebClient/ocapi/v1/films/availability"
    )

  private[odeon] def getScreeningDates(): IO[ScreeningDatesResponse.Body] =
    get(
      uri"https://vwc.odeon.co.uk/WSVistaWebClient/ocapi/v1/film-screening-dates"
        .params(siteIdsQueryParams*)
    )

  private[odeon] def getShowtimes(date: LocalDate): IO[ShowtimesResponse.Body] =
    get(
      uri"https://vwc.odeon.co.uk/WSVistaWebClient/ocapi/v1/showtimes/by-business-date/${date.shortFormat}"
        .params(siteIdsQueryParams*)
    )

  private def get[R](uri: Uri)(using d: Decoder[R]): IO[R] =
    for {
      token <- cachedToken
      body <-
        http.get[R](
          uri,
          headers = Map("Authorization" -> s"Bearer ${token.authToken}")
        )
    } yield body

  private[odeon] def getToken(): IO[Token] = {
    val authTokenRegex = raw"\"authToken\":\"([^\"]+)\"".r

    for {
      html <- http
        .getHtml(
          uri"https://webcache.googleusercontent.com/search?q=cache:https://www.odeon.co.uk"
        )

      authToken = authTokenRegex
        .findFirstMatchIn(html)
        .getOrElse(
          throw IllegalArgumentException(s"authToken not found in html!")
        )
        .group(1)
    } yield Token(authToken)
  }

  final private[odeon] case class Token(
      authToken: String
  )

  private val siteIdsQueryParams: Seq[(String, String)] =
    siteIds.map(_.map(("siteIds", _))).getOrElse(Seq.empty)

private[odeon] object SitesResponse:
  final case class Body(
      sites: Seq[Site]
  )
  final case class Site(
      id: String,
      name: SiteName
  )
  final case class SiteName(
      text: String
  )

private[odeon] object FilmsResponse:
  final case class Body(
      films: Seq[Film]
  )
  final case class Film(
      id: String,
      title: FilmTitle
  )
  final case class FilmTitle(
      text: String
  )

private[odeon] object AvailabilityResponse:
  final case class Body(
      relatedData: RelatedData
  )
  final case class RelatedData(
      attributes: Seq[Attribute]
  )
  final case class Attribute(
      id: String,
      shortName: ShortName,
      displayPriority: Int
  )
  final case class ShortName(
      text: String
  )

private[odeon] object ScreeningDatesResponse:
  final case class Body(
      filmScreeningDates: Seq[FilmScreeningDate]
  )
  final case class FilmScreeningDate(
      businessDate: String
  )

private[odeon] object ShowtimesResponse:
  final case class Body(
      businessDate: String,
      showtimes: Seq[Showtime],
      relatedData: RelatedData
  )
  final case class Showtime(
      filmId: String,
      schedule: Schedule,
      attributeIds: Seq[String],
      siteId: String
  )
  final case class Schedule(
      startsAt: String
  )
  final case class RelatedData(
      attributes: Seq[Attribute],
      films: Seq[Film]
  )
  final case class Attribute(
      id: String,
      name: AttributeName
  )
  final case class AttributeName(
      text: String
  )
  final case class Film(
      id: String,
      title: FilmTitle
  )
  final case class FilmTitle(
      text: String
  )
