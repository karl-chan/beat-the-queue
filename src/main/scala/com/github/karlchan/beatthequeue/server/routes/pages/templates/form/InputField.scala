package com.github.karlchan.beatthequeue.server.routes.pages.templates.form

import com.github.karlchan.beatthequeue.server.routes.pages.Html

sealed trait InputField:
  val label: String
  val name: String
  def render: Html

private trait SingleInputField[V] extends InputField:
  val value: Option[V]

private trait MultiInputField[V] extends InputField:
  val value: Seq[V]
