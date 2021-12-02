package com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets

import scalatags.Text.all._
import scalatags.generic.Modifier
import scalatags.text.Builder

def linkButton(
    color: String,
    args: Modifier[Builder]*
) =
  a(
    cls := s"px-3 py-2 rounded-md text-sm font-medium text-white bg-$color-600 hover:bg-$color-500 focus:ring-4 focus:ring-$color-100",
    args
  )

def styledButton(
    color: String,
    args: Modifier[Builder]*
) =
  button(
    cls := s"px-3 py-2 rounded-md text-sm font-medium text-white bg-$color-600 hover:bg-$color-500 focus:ring-4 focus:ring-$color-100",
    args
  )
