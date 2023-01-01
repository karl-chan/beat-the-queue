package com.github.karlchan.beatthequeue.merchants.cinema.bfi

import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.EventFinder
import com.github.karlchan.beatthequeue.util.Http
import com.github.karlchan.beatthequeue.util.Properties
import com.github.karlchan.beatthequeue.util.mapOrTrue
import com.github.karlchan.beatthequeue.util.shortFormat
import com.softwaremill.quicklens.modify
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client3._

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

final class BFICrawler(
    untilDate: LocalDate = LocalDate.now.plus(Period.ofYears(1))
) extends EventFinder[BFI]:
  private val http =
    Http(
      maxParallelism = Properties.getInt("bfi.max.parallelism"),
      persistCookies = true
    )

  override def run(): Stream[IO, BFIEvent] = ???

  final case class Info(
      names: Seq[String],
      venues: Seq[String],
      screenTypes: Seq[String]
  )
  def getInfo(): IO[Info] = ???

  final private[bfi] case class Token(
      sToken: String,
      articleId: String
  )

  private[bfi] def getToken(): IO[Token] = {
    val sTokenMatchRegex = raw"sToken: \"([^\"]+)\"".r
    val articleIdMatchRegex = raw"articleId: \"([^\"]+)\"".r

    for {
      html <- http
        .getHtml(uri"https://whatson.bfi.org.uk/imax/Online/default.asp")

      sToken = sTokenMatchRegex
        .findFirstMatchIn(html)
        .getOrElse(throw IllegalArgumentException(s"sToken not found in html!"))
        .group(1)

      articleId = articleIdMatchRegex
        .findFirstMatchIn(html)
        .getOrElse(
          throw IllegalArgumentException("articleId not found in html!")
        )
        .group(1)

    } yield Token(sToken = sToken, articleId = articleId)
  }
