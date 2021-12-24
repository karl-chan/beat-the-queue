package com.github.karlchan.beatthequeue.server.routes.pages.templates.form

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import io.circe.syntax._
import scalatags.Text.all._

import java.util.UUID

final case class MultiSelectInputField(
    override val label: String,
    override val name: String,
    override val value: Option[Seq[String]] = None,
    val options: Seq[String]
) extends InputField[Seq[String]]:
  override def render: Html =
    div(
      xData := s"""
      {
        show: false,
        options: ${options.asJson},
        selectedOptions: ${value
        .getOrElse(Seq.empty)
        .asJson}.reduce((acc,curr)=> (acc[curr]=true,acc),{}),
        inputText: ''
      }
      """,
      xInit := s"$$watch('selectedOptions', value => formData.$name = Object.keys(value))",
      cls := "relative inline-block",
      // Label
      div(
        cls := "text-xs text-gray-600 font-bold mb-2",
        label
      ),
      // User input field
      input(
        `type` := "text",
        cls := "rounded-lg shadow-md mb-2 px-4 py-2 focus:ring-1 focus:ring-gray-400 focus:outline-none",
        xModel := "inputText",
        placeholder := "Click to change",
        attr("x-on:click") := "show = true",
        attr("x-on:click.outside") := "show = false; inputText = ''"
      ),
      // Dropdown
      div(
        xShow := "show",
        cls := "absolute inset-x-0 flex flex-col bg-gray-100 shadow-md rounded-lg divide-y-2 z-10",
        template(
          xFor := "option in options",
          template(
            xIf := "option.toLowerCase().includes(inputText.toLowerCase())",
            button(
              `type` := "button",
              cls := "flex place-content-between px-4 py-2 rounded-lg hover:bg-gray-200 z-20",
              xClass := "option in selectedOptions? 'bg-gray-300': 'bg-gray-100'",
              attr("x-on:click.stop") := """option in selectedOptions?
                                              delete selectedOptions[option]:
                                              selectedOptions[option] = true""",
              span(xText := "option"),
              template(
                xIf := "option in selectedOptions",
                span(cls := "material-icons", "check")
              )
            )
          )
        )
      ),
      // Selected options as text
      div(
        cls := "text-xs text-gray-600 italic",
        xText := "Object.keys(selectedOptions).join(', ')"
      )
    )
