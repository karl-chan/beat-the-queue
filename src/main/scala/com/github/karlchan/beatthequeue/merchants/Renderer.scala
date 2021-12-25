package com.github.karlchan.beatthequeue.merchants

import cats.effect.IO
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
import org.http4s.Uri
import scalatags.Text.all._

abstract class Renderer[M, C <: Criteria[M]]:
  final def render(criteria: C)(using encoder: Encoder[C]): Html =
    val url = Uri
      .unsafeFromString("/criteria/edit")
      .withQueryParam(
        "criteria",
        criteria.asJson.toString
      )
      .toString
    card(
      cls := "flex flex-col space-y-2",
      div(
        cls := "flex place-content-end",
        linkButton(
          color = "cyan",
          href := url,
          materialIcon("edit")
        )
      ),
      toFields(criteria)
        .map(_.render)
    )

  final def renderEditor(criteria: C)(using encoder: Encoder[C]): IO[Html] =
    for {
      inputFields <- toInputFields(criteria)
    } yield form(
      cls := "flex flex-col space-y-2",
      method := "POST",
      xData := s"""
      {
        formData: ${criteria.asJson}
      }
      """,
      attr("x-on:submit.prevent") := """
        fetch("/api/user/criteria", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(formData)
        })
        .then(res => {
          if (!res.ok) {
            alert("Failed to submit criteria. See console for error.")
            console.error(res)
          } else {
            window.location.href = "/"
          }
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

object Renderer:
  def renderCatalog(): Html =
    div(
      cls := "flex flex-col space-y-2",
      Merchants.All
        .map((category, merchants) =>
          div(
            div(cls := "text-4xl text-gray-800 font-semibold", category),
            verticalGap(4),
            div(
              cls := "grid grid-cols-4 gap-4",
              merchants
                .map(merchant =>
                  val url = Uri
                    .unsafeFromString("/criteria/edit")
                    .withQueryParam(
                      "criteria",
                      merchant.criteriaEncoder
                        .apply(merchant.defaultCriteria)
                        .toString
                    )
                    .toString
                  linkButton(
                    color = "gray",
                    href := url,
                    img(src := merchant.logoUrl, alt := merchant.name)
                  )
                )
            )
          )
        )
        .toSeq
    )
