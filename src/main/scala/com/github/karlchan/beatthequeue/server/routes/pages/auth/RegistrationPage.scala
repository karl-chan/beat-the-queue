package com.github.karlchan.beatthequeue.server.routes.pages.auth

import com.github.karlchan.beatthequeue.server.routes.pages._
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import scalatags.Text.all._

object RegistrationPage:
  def render(): Html = render(errorMessage = None)

  def renderFailure(errorMessage: String): Html =
    render(errorMessage = Some(errorMessage))

  private def render(errorMessage: Option[String]): Html =
    Template.styledPage(
      body(
        cls := "flex place-content-center place-items-center bg-gradient-to-r from-red-700 to-yellow-500",
        form(
          action := "/register",
          method := "POST",
          cls := "flex flex-col bg-gray-200 place-items-center shadow-2xl ring-2 ring-gray-400 p-8 space-y-4",
          h1(cls := "font-mono text-4xl text-gray-600", "Register"),
          span(cls := "text-red-500", errorMessage),
          input(
            `type` := "text",
            name := "username",
            placeholder := "Username"
          ),
          input(
            `type` := "password",
            name := "password",
            placeholder := "Password"
          ),
          input(
            `type` := "password",
            name := "confirmPassword",
            placeholder := "Confirm password"
          ),
          input(
            `type` := "hidden",
            id := "pushSubscriptionJson",
            name := "pushSubscriptionJson",
            value := ""
          ),
          styledButton(
            color = "yellow",
            `type` := "submit",
            "Submit"
          ),
          div(
            cls := "flex space-x-2",
            a(
              "Login with existing account",
              href := "/login",
              cls := "text-blue-900 underline"
            )
          )
        )
      )
    )
