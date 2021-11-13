package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers.all

import java.time.LocalDate

class CineworldCralwerTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:

  "run" should "return all events" in {
    val events = CineworldCrawler().run()
    events.asserting(
      _.length should be > 1000 // At least 1000 events on show
    )
  }

  "getCinemas" should "return list of cinemas" in {
    CineworldCrawler()
      .getCinemas()
      .asserting(
        _.body.cinemas.find(_.id == "103").get should have(
          Symbol("displayName")("London - Leicester Square")
        )
      )
  }

  "getFilmEvents" should "return film timetable" in {
    val res = CineworldCrawler().getFilmEvents(
      "103", // Leicester Square
      LocalDate.now
    )
    res.asserting(res => all(res.body.events.map(_.cinemaId)) should be("103"))
  }
