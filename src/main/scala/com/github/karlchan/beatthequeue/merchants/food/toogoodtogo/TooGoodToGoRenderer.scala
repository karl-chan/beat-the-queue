package com.github.karlchan.beatthequeue.merchants.food.toogoodtogo

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

class TooGoodToGoRenderer
    extends Renderer[TooGoodToGo, TooGoodToGoCriteria, TooGoodToGoEvent]:

  private val cachedInfo: IO[TooGoodToGoCrawler#Info] =
    TooGoodToGoCrawler().getInfo().memoize.unsafeRunSync()

  override def toFields(criteria: TooGoodToGoCriteria) = Seq(
    MultiStringField(label = "Names", value = criteria.names),
    DateField(label = "Start date", value = criteria.startDate),
    DateField(label = "End date", value = criteria.endDate),
    TimeField(label = "Start time", value = criteria.startTime),
    TimeField(label = "End time", value = criteria.endTime),
    DayOfWeekField(label = "Days of week", value = criteria.daysOfWeek)
  )

  override def toInputFields(criteria: TooGoodToGoCriteria) =
    for {
      info <- cachedInfo
    } yield Seq(
      MultiAutocompleteInputField(
        label = "Names",
        name = "names",
        options = info.names,
        value = criteria.names
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
      )
    )

  override def toFields(event: TooGoodToGoEvent) = Seq(
    StringField(label = "Name", value = Some(event.name)),
    DateTimeField(label = "Time", value = Some(event.time))
  )
