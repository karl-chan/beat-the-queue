package com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets

import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import scalatags.Text.all._

def navigationBar =
  nav(
    cls := "flex items-center bg-gray-800 text-white",
    button(
      `type` := "button",
      cls := "p-4 text-gray-500 hover:bg-gray-900",
      materialIcon("menu")
    ),
    hspace(8),
    linkButton(
      color = "red",
      href := "/criteria/catalog",
      "Create alert"
    ),
    div(cls := "flex-grow"),
    linkButton(color = "gray", href := "/logout", "Logout"),
    hspace(8)
  )
