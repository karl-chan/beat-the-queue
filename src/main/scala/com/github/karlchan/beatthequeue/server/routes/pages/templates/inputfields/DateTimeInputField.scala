package com.github.karlchan.beatthequeue.server.routes.pages.templates.inputfields

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import java.time.LocalDateTime
import scalatags.Text.all._

case class DateTimeInputField(
    override val value: Option[LocalDateTime] = None
) extends InputField[LocalDateTime]:
  override def render(name: String): Html =
    input(
      `type` := "datetime-local",
      attr("name") := name
    )
