package com.github.karlchan.beatthequeue.merchants.cinema.odeon

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.github.karlchan.beatthequeue.merchants.Renderer
import com.github.karlchan.beatthequeue.merchants.cinema.odeon.Odeon
import com.github.karlchan.beatthequeue.merchants.cinema.odeon.OdeonCrawler
import com.github.karlchan.beatthequeue.merchants.cinema.odeon.OdeonCriteria
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.DateField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.DateTimeField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.DayOfWeekField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.MultiStringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.StringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.TimeField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.DateInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.DateTimeInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.DayOfWeekInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.InputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.MultiAutocompleteInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.MultiSelectInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.TimeInputField

class OdeonRenderer extends Renderer[Odeon, OdeonCriteria, OdeonEvent]:

  private val cachedInfo: IO[OdeonCrawler#Info] =
    OdeonCrawler().getInfo().memoize.unsafeRunSync()

  override def toFields(criteria: OdeonCriteria) = Seq(
    MultiStringField(label = "Film names", value = criteria.filmNames),
    DateField(label = "Start date", value = criteria.startDate),
    DateField(label = "End date", value = criteria.endDate),
    TimeField(label = "Start time", value = criteria.startTime),
    TimeField(label = "End time", value = criteria.endTime),
    DayOfWeekField(label = "Days of week", value = criteria.daysOfWeek),
    MultiStringField(label = "Venues", value = criteria.venues),
    MultiStringField(label = "Screen types", value = criteria.screenTypes)
  )

  override def toInputFields(criteria: OdeonCriteria) =
    for {
      info <- cachedInfo
    } yield Seq(
      MultiAutocompleteInputField(
        label = "Film names",
        name = "filmNames",
        options = info.names,
        value = criteria.filmNames
      ),
      DateInputField(
        label = "Start date",
        name = "startDate",
        value = criteria.startDate
      ),
      DateInputField(
        label = "End date",
        name = "endDate",
        value = criteria.endDate
      ),
      TimeInputField(
        label = "Start time",
        name = "startTime",
        value = criteria.startTime
      ),
      TimeInputField(
        label = "End time",
        name = "endTime",
        value = criteria.endTime
      ),
      DayOfWeekInputField(
        label = "Days of week",
        name = "daysOfWeek",
        value = criteria.daysOfWeek
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

  override def toFields(event: OdeonEvent) = Seq(
    StringField(label = "Film name", value = Some(event.name)),
    DateTimeField(label = "Time", value = Some(event.time)),
    StringField(label = "Venue", value = Some(event.venue)),
    StringField(label = "Screen type", value = Some(event.screenType))
  )
