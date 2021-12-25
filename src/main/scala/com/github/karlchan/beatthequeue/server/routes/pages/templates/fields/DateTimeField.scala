package com.github.karlchan.beatthequeue.server.routes.pages.templates.fields

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import scalatags.Text.all._

import java.time.LocalDateTime

final case class DateTimeField(
    override val label: String,
    override val value: Option[LocalDateTime] = None
) extends Field[LocalDateTime]:
  override def render: Html =
    div(
      span(cls := "font-semibold", s"$label: "),
      span(s"${value.getOrElse("N/A")}")
    )
