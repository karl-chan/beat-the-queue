package com.github.karlchan.beatthequeue.util

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should

final class HttpTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:
  "get" should "return html as string" in {
    Http()
      .get[String]("https://duckduckgo.com")
      .asserting(_ should include("<!DOCTYPE html>"))
  }
