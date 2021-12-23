package com.github.karlchan.beatthequeue.server.routes.pages.merchants

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.Field
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.InputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import com.github.karlchan.beatthequeue.server.routes.pages.templates.{
  form => _,
  _
}
import com.github.karlchan.beatthequeue.util.Reflection
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import scalatags.Text.all._

abstract class Renderer[M, C <: Criteria[M]]:
  final def render(criteria: C): Html =
    div(
      cls := "flex flex-col space-y-2",
      toFields(criteria).map(_.render)
    )

  final def renderEditor(criteria: C): IO[Html] =
    for {
      inputFields <- toInputFields(criteria)
      formData = inputFields.map(field => (field.name, None)).toMap.asJson
    } yield form(
      cls := "flex flex-col space-y-2",
      method := "POST",
      xData := s"""
      {
        formData: $formData
      }
      """,
      attr("x-on:submit.prevent") := s""""
        fetch("/api/user/criteria", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(this.formData)
        })
        .then(() => {
          window.location.href = "/"
        })
        .catch((err) => {
          alert("Failed to submit criteria. See console for error.")
          console.error(err)
        })
      """,
      inputFields.map(_.render),
      styledButton(
        color = "green",
        `type` := "submit",
        "Submit"
      )
    )

  def toFields(criteria: C): Seq[Field[_]]
  def toInputFields(criteria: C): IO[Seq[InputField[_]]]
