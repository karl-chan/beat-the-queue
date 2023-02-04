package com.github.karlchan.beatthequeue.merchants.haircut.lsb

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.github.karlchan.beatthequeue.merchants.Renderer
import com.github.karlchan.beatthequeue.merchants.haircut.lsb.LSB
import com.github.karlchan.beatthequeue.merchants.haircut.lsb.LSBCrawler
import com.github.karlchan.beatthequeue.merchants.haircut.lsb.LSBCriteria
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.DateField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.DateTimeField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.DayOfWeekField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.MultiStringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.StringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.TimeField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.DateInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.DayOfWeekInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.InputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.MultiSelectInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.TimeInputField

class LSBRenderer extends Renderer[LSB, LSBCriteria, LSBEvent]:

  private val cachedInfo: IO[LSBCrawler#Info] =
    LSBCrawler().getInfo().memoize.unsafeRunSync()

  override def toFields(criteria: LSBCriteria) = Seq(
    DateField(label = "Start date", value = criteria.startDate),
    DateField(label = "End date", value = criteria.endDate),
    TimeField(label = "Start time", value = criteria.startTime),
    TimeField(label = "End time", value = criteria.endTime),
    DayOfWeekField(label = "Days of week", value = criteria.daysOfWeek),
    MultiStringField(label = "Categories", value = criteria.categories),
    MultiStringField(label = "Services", value = criteria.services)
  )

  override def toInputFields(criteria: LSBCriteria) =
    for {
      info <- cachedInfo
    } yield Seq(
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
        label = "Categories",
        name = "categories",
        options = info.categories,
        value = criteria.categories
      ),
      MultiSelectInputField(
        label = "Services",
        name = "services",
        options = info.services,
        value = criteria.services
      )
    )

  override def toFields(event: LSBEvent) = Seq(
    DateTimeField(label = "Time", value = Some(event.time)),
    StringField(label = "Category", value = Some(event.category)),
    StringField(label = "Service", value = Some(event.name))
  )
