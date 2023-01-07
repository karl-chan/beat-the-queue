package com.github.karlchan.beatthequeue.merchants.cinema.bfi

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.github.karlchan.beatthequeue.merchants.Renderer
import com.github.karlchan.beatthequeue.merchants.cinema.bfi.BFI
import com.github.karlchan.beatthequeue.merchants.cinema.bfi.BFICrawler
import com.github.karlchan.beatthequeue.merchants.cinema.bfi.BFICriteria
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.DateTimeField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.MultiStringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.fields.StringField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.DateTimeInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.InputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.MultiAutocompleteInputField
import com.github.karlchan.beatthequeue.server.routes.pages.templates.form.MultiSelectInputField

class BFIRenderer extends Renderer[BFI, BFICriteria, BFIEvent]:

  private val cachedInfo: IO[BFICrawler#Info] =
    BFICrawler().getInfo().memoize.unsafeRunSync()

  override def toFields(criteria: BFICriteria) = Seq(
    MultiStringField(label = "Film names", value = criteria.filmNames),
    DateTimeField(label = "Start time", value = criteria.startTime),
    DateTimeField(label = "End time", value = criteria.endTime),
    MultiStringField(label = "Venues", value = criteria.venues),
    MultiStringField(label = "Screen types", value = criteria.screenTypes)
  )

  override def toInputFields(criteria: BFICriteria) =
    for {
      info <- cachedInfo
    } yield Seq(
      MultiAutocompleteInputField(
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

  override def toFields(event: BFIEvent) = Seq(
    StringField(label = "Film name", value = Some(event.name)),
    DateTimeField(label = "Time", value = Some(event.time)),
    StringField(label = "Venue", value = Some(event.venue)),
    StringField(label = "Screen type", value = Some(event.screenType))
  )
