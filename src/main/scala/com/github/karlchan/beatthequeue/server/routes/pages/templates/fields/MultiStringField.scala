package com.github.karlchan.beatthequeue.server.routes.pages.templates.fields

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import scalatags.Text.all._

final case class MultiStringField(
    override val label: String,
    override val value: Option[Seq[String]]
) extends Field[Seq[String]]:
  override def render: Html =
    div(
      cls := "flex flex-wrap space-x-2",
      span(cls := "font-semibold", s"$label:"),
      span(value.map(_.mkString(", ")).getOrElse("N/A"))
    )
