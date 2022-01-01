package com.github.karlchan.beatthequeue.merchants

import cats.effect.IO
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.Field
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.MultiStringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.InputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import com.github.karlchan.beatthequeue.server.routes.pages.templates.{
  form => _,
  _
}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s.Uri
import scalatags.Text.all._

abstract class Renderer[M, C <: Criteria[M], E <: Event[M]]:
  final def render(criteria: C)(using encoder: Encoder[C]): Html =
    val merchant = Merchants.findMerchantFor(criteria)
    card(
      cls := "flex flex-col space-y-2 max-w-md",
      div(
        cls := "flex place-content-between space-x-2",
        img(
          cls := "bg-gray-200",
          src := merchant.logoUrl,
          alt := merchant.name
        ),
        linkButton(
          color = "cyan",
          href := Uri
            .unsafeFromString("/criteria/edit")
            .withQueryParam("criteria", criteria.asJson.toString)
            .toString,
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
        formData: ${criteria.asJson},
        submit() {
          fetch("/api/user/criteria", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(this.formData)
          })
          .then(res => {
            if (!res.ok) {
              alert("Failed to submit criteria. See console for error.")
              console.error(res)
            } else {
              window.location.href = "/"
            }
          })
        },
        deleteCriteria() {
          fetch("/api/user/criteria?id=" + this.formData.id, {
            method: "DELETE",
          })
          .then(res => {
            if (!res.ok) {
              alert("Failed to delete criteria. See console for error.")
              console.error(res)
            } else {
              window.location.href = "/"
            }
          })
        },
      }
      """,
      attr("x-on:submit.prevent") := "submit()",
      inputFields.map(_.render),
      styledButton(
        color = "green",
        `type` := "submit",
        "Submit"
      ),
      styledButton(
        color = "red",
        `type` := "button",
        attr("x-on:click") := "deleteCriteria()",
        "Delete"
      )
    )

  final def render(event: E): Html =
    val merchant = Merchants.findMerchantFor(event)
    card(
      cls := "flex flex-col space-y-2 max-w-md",
      toFields(event).map(_.render)
    )

  def toFields(criteria: C): Seq[Field]
  def toInputFields(criteria: C): IO[Seq[InputField]]
  def toFields(event: E): Seq[Field]

object Renderer:
  def renderCatalog(): Html =
    div(
      cls := "flex flex-col space-y-2",
      Merchants.All
        .map((category, merchants) =>
          // Row of category
          div(
            h1(cls := "text-4xl text-gray-800 font-semibold", category),
            vspace(4),
            // Grid of merchants
            div(
              cls := "grid grid-cols-4 gap-4",
              merchants
                .map(merchant =>
                  linkButton(
                    color = "gray",
                    href := Uri
                      .unsafeFromString("/criteria/edit")
                      .withQueryParam(
                        "criteria",
                        merchant.criteriaEncoder
                          .apply(merchant.defaultCriteria)
                          .toString
                      )
                      .toString,
                    img(src := merchant.logoUrl, alt := merchant.name)
                  )
                )
            )
          )
        )
        .toSeq
    )
