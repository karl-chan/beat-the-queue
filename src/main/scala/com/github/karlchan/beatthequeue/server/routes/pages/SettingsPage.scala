package com.github.karlchan.beatthequeue.server.routes.pages

import cats.effect.IO
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.MultiStringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Models
import org.http4s.Uri
import scalatags.Text.all._

object SettingsPage:
  def render(authUser: AuthUser)(using db: Db): IO[Html] =
    for {
      dbUser <- db.findUser(authUser)
    } yield Template.styledPageWithNav(
      div(
        cls := "flex flex-col space-y-2",
        div(
          cls := "flex justify-between",
          h1(cls := "text-4xl text-gray-800 font-bold", "Settings"),
          renderEditButton
        ),
        vspace(4),
        renderNotificationSettings(dbUser)
      )
    )

  private def renderEditButton: Html =
    linkButton(
      color = "cyan",
      href := Uri
        .unsafeFromString("/settings/edit")
        .toString,
      materialIcon("edit")
    )

  private def renderNotificationSettings(dbUser: Models.User): Html =
    div(
      cls := "flex flex-col",
      h2(cls := "text-2xl", "Notifications"),
      MultiStringField(
        label = "Email",
        value = dbUser.notificationSettings.emailAddresses
      ).render
    )
