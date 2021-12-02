package com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets

import com.github.karlchan.beatthequeue.server.routes.pages.templates._
import scalatags.Text.all._

def horizontalGap(width: Int) = div(cls := s"w-$width")
def verticalGap(height: Int) = div(cls := s"w-$height")
