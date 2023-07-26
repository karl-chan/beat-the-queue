package com.github.karlchan.beatthequeue.merchants.cinema.thecinema

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

import scala.jdk.CollectionConverters._

import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.EventFinder
import com.github.karlchan.beatthequeue.util.Http
import com.github.karlchan.beatthequeue.util.given_HttpConnection
import fs2.Stream
import org.jsoup.Jsoup
import sttp.client3._
import sttp.model.Uri

final class TheCinemaCrawler() extends EventFinder[TheCinema]:
  private val http = Http(persistCookies = true)

  override def run(): Stream[IO, TheCinemaEvent] =
    for {
      branch <- Stream.emits(Branch.values)
      filmEvent <- Stream.evalSeq(getFilmEvents(branch))
    } yield TheCinemaEvent(
      name = filmEvent.name,
      time = filmEvent.startTime,
      venue = branch.venue,
      screenType = filmEvent.screenType.description
    )

  final case class Info(
      names: Seq[String],
      venues: Seq[String],
      screenTypes: Seq[String]
  )
  def getInfo(): IO[Info] =
    for {
      allFilmEvents <- Branch.values.toVector.parUnorderedFlatTraverse(
        getFilmEvents(_)
      )
    } yield Info(
      names = allFilmEvents.map(_.name).sorted.distinct,
      venues = Branch.values.map(_.venue),
      screenTypes = ScreenType.values.map(_.description)
    )

  final private[thecinema] case class FilmEvent(
      name: String,
      startTime: LocalDateTime,
      screenType: ScreenType
  )

  final private[thecinema] case class Token()

  private[thecinema] def getFilmEvents(branch: Branch): IO[Vector[FilmEvent]] =
    def parseDate(s: String): LocalDate =
      val split = s.split(" ")
      val day = split(1).replaceAll("[^0-9]", "").toInt
      val month = DateTimeFormatter
        .ofPattern("MMMM")
        .parse(split(2))
        .get(ChronoField.MONTH_OF_YEAR)
      val year =
        if month >= LocalDate.now().getMonthValue() then
          LocalDate.now().getYear()
        else LocalDate.now().getYear() + 1
      LocalDate.of(year, month, day)

    def parseTime(s: String): LocalTime =
      LocalTime.parse(
        s.split(' ').head, // Hack to strip out "Last Few"
        DateTimeFormatter.ofPattern("HH:mm")
      )

    def toScreenType(
        is3D: Boolean,
        isDolbyCinema: Boolean,
        isDolbyAtmos: Boolean
    ): ScreenType =
      (is3D, isDolbyCinema, isDolbyAtmos) match {
        case (false, false, false) => ScreenType.Regular
        case (true, false, false)  => ScreenType.ThreeD
        case (false, true, false)  => ScreenType.DolbyCinema
        case (false, false, true)  => ScreenType.DolbyAtmos
        case (true, true, false)   => ScreenType.DolbyCinema3D
        case (true, false, true)   => ScreenType.DolbyAtmos3D
        case _ =>
          throw IllegalArgumentException(
            s"Unknown combination - {is3D: $is3D, isDolbyCinema: $isDolbyCinema, isDolbyAtmos: $isDolbyAtmos}"
          )
      }

    for {
      token <- getToken(branch)
      html <- http.getHtml(branch.whatsOnUrl)

      dateSchedule = Jsoup
        .parse(html)
        .getElementsByAttributeValue("id", "listingsByDate")

      events = for {
        listingsByDate <- dateSchedule.iterator().asScala
        filmDate = parseDate(
          listingsByDate.getElementsByClass("filmlistDate").first().text()
        )
        filmNames = listingsByDate
          .getElementsByClass("text-decoration-none")
          .iterator()
          .asScala
          .map(_.text().trim())
        filmTimesByName = listingsByDate
          .select("ul.d-flex")
          .iterator()
          .asScala
          .map(
            _.select("li.me-3.pb-2")
              .iterator()
              .asScala
              .map(badge =>
                val time = parseTime(badge.text().trim())
                val is3D =
                  !badge.getElementsByClass("bi-badge-3d-fill").isEmpty()
                val isDolbyCinema =
                  !badge.select("img[alt='Dolby Cinema']").isEmpty()
                val isDolbyAtmos =
                  !badge.select("img[alt='Dolby Atmos']").isEmpty()
                val screenType = toScreenType(is3D, isDolbyCinema, isDolbyAtmos)
                (time, screenType)
              )
          )
        events = (filmNames zip filmTimesByName)
          .flatMap((filmName, filmTimes) =>
            filmTimes.map(filmTime =>
              FilmEvent(
                name = filmName,
                startTime = LocalDateTime.of(filmDate, filmTime._1),
                screenType = filmTime._2
              )
            )
          )
        event <- events
      } yield event
    } yield events.toVector

  private def getToken(branch: Branch): IO[Token] = {
    for {
      _ <- http.getHtml(branch.siteUrl)
      _ <- http.postHtml(
        branch.ajaxSetViewSessionUrl,
        Map("viewSession" -> "date")
      )
    } yield Token()
  }

private[thecinema] enum ScreenType(val description: String):
  case Regular extends ScreenType("2D")
  case ThreeD extends ScreenType("3D")
  case DolbyCinema extends ScreenType("Dolby Cinema")
  case DolbyAtmos extends ScreenType("Dolby Atmos")
  case DolbyCinema3D extends ScreenType("Dolby Cinema 3D")
  case DolbyAtmos3D extends ScreenType("Dolby Atmos 3D")

private[thecinema] enum Branch(
    val venue: String,
    val siteUrl: Uri,
    val whatsOnUrl: Uri,
    val ajaxSetViewSessionUrl: Uri
):
  case PowerStation
      extends Branch(
        venue = "The Cinema in the Power Station",
        siteUrl = uri"https://www.thecinemainthepowerstation.com/",
        whatsOnUrl =
          uri"https://www.thecinemainthepowerstation.com/whats-on-in-the-powerstation",
        ajaxSetViewSessionUrl =
          uri"https://www.thecinemainthepowerstation.com/ajax/ajaxSetViewSession.php"
      )
  case Arches
      extends Branch(
        venue = "The Cinema in the Arches",
        siteUrl = uri"https://www.thecinemainthepowerstation.com/",
        whatsOnUrl =
          uri"https://www.thecinemainthepowerstation.com/whats-on-in-the-arches",
        ajaxSetViewSessionUrl =
          uri"https://www.thecinemainthepowerstation.com/ajax/ajaxSetViewSession.php"
      )
  case Selfridges
      extends Branch(
        venue = "The Cinema at Selfridges",
        siteUrl = uri"https://www.thecinemaatselfridges.com/",
        whatsOnUrl = uri"https://www.thecinemaatselfridges.com/whats-on",
        ajaxSetViewSessionUrl =
          uri"https://www.thecinemaatselfridges.com/ajax/ajaxSetViewSession.php"
      )
