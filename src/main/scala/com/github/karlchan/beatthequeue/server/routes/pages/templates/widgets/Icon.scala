package com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets

import scalatags.Text.all._

def materialIcon(
    args: Modifier*
) = span(cls := "material-icons", args)
