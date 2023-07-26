package com.github.karlchan.beatthequeue.server.routes.pages.templates.form

import java.util.UUID

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import io.circe.syntax._
import scalatags.Text.all._

final case class MultiAutocompleteInputField(
    override val label: String,
    override val name: String,
    override val value: Seq[String] = Seq.empty,
    val options: Seq[String]
) extends MultiInputField[String]:
  override def render: Html =
    div(
      xData := s"""
      {
        show: false,
        selectedOptions: ${value.asJson}.reduce((acc,curr)=> (acc[curr]=true,acc),{}),
        inputText: '',
        get options() {
          const rawOptions = ${options.asJson}
          const rawOptionsSet = new Set(rawOptions)
          const customOptions = Object.keys(this.selectedOptions).sort().filter(v => !rawOptionsSet.has(v))
          const o = [
            ...customOptions,
            ... rawOptions
          ]
          console.log(o)
          return o
        },
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
      div(
        cls := "relative inline-block",
        // "Click to change" placeholder text
        input(
          `type` := "text",
          cls := "rounded-lg shadow-md mb-2 px-4 py-2 focus:ring-1 focus:ring-gray-400 focus:outline-none",
          xModel := "inputText",
          placeholder := "Click to change",
          attr("x-on:keydown.enter.prevent") :=
            "if (inputText.trim()) { selectedOptions[inputText.trim()] = true }",
          attr("x-on:click") := "show = true",
          attr("x-on:click.outside") := "show = false; inputText = ''"
        ),
        // Clear button
        span(
          cls := "absolute right-0",
          styledButton(
            color = "yellow",
            `type` := "button",
            attr("x-on:click") := "selectedOptions = {}",
            materialIcon("backspace")
          )
        )
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
                materialIcon("check")
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
