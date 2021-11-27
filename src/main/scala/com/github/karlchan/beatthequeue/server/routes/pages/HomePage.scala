package com.github.karlchan.beatthequeue.server.routes.pages

import com.github.karlchan.beatthequeue.server.routes.pages.Widget._
import scalatags.Text.all._

def homePage: Html =
  Template.styledPage(
    navigationBar
  )
