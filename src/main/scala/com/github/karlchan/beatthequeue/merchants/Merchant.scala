package com.github.karlchan.beatthequeue.merchants

import cats.effect.IO
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.InputField
import io.circe.Decoder
import io.circe.Encoder

import java.util.UUID

trait Merchant[M]:
  val name: String
  val eventFinder: EventFinder[M]
  val codecs: Codecs[M]

trait Event[M]:
  val name: String

trait EventFinder[M]:
  def run(): IO[Seq[Event[M]]]

trait Criteria[M]:
  val id: String
  val merchant: String
  def matches(event: Event[M]): Boolean

trait Codecs[M]:
  val criteriaEncoder: Encoder[Criteria[M]]
  val criteriaDecoder: Decoder[Criteria[M]]
