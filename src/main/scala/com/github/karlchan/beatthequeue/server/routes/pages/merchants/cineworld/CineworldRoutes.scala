package com.github.karlchan.beatthequeue.server.routes.pages.merchants.cineworld

import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.CineworldCrawler
import cats.effect.IO
import com.github.karlchan.beatthequeue.server.routes.pages.Html

def cineworldCriteriaForm: IO[Html] =
  for {
    info <- CineworldCrawler().getInfo()
  } yield CineworldRenderer.renderForm(
    names = info.names,
    venues = info.venues,
    screenTypes = info.screenTypes
  )
