package com.github.karlchan.beatthequeue.merchants

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Renderer
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.InputField
import io.circe.Decoder
import io.circe.Encoder
import io.circe.HCursor
import io.circe.Json
import mongo4cats.codecs.MongoCodecProvider
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

import java.util.UUID
import java.time.LocalDateTime

abstract class Merchant[M, C <: Criteria[M]](using
    childEncoder: Encoder[C],
    childDecoder: Decoder[C]
):
  val name: String
  val logoUrl: String
  val eventFinder: EventFinder[M]
  val defaultCriteria: C
  val renderer: Renderer[M, C]

  final val criteriaEncoder: Encoder[Criteria[M]] =
    childEncoder.contramap(_.asInstanceOf[C])
  final val criteriaDecoder: Decoder[Criteria[M]] = childDecoder.map(identity)

trait Event[M]:
  val merchant: String
  val name: String
  val time: LocalDateTime

trait EventFinder[M]:
  def run(): IO[Seq[Event[M]]]

trait Criteria[M]:
  val id: String
  val merchant: String
  def matches(event: Event[M]): Boolean

given [M]: Encoder[Criteria[M]] = new {
  final def apply(criteria: Criteria[M]): Json =
    val merchant = Merchants.findMerchantFor(criteria)
    merchant.criteriaEncoder.apply(criteria)
}

given Decoder[Criteria[_]] = new {
  final def apply(c: HCursor): Decoder.Result[Criteria[_]] =
    for {
      name <- c.get[String]("merchant")
      merchant = Merchants.AllByName(name)
      result <- merchant.criteriaDecoder.apply(c)
    } yield result
}

given EntityDecoder[IO, Criteria[_]] = jsonOf[IO, Criteria[_]]
