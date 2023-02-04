package com.github.karlchan.beatthequeue.server.routes.pages.templates.form

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import scalatags.Text.all._

import java.time.LocalDate

final case class DateInputField(
    override val label: String,
    override val name: String,
    override val value: Option[LocalDate] = None
) extends SingleInputField[LocalDate]:
  override def render: Html =
    div(
      // Label
      div(
        cls := "text-xs text-gray-600 font-bold mb-2",
        label
      ),
      // User input field
      input(
        `type` := "date",
        xModel := s"formData.$name",
        xInit := s"""$$watch('formData.$name', value => {
          if (formData.$name === '') {
            formData.$name = null
          }
        })""",
        cls := "rounded-lg shadow-md mb-2 px-4 py-2 focus:ring-1 focus:ring-gray-400 focus:outline-none",
        attr("name") := name,
        attr("x-on:keydown.enter.prevent") := ""
      )
    )
