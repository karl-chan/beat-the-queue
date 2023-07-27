package com.github.karlchan.beatthequeue.merchants.cinema.picturehouse

import java.time.LocalDate

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers.all

final class PicturehouseCrawlerTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:

  val crawler =
    PicturehouseCrawler(cinemaIds =
      Some(Vector("022" /* Picturehouse Central */ ))
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
      .asserting(_.length should be(1))
  }

  "getMovies" should "return film timetable" in {
    val res = crawler.getMovies()
    res.asserting(
      _.flatMap(_.movie_times.filter(_.CinemaId == "022")) should not be empty
    )
  }

  "getToken" should "return valid token" in {
    val res = crawler.getToken()
    res.asserting(_.token should have length 40)
  }
