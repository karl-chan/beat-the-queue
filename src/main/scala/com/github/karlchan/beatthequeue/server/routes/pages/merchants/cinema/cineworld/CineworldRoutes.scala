package com.github.karlchan.beatthequeue.server.routes.pages.merchants.cinema.cineworld

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.CineworldCrawler
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.CineworldCriteria
import com.github.karlchan.beatthequeue.server.auth.Auth
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.github.karlchan.beatthequeue.server.routes.given_EntityEncoder_IO_TypedTag
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.DateTimeField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.MultiStringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.DateTimeInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.HiddenInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.MultiSelectInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.given_Db
import mongo4cats.collection.operations.Filter
import org.http4s.HttpRoutes
import org.http4s._
import org.http4s.dsl.io._
import scalatags.Text.all._
import tsec.authentication.TSecAuthService
import tsec.authentication.asAuthed

import java.util.UUID

private object Fields:
  val Id = "id"
  val Name = "name"
  val StartTime = "startTime"
  val EndTime = "endTime"
  val Venues = "venues"
  val ScreenTypes = "screenTypes"

val cineworldRoutes: HttpRoutes[IO] =
  Auth.service(TSecAuthService {
    case secured @ GET -> Root / "criteria" / "edit" asAuthed user =>
      for {
        info <- CineworldCrawler().getInfo()
        form = renderCriteriaForm(info)
        res <- Ok(form)
      } yield res
    case secured @ POST -> Root / "criteria" / "edit" asAuthed user =>
      secured.request.decode[UrlForm] { m =>
        Ok()
      }
  })

def renderCineworldCriteria(criteria: CineworldCriteria): Html =
  div(
    cls := "flex flex-col space-y-2",
    MultiStringField(
      label = "Film names",
      value = criteria.filmNames
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

private def renderCriteriaForm(info: CineworldCrawler#Info): Html =
  form(
    action := "/merchants/cineworld/criteria/edit",
    method := "POST",
    HiddenInputField(
      name = Fields.Id,
      value = Some(UUID.randomUUID.toString)
    ).render,
    MultiSelectInputField(
      label = "Film names",
      name = Fields.Name,
      options = info.names
    ).render,
    DateTimeInputField(label = "Start time", name = Fields.StartTime).render,
    DateTimeInputField(label = "End time", name = Fields.EndTime).render,
    MultiSelectInputField(
      label = "Venues",
      name = Fields.Venues,
      options = info.venues
    ).render,
    MultiSelectInputField(
      label = "Screen types",
      name = Fields.ScreenTypes,
      options = info.screenTypes
    ).render,
    styledButton(color = "green", `type` := "submit", "Submit")
  )
