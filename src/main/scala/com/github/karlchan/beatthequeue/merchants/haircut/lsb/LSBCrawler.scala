package com.github.karlchan.beatthequeue.merchants.haircut.lsb

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
import io.circe.HCursor
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client3._
import sttp.model.Uri

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter

final class LSBCrawler(
    untilDate: LocalDate = LocalDate.now.plus(Period.ofMonths(1))
) extends EventFinder[LSB]:
  private val http = Http(
    maxParallelism = Properties.getInt("lsb.max.parallelism"),
    persistCookies = true
  )

  private val cachedToken: IO[Token] =
    getToken().memoize.unsafeRunSync()

  override def run(): Stream[IO, LSBEvent] =
    for {
      categories <- Stream.eval(getCategories())
      services <- Stream.eval(getServices())

      categoryIdsToNamesLookup = categories
        .map(category => (category.id, category.name))
        .toMap
      serviceIdsToNamesLookup = services
        .map(service => (service.id, service.name))
        .toMap

      category <- Stream.emits(categories)
      (serviceId, timeSlots) <- Stream.evals(
        category.events.parTraverse(serviceId =>
          getTimeSlots(
            from = LocalDate.now,
            to = untilDate,
            category = category.id,
            service = serviceId
          ).map(timeSlots => (serviceId, timeSlots.filter(_.`type` == "free")))
        )
      )
      timeSlot <- Stream.emits(timeSlots)
    } yield LSBEvent(
      name = serviceIdsToNamesLookup(serviceId),
      time = LocalDateTime.of(
        LocalDate
          .parse(
            timeSlot.date,
            DateTimeFormatter.ISO_LOCAL_DATE
          ),
        LocalTime
          .parse(
            timeSlot.time,
            DateTimeFormatter.ISO_LOCAL_TIME
          )
      ),
      category = categoryIdsToNamesLookup(category.id)
    )

  final case class Info(
      categories: Seq[String],
      services: Seq[String]
  )
  def getInfo(): IO[Info] = for {
    categories <- getCategories()
    services <- getServices()
  } yield Info(
    categories = categories.map(_.name).sorted,
    services = services.map(_.name).sorted
  )

  private[lsb] def getCategories(): IO[Vector[CategoryResponse.Category]] =
    get(
      uri"https://liverpoolstreetbarber.simplybook.it/v2/ext/category/"
    )

  private[lsb] def getServices(): IO[Vector[ServiceResponse.Service]] =
    get(
      uri"https://liverpoolstreetbarber.simplybook.it/v2/service/"
    )
  private[lsb] def getTimeSlots(
      from: LocalDate,
      to: LocalDate,
      category: String,
      service: String
  ): IO[Vector[TimeSlotsResponse.TimeSlot]] =
    get(
      uri"https://liverpoolstreetbarber.simplybook.it/v2/booking/time-slots/"
        .param("from", from.shortFormat)
        .param("to", to.shortFormat)
        .param("category", category)
        .param("service", service)
    )

  private def get[R](uri: Uri)(using d: Decoder[R]): IO[R] =
    for {
      token <- cachedToken
      body <- http.get[R](
        uri,
        headers = Map("X-Csrf-Token" -> token.csrfToken)
      )
    } yield body

  private[lsb] def getToken(): IO[Token] = {
    val csrfTokenRegex = raw"\"csrf_token\":\"([^\"]+)\"".r

    for {
      html <- http
        .getHtml(
          uri"https://liverpoolstreetbarber.simplybook.it/v2/"
        )
      csrfToken = csrfTokenRegex
        .findFirstMatchIn(html)
        .getOrElse(
          throw IllegalArgumentException(s"csrfToken not found in html!")
        )
        .group(1)
    } yield Token(csrfToken)
  }

  final private[lsb] case class Token(
      csrfToken: String
  )

private[lsb] object CategoryResponse:
  final case class Category(
      id: String,
      name: String,
      events: Seq[String]
  )

private[lsb] object ServiceResponse:
  final case class Service(
      id: String,
      name: String
  )

private[lsb] object TimeSlotsResponse:
  final case class TimeSlot(
      date: String,
      time: String,
      `type`: String
  )
