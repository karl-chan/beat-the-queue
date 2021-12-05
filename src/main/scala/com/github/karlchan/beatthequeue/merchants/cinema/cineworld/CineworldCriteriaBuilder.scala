package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.CriteriaBuilder
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.DateTimeInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.MultiSelectInputField

import java.time.LocalDateTime
import java.util.UUID

class CineworldCriteriaBuilder(
    names: Seq[String],
    venues: Seq[String],
    screenTypes: Seq[String]
) extends CriteriaBuilder[Cineworld]:
  val fields = Map(
    "names" -> MultiSelectInputField(label = "Film name", options = names),
    "startTime" -> DateTimeInputField(label = "Start time"),
    "endTime" -> DateTimeInputField(label = "End time"),
    "venues" -> MultiSelectInputField(label = "Venue", options = venues),
    "screenTypes" -> MultiSelectInputField(
      label = "Screen type",
      options = screenTypes
    )
  )
