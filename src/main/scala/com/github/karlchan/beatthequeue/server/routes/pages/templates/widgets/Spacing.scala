package com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets

import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import scalatags.Text.all._

def hspace(width: Int) = div(cls := s"w-$width")
def vspace(height: Int) = div(cls := s"h-$height")
