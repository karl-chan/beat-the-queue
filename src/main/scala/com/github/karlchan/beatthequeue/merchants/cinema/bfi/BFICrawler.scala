package com.github.karlchan.beatthequeue.merchants.cinema.bfi

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

import scala.jdk.CollectionConverters._

import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.EventFinder
import com.github.karlchan.beatthequeue.util.Http
import com.github.karlchan.beatthequeue.util.Properties
import com.github.karlchan.beatthequeue.util.given_HttpConnection
import fs2.Stream
import io.circe.Decoder
import io.circe.HCursor
import io.circe.parser.decode
import org.jsoup.Jsoup
import sttp.client3._
import sttp.model.Uri

final class BFICrawler(
    untilDate: LocalDate = LocalDate.now.plus(Period.ofYears(1))
) extends EventFinder[BFI]:
  private val http =
    Http(
      maxParallelism = Properties.getInt("bfi.max.parallelism"),
      persistCookies = true
    )

  override def run(): Stream[IO, BFIEvent] =
    for {
      filmEvent <- Stream.evalSeq(
        getFilmEvents(startDate = LocalDate.now, endDate = untilDate)
      )
    } yield BFIEvent(
      name = filmEvent.description,
      time = filmEvent.startTime,
      venue = filmEvent.venue,
      screenType = filmEvent.screenType
    )

  final case class Info(
      names: Seq[String],
      venues: Seq[String],
      screenTypes: Seq[String]
  )
  def getInfo(): IO[Info] =
    for {
      filmEvents <- getFilmEvents(LocalDate.now, untilDate)
    } yield Info(
      names = filmEvents.map(_.description).sorted.distinct,
      venues = filmEvents.map(_.venue).sorted.distinct,
      screenTypes = filmEvents.map(_.screenType).sorted.distinct
    )

  final private[bfi] case class FilmEvent(
      id: String,
      screenType: String,
      description: String,
      startTime: LocalDateTime,
      venue: String,
      onSaleDate: Option[LocalDateTime],
      minPrice: Double,
      maxPrice: Double
  )

  final private[bfi] case class Token(
      sToken: String,
      articleSearchId: String
  )

  private[bfi] def getFilmEvents(
      startDate: LocalDate,
      endDate: LocalDate
  ): IO[Vector[FilmEvent]] =
    val searchResultsRegex = raw"(?s)\n  searchResults : (\[.*?\n  ]),".r

    def parseTime(s: String): LocalDateTime =
      LocalDateTime.parse(
        s,
        DateTimeFormatter.ofPattern("EEEE dd LLLL yyyy HH:mm")
      )
    def parsePrice(s: String): Double =
      s.replaceAllLiterally("£", "").toDouble

    given filmEventDecoder: Decoder[FilmEvent] = (c: HCursor) =>
      for {
        id <- c.downN(0).as[String]
        screenType <- c.downN(2).as[String]
        description <- c.downN(6).as[String]
        startTime <- c.downN(7).as[String]
        venue <- c.downN(64).as[String]
        onSaleDate <- c.downN(13).as[String]
        minPrice <- c.downN(81).as[String]
        maxPrice <- c.downN(82).as[String]
      } yield FilmEvent(
        id = id,
        screenType = screenType,
        description = description,
        startTime = parseTime(startTime),
        venue = venue,
        onSaleDate = onSaleDate match {
          case ""   => None
          case text => Some(parseTime(text))
        },
        minPrice = parsePrice(minPrice),
        maxPrice = parsePrice(maxPrice)
      )

    http.get[Vector[FilmEvent]](
      Uri.unsafeParse(Properties.get("bfi.cache.url"))
    )
