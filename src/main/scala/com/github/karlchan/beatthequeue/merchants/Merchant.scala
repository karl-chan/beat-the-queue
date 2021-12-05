package com.github.karlchan.beatthequeue.merchants

import cats.effect.IO
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.InputField

trait Merchant[M]:
  val name: String
  val eventFinder: EventFinder[M]
  val matcher: Matcher[M]
  def criteriaTemplate(): IO[Criteria[M]]

trait Event[M]:
  val name: String

trait EventFinder[M]:
  def run(): IO[Seq[Event[M]]]

trait Criteria[M]:
  val fields: Map[String, InputField[?]]

trait Matcher[M]:
  def matches(event: Event[M], criteria: Criteria[M]): Boolean
