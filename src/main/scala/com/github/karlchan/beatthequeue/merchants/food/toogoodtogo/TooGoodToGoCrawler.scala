package com.github.karlchan.beatthequeue.merchants.food.toogoodtogo

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.github.karlchan.beatthequeue.merchants.EventFinder
import com.github.karlchan.beatthequeue.util.Http
import com.github.karlchan.beatthequeue.util.Properties
import com.github.karlchan.beatthequeue.util.given_HttpConnection
import fs2.Stream
import io.circe.generic.auto.deriveDecoder
import io.circe.generic.auto.deriveEncoder
import sttp.model.Uri
import sttp.model.Uri.UriContext

final class TooGoodToGoCrawler extends EventFinder[TooGoodToGo]:
  private val http =
    Http(
      persistCookies = true,
      userAgent = Some(Properties.get("toogoodtogo.userAgent"))
    )

  override def run(): Stream[IO, TooGoodToGoEvent] =
    for {
      token <- Stream.eval(getToken())
      item <- Stream.evalSeq(getFavourites(token))
      if item.items_available > 0
      interval <- Stream.fromOption(item.pickup_interval)
    } yield TooGoodToGoEvent(
      name = item.display_name,
      time = OffsetDateTime
        .parse(interval.start, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        .atZoneSameInstant(ZoneId.of("Europe/London"))
        .toLocalDateTime
    )

  final case class Info(
      names: Seq[String]
  )
  def getInfo(): IO[Info] = for {
    token <- getToken()
    items <- getFavourites(token)
  } yield Info(
    names = items.map(_.display_name).sorted.distinct
  )

  private[toogoodtogo] def getFavourites(
      token: Token
  ): IO[Seq[FavouriteResponse.Item]] =
    for {
      body <-
        http.postJson[Request.Favourites, FavouriteResponse.Body](
          uri"https://apptoogoodtogo.com/api/discover/v1/bucket",
          Request.Favourites(),
          headers = Map("Authorization" -> s"Bearer ${token.accessToken}")
        )
    } yield (body.mobile_bucket.items)

  private[toogoodtogo] def getToken(): IO[Token] = {
    for {
      body <- http.postJson[Request.RefreshToken, TokenResponse.Body](
        uri"https://apptoogoodtogo.com/api/token/v1/refresh",
        Request.RefreshToken(refresh_token =
          Properties.get("toogoodtogo.refreshToken")
        )
      )
    } yield Token(body.access_token)
  }

  final private[toogoodtogo] case class Token(
      accessToken: String
  )

private[toogoodtogo] object TokenResponse:
  final case class Body(
      access_token: String,
      access_token_ttl_seconds: Int,
      refresh_token: String
  )

private[toogoodtogo] object Request:
  final case class RefreshToken(
      refresh_token: String
  )

  final case class Favourites(
      origin: Origin = Origin(),
      radius: Int = 21,
      paging: Paging = Paging(),
      bucket: Bucket = Bucket()
  )

  final case class Origin(
      latitude: Double = 0.0,
      longitude: Double = 0.0
  )

  final case class Paging(
      page: Int = 0,
      size: Int = 50
  )

  final case class Bucket(
      filler_type: String = "Favorites"
  )

private[toogoodtogo] object FavouriteResponse:
  final case class Body(
      mobile_bucket: MobileBucket
  )

  final case class MobileBucket(
      items: Seq[Item]
  )

  final case class Item(
      display_name: String,
      pickup_interval: Option[PickupInterval],
      items_available: Int
  )

  final case class PickupInterval(
      start: String,
      end: String
  )
