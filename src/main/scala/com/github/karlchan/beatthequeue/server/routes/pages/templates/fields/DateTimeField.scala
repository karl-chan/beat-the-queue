package com.github.karlchan.beatthequeue.server.routes.pages.templates.fields

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import scalatags.Text.all._

final case class DateTimeField(
    override val label: String,
    override val value: Option[LocalDateTime] = None
) extends SingleField[LocalDateTime]:
  override def render: Html =
    div(
      span(cls := "font-semibold", s"$label: "),
      span(
        s"${value.map(_.format(DateTimeFormatter.ofPattern("LLL d h:mm a (EE)"))).getOrElse("N/A")}"
      )
    )
