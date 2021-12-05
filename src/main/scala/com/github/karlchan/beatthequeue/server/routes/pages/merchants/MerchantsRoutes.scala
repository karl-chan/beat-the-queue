package com.github.karlchan.beatthequeue.server.routes.pages.merchants

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.server.auth.Auth
import com.github.karlchan.beatthequeue.server.routes.given_EntityEncoder_IO_TypedTag
import com.github.karlchan.beatthequeue.server.routes.pages.Html
import com.github.karlchan.beatthequeue.server.routes.pages.Template
import com.github.karlchan.beatthequeue.server.routes.pages.merchants.cinema.cineworld.cineworldRoutes
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import org.http4s.HttpRoutes
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server.Router
import scalatags.Text.all._
import tsec.authentication.TSecAuthService
import tsec.authentication.asAuthed

private val privateRoutes: HttpRoutes[IO] =
  Auth.service(TSecAuthService {
    case GET -> Root / "criteria" asAuthed user =>
      Ok(userCriteriaPage)
    case GET -> Root / "criteria" / "builder" asAuthed user =>
      Ok(criteriaBuilderCatalogPage)
  })

val merchantsRoutes = Router(
  "/cineworld" -> cineworldRoutes,
  "" -> privateRoutes
)

private def userCriteriaPage: Html =
  Template.styledPage(
    div(
      cls := "flex flex-col space-y-2",
      Merchants.All
        .map((category, merchants) =>
          div(
            h1(category),
            div(
              cls := "flex flex-wrap space-x-1",
              merchants
                .map(merchant =>
                  linkButton(
                    "gray",
                    href := s"/merchants/$category/criteria",
                    merchant.name
                  )
                )
                .toSeq
            )
          )
        )
        .toSeq
    )
  )

private def criteriaBuilderCatalogPage: Html =
  Template.styledPage(
    div(
      cls := "flex flex-col space-y-2",
      Merchants.All
        .map((category, merchants) =>
          div(
            h1(category),
            div(
              cls := "flex flex-wrap space-x-1",
              merchants
                .map(merchant =>
                  linkButton(
                    "gray",
                    href := s"/merchants/$category/criteria/builder",
                    merchant.name
                  )
                )
                .toSeq
            )
          )
        )
        .toSeq
    )
  )
