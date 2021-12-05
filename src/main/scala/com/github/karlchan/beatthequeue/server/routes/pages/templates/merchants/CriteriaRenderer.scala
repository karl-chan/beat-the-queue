package com.github.karlchan.beatthequeue.server.routes.pages.templates.merchants

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import scalatags.Text.all._

object CriteriaRenderer:
  def render[M](criteria: Criteria[M]): Html = div(
    cls := "flex flex-col space-y-2",
    criteria.fields
      .map((fieldName, fieldValue) =>
        div(
          cls := "flex place-content-between",
          span(cls := "text-bold", s"$fieldName:"),
          span(fieldValue.toString)
        )
      )
      .toSeq
  )

  def renderForm[M](merchant: Merchant[M]): IO[Html] =
    for {
      builder <- merchant.criteriaBuilder()
    } yield form(
      action := "/criteria/create",
      method := "POST",
      input(`type` := "hidden", name := "merchant", value := merchant.name),
      builder.fields
        .map((fieldName, inputField) => inputField.render(fieldName))
        .toSeq,
      styledButton(color = "green", `type` := "submit", "Submit")
    )
