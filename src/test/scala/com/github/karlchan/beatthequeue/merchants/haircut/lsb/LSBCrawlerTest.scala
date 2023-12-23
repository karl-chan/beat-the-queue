package com.github.karlchan.beatthequeue.merchants.haircut.lsb

import java.time.LocalDate

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers.all

final class LSBCrawlerTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:

  val crawler = LSBCrawler()

  "run" should "return all events" in {
    val events = crawler.run().compile.toVector
    events.asserting(
      _.length should be > 10 // At least 10 available haircut slots
    )
  }

  "getCategories" should "return json with available categories" in {
    crawler
      .getCategories()
      .asserting(
        _ should contain(
          CategoryResponse.Category(
            id = "4",
            name = "GUARANTEED APPOINTMENT £0-£7 (NON REFUNDABLE) ",
            events = Vector(
              "47",
              "125",
              "113",
              "127",
              "140",
              "176",
              "139",
              "132",
              "135",
              "179",
              "178",
              "84",
              "40",
              "153",
              "137",
              "138",
              "166"
            )
          )
        )
      )
  }

  "getServices" should "return json with available services" in {
    crawler
      .getServices()
      .asserting(
        _ should contain(
          ServiceResponse.Service(id = "47", name = "2+ Clipper Cut ")
        )
      )
  }

  "getTimeSlots" should "return json with available time slots" in {
    crawler
      .getTimeSlots(
        from = LocalDate.now(),
        to = LocalDate.now().plusMonths(1),
        category = "4",
        service = "47"
      )
      .asserting(_ should not be empty)
  }

  "getToken" should "return valid csrfToken" in {
    crawler
      .getToken()
      .asserting(_.csrfToken.length() should be(32))
  }
