package com.github.karlchan.beatthequeue.merchants

import cats.effect.IO
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.InputField

trait Merchant[M]:
  def name: String
  def eventFinder: EventFinder[M]
  def matcher: Matcher[M]

trait Event[M]:
  def name: String

trait EventFinder[M]:
  def run(): IO[Seq[Event[M]]]

trait Criteria[M]:
  val fields: Map[String, InputField[?]]

trait Matcher[M]:
  def matches(event: Event[M], criteria: Criteria[M]): Boolean
