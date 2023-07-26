package com.github.karlchan.beatthequeue.server.routes.pages.templates.form

import java.time.DayOfWeek
import java.util.UUID

import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import io.circe.syntax._
import scalatags.Text.all._

final case class DayOfWeekInputField(
    override val label: String,
    override val name: String,
    override val value: Seq[DayOfWeek] = Seq.empty
) extends MultiInputField[DayOfWeek]:

  private val numericValues: Vector[Int] = value.map(_.getValue() % 7).toVector

  override def render: Html =
    div(
      xData := s"""
      {
        show: false,
        options: ['S', 'M', 'T', 'W', 'T', 'F', 'S'],
        selectedOptions: ${numericValues.asJson}.reduce((acc,curr)=> (acc[curr]=true,acc),{}),
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
      // Horizontal circular buttons
      div(
        cls := "flex gap-4",
        template(
          xFor := "(option, index) in options",
          button(
            `type` := "button",
            cls := "rounded-full w-12 h-12 ring-2 hover:ring-4",
            xClass := """index in selectedOptions?
                            'bg-sky-800 text-slate-50':
                            'bg-slate-50 text-sky-800'""",
            xText := "option",
            attr("x-on:click.stop") := """index in selectedOptions?
                                              delete selectedOptions[index]:
                                              selectedOptions[index] = true"""
          )
        )
      )
    )
