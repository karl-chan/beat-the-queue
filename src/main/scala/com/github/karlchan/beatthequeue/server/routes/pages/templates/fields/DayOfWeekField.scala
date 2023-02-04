package com.github.karlchan.beatthequeue.server.routes.pages.templates.fields

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import scalatags.Text.all._

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

final case class DayOfWeekField(
    override val label: String,
    override val value: Seq[DayOfWeek]
) extends MultiField[DayOfWeek]:
  override def render: Html =
    div(
      cls := "flex flex-wrap space-x-2",
      span(cls := "font-semibold", s"$label:"),
      span(
        if value.nonEmpty then
          value
            .map(_.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
            .mkString(", ")
        else "N/A"
      )
    )
