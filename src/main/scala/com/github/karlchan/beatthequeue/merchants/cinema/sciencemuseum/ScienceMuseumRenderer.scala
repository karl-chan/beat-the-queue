package com.github.karlchan.beatthequeue.merchants.cinema.sciencemuseum

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.github.karlchan.beatthequeue.merchants.Renderer
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

class ScienceMuseumRenderer
    extends Renderer[ScienceMuseum, ScienceMuseumCriteria, ScienceMuseumEvent]:

  private val cachedInfo: IO[ScienceMuseumCrawler#Info] =
    ScienceMuseumCrawler().getInfo().memoize.unsafeRunSync()

  override def toFields(criteria: ScienceMuseumCriteria) = Seq(
    MultiStringField(label = "Film names", value = criteria.filmNames),
    DateField(label = "Start date", value = criteria.startDate),
    DateField(label = "End date", value = criteria.endDate),
    TimeField(label = "Start time", value = criteria.startTime),
    TimeField(label = "End time", value = criteria.endTime),
    DayOfWeekField(label = "Days of week", value = criteria.daysOfWeek),
    MultiStringField(label = "Product types", value = criteria.productTypeIds)
  )

  override def toInputFields(criteria: ScienceMuseumCriteria) =
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
        label = "Product types",
        name = "productTypeIds",
        options = info.productTypeIds,
        value = criteria.productTypeIds
      )
    )

  override def toFields(event: ScienceMuseumEvent) = Seq(
    StringField(label = "Film name", value = Some(event.name)),
    DateTimeField(label = "Time", value = Some(event.time)),
    StringField(label = "Product types", value = Some(event.productTypeId))
  )
