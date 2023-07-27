package com.github.karlchan.beatthequeue.merchants.cinema.picturehouse

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

final class PicturehouseCrawler(
    cinemaIds: Option[Seq[String]] = Some(
      Properties.getList("picturehouse.cinemaIds")
    )
) extends EventFinder[Picturehouse]:
  private val http = Http(persistCookies = true)

  override def run(): Stream[IO, PicturehouseEvent] =
    for {
      cinemas <- Stream.eval(getCinemas())
      cinemaIdToNameLookup = cinemas.map(c => c.cinema_id -> c.name).toMap

      movie <- Stream.evals(getMovies())
      movieTime <- Stream.emits(movie.movie_times)
    } yield PicturehouseEvent(
      name = movie.Title,
      time = LocalDateTime.parse(movieTime.Showtime),
      venue = cinemaIdToNameLookup(movieTime.CinemaId),
      screenType = movieTime.SessionAttributesNames.mkString(" ")
    )

  final case class Info(
      names: Seq[String],
      venues: Seq[String],
      screenTypes: Seq[String]
  )
  def getInfo(): IO[Info] = for {
    cinemas <- getCinemas()
    movies <- getMovies()
    names = movies.map(_.Title).sorted.distinct
    venues = cinemas.map(_.name).sorted.distinct
    screenTypes = movies
      .flatMap(_.movie_times.flatMap(_.SessionAttributesNames))
      .sorted
      .distinct
  } yield Info(
    names = names,
    venues = venues,
    screenTypes = screenTypes
  )

  private[picturehouse] def getCinemas(): IO[Seq[CinemasResponse.Cinema]] =
    for {
      token <- getToken()
      body <- http.post[CinemasResponse.Body](
        uri"https://www.picturehouses.com/ajax-cinema-list",
        Map("_token" -> token.token)
      )
    } yield body.cinema_list
      .filter(c => cinemaIds.mapOrTrue(_.contains(c.cinema_id)))

  private[picturehouse] def getMovies(): IO[Seq[MoviesResponse.Movie]] =
    for {
      body <- http.post[MoviesResponse.Body](
        uri"https://www.picturehouses.com/api/get-movies-ajax",
        Map(
          "start_date" -> "show_all_dates"
        ) ++ cinemaIds
          .map(_.map("cinema_id" -> _))
          .getOrElse(Vector("cinema_id" -> "0"))
      )
    } yield body.movies

  private[picturehouse] def getToken(): IO[Token] = {
    val tokenMatchRegex =
      raw"<input type=\"hidden\" name=\"_token\" value=\"([^\"]+)\">".r

    for {
      html <- http.getHtml(uri"https://www.picturehouses.com/")
      token = tokenMatchRegex
        .findFirstMatchIn(html)
        .getOrElse(
          throw IllegalArgumentException(s"token not found in html!")
        )
        .group(1)
    } yield Token(token)
  }

  final private[picturehouse] case class Token(
      token: String
  )

private[picturehouse] object CinemasResponse:
  final case class Body(
      cinema_list: Seq[Cinema]
  )
  final case class Cinema(
      cinema_id: String,
      name: String
  )

private[picturehouse] object MoviesResponse:
  final case class Body(
      movies: Seq[Movie]
  )
  final case class Movie(
      CinemaId: String,
      Title: String,
      movie_times: Seq[MovieTime]
  )
  final case class MovieTime(
      CinemaId: String,
      ScreenName: String,
      SessionAttributesNames: Seq[String],
      Showtime: String // YYYY-MM-DDTHH:mm:ss
  )
