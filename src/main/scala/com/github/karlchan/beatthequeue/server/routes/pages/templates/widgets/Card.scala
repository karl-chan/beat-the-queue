package com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets

import scalatags.Text.all._

def card(args: Modifier*) = div(
  cls := "p-6 bg-white rounded-xl shadow-lg",
  args
)
