package com.github.karlchan.beatthequeue.server.routes.pages

import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._
import scalatags.Text.all._

def homePage: Html =
  Template.styledPage(
    navigationBar
  )
