package com.github.karlchan.beatthequeue.merchants.cinema.odeon

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers.all

import java.time.LocalDate

final class OdeonCrawlerTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:

  val crawler = OdeonCrawler(siteIds = Some(Seq("155" /* London West End */ )))

  "run" should "return all events" in {
    val events = crawler.run().compile.toVector
    events.asserting(
      _.length should be > 10 // At least 10 events on show
    )
  }

  "getSites" should "return json with available sites" in {
    crawler
      .getSites()
      .asserting(_.sites should not be empty)
  }

  "getFilms" should "return json with available films" in {
    crawler
      .getFilms()
      .asserting(_.films should not be empty)
  }

  "getScreeningDates" should "return json with available dates" in {
    crawler
      .getScreeningDates()
      .asserting(_.filmScreeningDates should not be empty)
  }

  "getShowtimes" should "return json with available times" in {
    crawler
      .getShowtimes(LocalDate.now().plusDays(7))
      .asserting(_.showtimes should not be empty)
  }

  "getToken" should "return valid authToken" in {
    crawler
      .getToken()
      .asserting(_.authToken.length() should be > 1000)
  }
