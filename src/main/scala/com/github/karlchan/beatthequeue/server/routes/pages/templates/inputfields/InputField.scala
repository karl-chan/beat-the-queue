package com.github.karlchan.beatthequeue.server.routes.pages.templates.inputfields

import com.github.karlchan.beatthequeue.server.routes.pages.Html

trait InputField[V]:
  def render(name: String): Html
  def value: Option[V]
