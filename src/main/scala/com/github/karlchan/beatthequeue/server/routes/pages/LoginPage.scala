package com.github.karlchan.beatthequeue.server.routes.pages

import com.github.karlchan.beatthequeue.server.routes.pages.Widget._
import scalatags.Text.all._

def loginPage =
  Template.styledPage(
    body(
      cls := "flex place-content-center place-items-center bg-gradient-to-r from-gray-700 to-blue-500",
      form(
        action := "/login",
        method := "POST",
        cls := "flex flex-col bg-gray-200 place-items-center shadow-2xl ring-2 ring-gray-400 p-8 space-y-4",
        h1(cls := "font-mono text-4xl text-gray-600", "Welcome"),
        input(`type` := "text", placeholder := "Username"),
        input(`type` := "text", placeholder := "Password"),
        styledButton(
          "Login",
          buttonType = "submit",
          color = "green"
        )
      )
    )
  )
