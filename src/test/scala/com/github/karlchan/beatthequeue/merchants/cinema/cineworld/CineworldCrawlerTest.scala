package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import java.time.LocalDate

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers.all

final class CineworldCrawlerTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:

  val crawler =
    CineworldCrawler(cinemaIds = Some(Vector("103" /* Leicester Square */ )))

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

  "getFilmEvents" should "return film timetable" in {
    val res = crawler.getFilmEvents(
      "103", // Leicester Square
      LocalDate.now.plusDays(7)
    )
    res.asserting(filmEventsResponse =>
      filmEventsResponse.body.events.map(
        _.cinemaId
      ) should contain only "103"
    )
  }

  "getNowPlaying" should "return non-empty list of films" in {
    val res = crawler.getNowPlaying()
    res.asserting(_.body.posters should not be empty)
  }

  "getComingSoon" should "return non-empty list of films" in {
    val res = crawler.getComingSoon()
    res.asserting(_.body.posters should not be empty)
  }
