package com.github.karlchan.beatthequeue.server.routes.pages.templates.inputfields

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import scalatags.Text.all._

import java.util.UUID

case class MultiSelectInputField(
    options: Seq[String],
    override val value: Option[Seq[String]] = None
) extends InputField[Seq[String]]:
  override def render(name: String): Html =
    select(
      attr("name") := name,
      multiple := true,
      for (o <- options) yield option(attr("value") := o, o)
    )
