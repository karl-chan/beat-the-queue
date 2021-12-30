package com.github.karlchan.beatthequeue.server.routes.pages.templates.form

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import scalatags.Text.all._

final case class HiddenInputField(
    override val name: String,
    val value: Option[String]
) extends SingleInputField[String]:
  override val label = ""
  override def render: Html =
    input(
      `type` := "hidden",
      xModel := s"formData.$name",
      attr("name") := name,
      attr("value") := value.get
    )
