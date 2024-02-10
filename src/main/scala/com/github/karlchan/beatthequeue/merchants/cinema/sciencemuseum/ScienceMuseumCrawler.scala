package com.github.karlchan.beatthequeue.merchants.cinema.sciencemuseum

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.EventFinder
import com.github.karlchan.beatthequeue.util.Http
import com.github.karlchan.beatthequeue.util.Properties
import com.github.karlchan.beatthequeue.util.given_HttpConnection
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client3._
import sttp.model.Uri
import sttp.model.headers.CookieWithMeta

final class ScienceMuseumCrawler(
    untilDate: LocalDate = LocalDate.now.plus(Period.ofYears(1))
) extends EventFinder[ScienceMuseum]:
  private val http = Http()

  override def run(): Stream[IO, ScienceMuseumEvent] =
    for {
      productionSeason <- Stream.evals(
        getProductionSeasons(startDate = LocalDate.now(), endDate = untilDate)
      )
      performance <- Stream.emits(productionSeason.performances)
    } yield ScienceMuseumEvent(
      name = performance.performanceTitle,
      time = LocalDateTime.parse(performance.iso8601DateString),
      productTypeId = performance.productTypeId.toString
    )

  final case class Info(
      names: Seq[String],
      productTypeIds: Seq[String]
  )
  def getInfo(): IO[Info] =
    for {
      productionSeasons <- getProductionSeasons(
        startDate = LocalDate.now(),
        endDate = untilDate
      )
    } yield Info(
      names = productionSeasons.map(_.productionTitle).distinct,
      productTypeIds = productionSeasons
        .flatMap(_.performances.map(_.productTypeId))
        .sorted
        .distinct
        .map(_.toString)
    )

  private[sciencemuseum] def getProductionSeasons(
      startDate: LocalDate,
      endDate: LocalDate
  ): IO[Seq[Response.ProductionSeason]] =
    for {
      token <- getToken()
      body <- http.post[Seq[Response.ProductionSeason]](
        uri"https://my.sciencemuseum.org.uk/api/products/productionseasons",
        Map(
          "startDate" -> startDate
            .format(DateTimeFormatter.ISO_LOCAL_DATE),
          "endDate" -> endDate
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
        ),
        cookies = token.cookies
      )
    } yield body

  private[sciencemuseum] def getToken(): IO[Token] = {
    for {
      body <- http.get[TokenResponse.Body](
        Uri.unsafeParse(Properties.get("sciencemuseum.token.url"))
      )
      cookies = body.cookies.map(c => CookieWithMeta(c.name, c.value))
    } yield Token(cookies)
  }

  final private[sciencemuseum] case class Token(
      cookies: Seq[CookieWithMeta]
  )

private[sciencemuseum] object TokenResponse:
  final case class Body(
      cookies: Seq[NameValuePair]
  )
  final case class NameValuePair(
      name: String,
      value: String
  )

private[sciencemuseum] object Response:
  final case class ProductionSeason(
      productionTitle: String,
      performances: Seq[Performance]
  )
  final case class Performance(
      performanceTitle: String,
      iso8601DateString: String,
      productTypeId: Int
  )
