package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.Cineworld
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.CineworldCrawler
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.CineworldCriteria
import com.github.karlchan.beatthequeue.server.routes.pages.merchants.Renderer
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.DateTimeField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.MultiStringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.DateTimeInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.InputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.MultiSelectInputField

class CineworldRenderer extends Renderer[Cineworld, CineworldCriteria]:
  override def toFields(criteria: CineworldCriteria) = Seq(
    MultiStringField(label = "Film names", value = criteria.filmNames),
    DateTimeField(label = "Start time", value = criteria.startTime),
    DateTimeField(label = "End time", value = criteria.endTime),
    MultiStringField(label = "Venues", value = criteria.venues),
    MultiStringField(label = "Screen types", value = criteria.screenTypes)
  )

  override def toInputFields(criteria: CineworldCriteria) =
    for {
      info <- CineworldCrawler().getInfo()
    } yield Seq(
      MultiSelectInputField(
        label = "Film names",
        name = "filmNames",
        options = info.names,
        value = criteria.filmNames
      ),
      DateTimeInputField(
        label = "Start time",
        name = "startTime",
        value = criteria.startTime
      ),
      DateTimeInputField(
        label = "End time",
        name = "endTime",
        value = criteria.endTime
      ),
      MultiSelectInputField(
        label = "Venues",
        name = "venues",
        options = info.venues,
        value = criteria.venues
      ),
      MultiSelectInputField(
        label = "Screen types",
        name = "screenTypes",
        options = info.screenTypes,
        value = criteria.screenTypes
      )
    )
