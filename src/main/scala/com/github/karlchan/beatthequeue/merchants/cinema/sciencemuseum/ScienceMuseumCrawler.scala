package com.github.karlchan.beatthequeue.merchants.cinema.sciencemuseum

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

import java.time.LocalDateTime

final class ScienceMuseumCrawler extends EventFinder[ScienceMuseum]:
  private val http = Http()

  override def run(): Stream[IO, ScienceMuseumEvent] =
    for {
      productionSeason <- Stream.evals(getProductionSeasons())
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
      productionSeasons <- getProductionSeasons()
    } yield Info(
      names = productionSeasons.map(_.productionTitle),
      productTypeIds = productionSeasons
        .flatMap(_.performances.map(_.productTypeId))
        .sorted
        .distinct
        .map(_.toString)
    )

  private[sciencemuseum] def getProductionSeasons()
      : IO[Seq[Response.ProductionSeason]] =
    http.get[Seq[Response.ProductionSeason]](
      Uri.unsafeParse(Properties.get("sciencemuseum.events.url"))
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
