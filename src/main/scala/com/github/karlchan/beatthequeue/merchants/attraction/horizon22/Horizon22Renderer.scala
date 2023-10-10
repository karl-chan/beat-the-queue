package com.github.karlchan.beatthequeue.merchants.attraction.horizon22

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

class Horizon22Renderer
    extends Renderer[Horizon22, Horizon22Criteria, Horizon22Event]:

  override def toFields(criteria: Horizon22Criteria) = Seq(
    DateField(label = "Start date", value = criteria.startDate),
    DateField(label = "End date", value = criteria.endDate),
    TimeField(label = "Start time", value = criteria.startTime),
    TimeField(label = "End time", value = criteria.endTime),
    DayOfWeekField(label = "Days of week", value = criteria.daysOfWeek)
  )

  override def toInputFields(criteria: Horizon22Criteria) =
    IO.pure(
      Seq(
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
    )

  override def toFields(event: Horizon22Event) = Seq(
    StringField(label = "Name", value = Some(event.name)),
    DateTimeField(label = "Time", value = Some(event.time))
  )
