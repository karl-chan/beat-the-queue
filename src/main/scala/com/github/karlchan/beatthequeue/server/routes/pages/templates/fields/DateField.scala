package com.github.karlchan.beatthequeue.server.routes.pages.templates.fields

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import scalatags.Text.all._

final case class DateField(
    override val label: String,
    override val value: Option[LocalDate] = None
) extends SingleField[LocalDate]:
  override def render: Html =
    div(
      span(cls := "font-semibold", s"$label: "),
      span(
        s"${value.map(_.format(DateTimeFormatter.ofPattern("LLL d (EE)"))).getOrElse("N/A")}"
      )
    )
