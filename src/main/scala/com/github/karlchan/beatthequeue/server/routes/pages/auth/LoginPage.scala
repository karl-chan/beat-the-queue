package com.github.karlchan.beatthequeue.server.routes.pages.auth

import com.github.karlchan.beatthequeue.server.routes.pages.Widget._
import com.github.karlchan.beatthequeue.server.routes.pages._
import scalatags.Text.all._

def loginPage: Html = loginPage(errorMessage = None)

def loginFailedPage: Html = loginPage(errorMessage = Some("Login failed"))

private def loginPage(errorMessage: Option[String]): Html =
  Template.styledPage(
    body(
      cls := "flex place-content-center place-items-center bg-gradient-to-r from-gray-700 to-blue-500",
      form(
        action := "/login",
        method := "POST",
        cls := "flex flex-col bg-gray-200 place-items-center shadow-2xl ring-2 ring-gray-400 p-8 space-y-4",
        h1(cls := "font-mono text-4xl text-gray-600", "Welcome"),
        span(cls := "text-red-500", errorMessage),
        input(`type` := "text", name := "username", placeholder := "Username"),
        input(
          `type` := "password",
          name := "password",
          placeholder := "Password"
        ),
        styledButton(
          "Login",
          buttonType = "submit",
          color = "green"
        ),
        div(
          cls := "flex space-x-2",
          a(
            "Register new user",
            href := "/register",
            cls := "text-blue-900 underline"
          )
        )
      )
    )
  )
