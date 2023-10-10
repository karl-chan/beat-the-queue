package com.github.karlchan.beatthequeue.merchants.attraction.horizon22

import java.time.LocalDate

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers.all

final class Horizon22CrawlerTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:

  val crawler = Horizon22Crawler()

  "run" should "return all events" in {
    val events = crawler.run().compile.toVector
    events.asserting(_.length should be >= 0)
  }

  "getCalendar" should "return calendar dates in the future" in {
    crawler
      .getCalendar()
      .asserting(cal => every(cal.keys) should be >= LocalDate.now)
  }

  "getSessions" should "return events" in {
    crawler
      .getSessions(LocalDate.now.plusDays(1))
      .asserting(_.length should be >= 0)
  }
