package com.github.karlchan.beatthequeue.server.routes.pages

import cats.effect.IO
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.MultiStringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.MultiStringInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import com.github.karlchan.beatthequeue.server.routes.pages.templates.{
  form => _,
  _
}
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Models
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.Uri
import scalatags.Text.all._

object SettingsEditPage:
  def render(authUser: AuthUser)(using db: Db): IO[Html] =
    for {
      dbUser <- db.findUser(authUser)
    } yield Template.styledPageWithNav(
      form(
        cls := "flex flex-col space-y-2",
        method := "POST",
        xData := s"""
        {
          formData: {
            emailAddresses: ${dbUser.notificationSettings.emailAddresses.asJson}
          },
          submit() {
            fetch("/api/user/settings", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify(this.formData)
            })
            .then(res => {
              if (!res.ok) {
                alert("Failed to submit settings. See console for error.")
                console.error(res)
              } else {
                window.location.href = "/settings"
              }
            })
          }
        }
        """,
        attr("x-on:submit.prevent") := "submit()",
        MultiStringInputField(
          label = "Email",
          name = "emailAddresses",
          value = dbUser.notificationSettings.emailAddresses,
          inputType = "email"
        ).render,
        styledButton(
          color = "green",
          `type` := "submit",
          "Submit"
        )
      )
    )
