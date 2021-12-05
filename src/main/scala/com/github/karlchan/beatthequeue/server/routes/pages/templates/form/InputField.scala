package com.github.karlchan.beatthequeue.server.routes.pages.templates.form

import cats.effect.IO
import com.github.karlchan.beatthequeue.server.routes.pages.Html

trait InputField[V]:
  val label: String
  val value: Option[V]
  def render(name: String): Html
