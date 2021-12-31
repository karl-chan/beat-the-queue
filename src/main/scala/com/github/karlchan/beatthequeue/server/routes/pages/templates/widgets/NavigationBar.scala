package com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets

import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import scalatags.Text.all._

def navigationBar(args: Modifier*) =
  div(
    xData := """{
      isOpen: false
    }
    """,
    nav(
      cls := "flex items-center bg-gray-800 text-white",
      div(
        cls := "cursor-pointer p-4 text-gray-500",
        xClass := "isOpen ? 'bg-gray-900 hover:bg-gray-700' : 'hover:bg-gray-900'",
        attr("x-on:click") := "isOpen = !isOpen",
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
    ),
    div(
      cls := "flex h-full",
      div(
        xShow := "isOpen",
        cls := "flex flex-col w-48 bg-gray-50 shadow-md z-10",
        a(
          cls := "p-4 hover:bg-gray-200 focus:bg-gray-300",
          href := "/settings",
          "Settings"
        )
      ),
      div(
        cls := "flex-grow",
        args
      )
    )
  )
