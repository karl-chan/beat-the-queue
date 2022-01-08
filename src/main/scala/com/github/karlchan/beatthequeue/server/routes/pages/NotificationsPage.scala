package com.github.karlchan.beatthequeue.server.routes.pages

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.merchants.Renderer
import com.github.karlchan.beatthequeue.merchants.given_Encoder_Event
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Models
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.Uri
import scalatags.Text.all._

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object NotificationsPage:
  def render(authUser: AuthUser)(using db: Db): IO[Html] =
    for {
      dbUser <- db.findUser(authUser)
      groupedNotifications = dbUser.notifications
        .filterNot(_.hidden)
        .groupBy(_.published)
        .toSeq
        .sortBy((published: LocalDateTime, _) =>
          published.toEpochSecond(ZoneOffset.UTC)
        )
        .reverse
    } yield Template.styledPageWithNav(
      div(
        cls := "flex flex-col space-y-2",
        h1(cls := "text-4xl text-gray-800 font-bold", "Upcoming events"),
        vspace(4),
        if groupedNotifications.isEmpty then
          div(cls := "mx-auto text-gray", "You have no upcoming events.")
        else
          groupedNotifications.map((published, notifications) =>
            div(
              div(
                cls := "flex items-center space-x-2",
                li(
                  cls := "italic",
                  s"Last found ${published.format(Formatter)}."
                ),
                linkButton(
                  "yellow",
                  href := Uri
                    .unsafeFromString("/notifications/hide")
                    .withQueryParam("published", published.toString)
                    .toString,
                  materialIcon("visibility_off")
                )
              ),
              div(
                cls := "grid grid-cols-4 gap-4",
                notifications
                  .sortBy(_.event.time.toEpochSecond(ZoneOffset.UTC))
                  .map(notification =>
                    val merchant =
                      Merchants
                        .AllByName(notification.event.merchant)
                    merchant.renderer.render(
                      notification
                    )
                  )
              )
            )
          )
      )
    )

private val Formatter = DateTimeFormatter.ofPattern("LLL d h:mm a (EE)");
