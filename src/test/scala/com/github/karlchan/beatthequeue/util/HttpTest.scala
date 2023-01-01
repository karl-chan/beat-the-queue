package com.github.karlchan.beatthequeue.util

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import sttp.client3._

final class HttpTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:
  "getHtml" should "return html as string" in {
    Http()
      .getHtml(uri"https://duckduckgo.com")
      .asserting(_ should include("<!DOCTYPE html>"))
  }
