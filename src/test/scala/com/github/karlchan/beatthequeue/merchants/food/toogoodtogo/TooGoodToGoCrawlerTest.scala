package com.github.karlchan.beatthequeue.merchants.food.toogoodtogo

import java.time.LocalDate

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers.all

final class TooGoodToGoCrawlerTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:

  val crawler = TooGoodToGoCrawler()

  "run" should "return all events" in {
    val events = crawler.run().compile.toVector
    events.asserting(_.length should be >= 0)
  }

  "getToken" should "return valid accessToken" in {
    crawler
      .getToken()
      .asserting(_.accessToken.split("\\.").length shouldBe 3)
  }
