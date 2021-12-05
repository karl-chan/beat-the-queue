package com.github.karlchan.beatthequeue.server.routes.pages.merchants.cineworld

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.CineworldCriteria
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.DateTimeInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.MultiSelectInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.DateTimeField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.MultiStringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import scalatags.Text.all._

object CineworldRenderer:
  def renderCriteria(criteria: CineworldCriteria): Html =
    div(
      cls := "flex flex-col space-y-2",
      MultiStringField(
        label = "Film names",
        value = criteria.names
      ).render,
      DateTimeField(
        label = "Start time",
        value = criteria.startTime
      ).render,
      DateTimeField(
        label = "End time",
        value = criteria.endTime
      ).render,
      MultiStringField(
        label = "Venues",
        value = criteria.venues
      ).render,
      MultiStringField(
        label = "Screen types",
        value = criteria.screenTypes
      ).render
    )

  def renderForm[M](
      names: Seq[String],
      venues: Seq[String],
      screenTypes: Seq[String]
  ): Html =
    form(
      action := "/criteria/create",
      method := "POST",
      input(`type` := "hidden", name := "merchant", value := "cineworld"),
      MultiSelectInputField(
        label = "Film names",
        name = "names",
        options = names
      ).render,
      DateTimeInputField(label = "Start time", name = "startTime").render,
      DateTimeInputField(label = "End time", name = "endTime").render,
      MultiSelectInputField(
        label = "Venues",
        name = "venues",
        options = venues
      ).render,
      MultiSelectInputField(
        label = "Screen types",
        name = "screenTypes",
        options = screenTypes
      ).render,
      styledButton(color = "green", `type` := "submit", "Submit")
    )
