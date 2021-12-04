package com.github.karlchan.beatthequeue.server.routes.pages.templates.form

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import scalatags.Text.all._

import java.time.LocalDateTime

case class DateTimeInputField(
    override val value: Option[LocalDateTime] = None
) extends InputField[LocalDateTime]:
  override def render(name: String): Html =
    input(
      `type` := "datetime-local",
      attr("name") := name
    )
