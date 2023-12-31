package com.github.karlchan.beatthequeue.merchants.cinema.vue

import java.time.LocalDate

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers.all

final class VueCrawlerTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:

  val crawler =
    VueCrawler(cinemaIds =
      Some(Vector("10030" /* Vue West End (Leicester Square) */ ))
    )

  "run" should "return all events" in {
    val events = crawler.run().compile.toVector
    events.asserting(
      _.length should be > 100 // At least 100 events on show
    )
  }

  "getCinemas" should "return only cinemas matching cinemaId" in {
    crawler
      .getCinemas()
      .asserting(
        _.length should be(1)
      )
  }

  "getFilms" should "return non-empty list of films" in {
    crawler
      .getFilms()
      .asserting(_.length should be > (10))
  }

  "getShowings" should "return film timetable" in {
    crawler
      .getShowings(
        "10030" /* Vue West End (Leicester Square) */
      )
      .asserting(
        _.flatMap(_.showingGroups.flatMap(_.sessions)) should not be empty
      )
  }

  "getAttributes" should "return all attributes" in {
    crawler
      .getAttributes()
      .asserting(_ should contain allOf ("IMAX", "Dolby Atmos"))
  }
