package com.github.karlchan.beatthequeue.server.routes.pages.templates.fields

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import scalatags.Text.all._

final case class StringField(
    override val label: String,
    override val value: Option[String] = None
) extends SingleField[String]:
  override def render: Html =
    div(
      span(cls := "font-semibold", s"$label: "),
      span(s"${value.getOrElse("N/A")}")
    )
