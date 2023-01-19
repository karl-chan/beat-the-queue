package com.github.karlchan.beatthequeue.merchants

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Renderer
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.InputField
import fs2.Stream
import io.circe.Decoder
import io.circe.Encoder
import io.circe.HCursor
import io.circe.Json
import mongo4cats.codecs.MongoCodecProvider
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

abstract class Merchant[M, C <: Criteria[M], E <: Event[M]](using
    actualCriteriaEncoder: Encoder[C],
    actualCriteriaDecoder: Decoder[C],
    actualEventEncoder: Encoder[E],
    actualEventDecoder: Decoder[E]
):
  val name: String
  val logoUrl: String
  val eventFinder: EventFinder[M]
  val defaultCriteria: C
  val renderer: Renderer[M, C, E]

  final val criteriaEncoder: Encoder[Criteria[M]] =
    actualCriteriaEncoder.contramap(_.asInstanceOf[C])
  final val criteriaDecoder: Decoder[Criteria[M]] =
    actualCriteriaDecoder.map(identity)
  final val eventEncoder: Encoder[Event[M]] =
    actualEventEncoder.contramap(_.asInstanceOf[E])
  final val eventDecoder: Decoder[Event[M]] =
    actualEventDecoder.map(identity)

trait Event[M]:
  val merchant: String
  val name: String
  val time: LocalDateTime

given Ordering[Event[?]] =
  Ordering.by[Event[?], Long](_.time.toEpochSecond(ZoneOffset.UTC))

trait EventFinder[M]:
  def run(): Stream[IO, Event[M]]

trait Criteria[M]:
  val id: String
  val merchant: String
  def matches(event: Event[M]): Boolean

given Ordering[Criteria[?]] =
  Ordering.by[Criteria[?], String](_.merchant).orElseBy(_.id)

given [M]: Encoder[Criteria[M]] = new {
  final def apply(criteria: Criteria[M]): Json =
    val merchant = Merchants.findMerchantFor(criteria)
    merchant.criteriaEncoder.apply(criteria)
}

given Decoder[Criteria[?]] = new {
  final def apply(c: HCursor): Decoder.Result[Criteria[?]] =
    for {
      name <- c.get[String]("merchant")
      merchant = Merchants.AllByName(name)
      result <- merchant.criteriaDecoder.apply(c)
    } yield result
}

given EntityDecoder[IO, Criteria[?]] = jsonOf[IO, Criteria[?]]

given [M]: Encoder[Event[M]] = new {
  final def apply(event: Event[M]): Json =
    val merchant = Merchants.findMerchantFor(event)
    merchant.eventEncoder.apply(event)
}

given Decoder[Event[?]] = new {
  final def apply(c: HCursor): Decoder.Result[Event[?]] =
    for {
      name <- c.get[String]("merchant")
      merchant = Merchants.AllByName(name)
      result <- merchant.eventDecoder.apply(c)
    } yield result
}
