package com.github.karlchan.beatthequeue.server.routes.pages.templates.form

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import io.circe.generic.auto._
import io.circe.syntax._
import scalatags.Text.all._

import java.time.LocalDateTime

final case class MultiStringInputField(
    override val label: String,
    override val name: String,
    override val value: Seq[String] = Seq.empty,
    val inputType: String = "text"
) extends MultiInputField[String]:
  override def render: Html =
    div(
      xData := s"""
      {
        values: ${value.asJson},
      }
      """,
      xInit := s"$$watch('values', values => formData.$name = values)",
      // Label
      div(
        cls := "text-xs text-gray-600 font-bold mb-2",
        label
      ),
      // User input fields
      div(
        cls := "flex flex-col",
        template(
          xFor := "(value, index) in values",
          div(
            cls := "flex items-center",
            input(
              `type` := inputType,
              xModel := s"values[index]",
              cls := "rounded-lg shadow-md mb-2 px-4 py-2 focus:ring-1 focus:ring-gray-400 focus:outline-none",
              attr("x-on:keydown.enter.prevent") := ""
            ),
            styledButton(
              color = "red",
              `type` := "button",
              attr("x-on:click") := "values.splice(index, 1)",
              materialIcon("delete")
            )
          )
        ),
        div(
          cls := "flex",
          styledButton(
            color = "blue",
            `type` := "button",
            attr("x-on:click") := "values.push('')",
            "Add"
          )
        )
      )
    )
