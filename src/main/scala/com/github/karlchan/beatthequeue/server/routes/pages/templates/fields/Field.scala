package com.github.karlchan.beatthequeue.server.routes.pages.templates.fields

import com.github.karlchan.beatthequeue.server.routes.pages.Html

trait Field[V]:
  val label: String
  val value: Option[V]
  def render: Html
