package com.github.karlchan.beatthequeue.merchants.cinema.thecinema

import java.time.LocalDate

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers.all

final class TheCinemaCrawlerTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:

  val crawler = TheCinemaCrawler()

  "run" should "return all events" in {
    val events = crawler.run().compile.toVector
    events.asserting(
      _.length should be > 100 // At least 100 events on show
    )
  }

  "getFilmEvents" should "return upcoming film events" in {
    crawler
      .getFilmEvents(Branch.PowerStation)
      .asserting(_ should not be empty)
  }
