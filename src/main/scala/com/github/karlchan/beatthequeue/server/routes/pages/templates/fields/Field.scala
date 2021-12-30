package com.github.karlchan.beatthequeue.server.routes.pages.templates.fields

import com.github.karlchan.beatthequeue.server.routes.pages.Html

sealed trait Field:
  val label: String
  def render: Html

private[this] trait SingleField[V] extends Field:
  val value: Option[V]

private[this] trait MultiField[V] extends Field:
  val value: Seq[V]
