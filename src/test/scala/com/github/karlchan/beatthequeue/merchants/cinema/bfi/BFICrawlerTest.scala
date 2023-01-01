package com.github.karlchan.beatthequeue.merchants.cinema.bfi

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers.all

import java.time.LocalDate

final class BFICrawlerTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:

  val crawler = BFICrawler()

  "getToken" should "return valid sToken" in {
    crawler
      .getToken()
      .asserting(
        _.sToken should include(",")
      )
  }

  "getToken" should "return valid articleId" in {
    crawler
      .getToken()
      .asserting(
        _.articleId should include("-")
      )
  }
