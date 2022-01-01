package com.github.karlchan.beatthequeue.server.routes.pages.templates.fields

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import scalatags.Text.all._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

final case class DateTimeField(
    override val label: String,
    override val value: Option[LocalDateTime] = None
) extends SingleField[LocalDateTime]:
  override def render: Html =
    div(
      span(cls := "font-semibold", s"$label: "),
      span(s"${value.map(_.format(Formatter)).getOrElse("N/A")}")
    )

private val Formatter = DateTimeFormatter.ofPattern("LLL d h:mm a (EE)");
