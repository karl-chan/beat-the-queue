package com.github.karlchan.beatthequeue.server.routes.pages.templates.fields

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import scalatags.Text.all._

final case class TimeField(
    override val label: String,
    override val value: Option[LocalTime] = None
) extends SingleField[LocalTime]:
  override def render: Html =
    div(
      span(cls := "font-semibold", s"$label: "),
      span(
        s"${value.map(_.format(DateTimeFormatter.ofPattern("h:mm a"))).getOrElse("N/A")}"
      )
    )
