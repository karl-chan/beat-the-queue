package com.github.karlchan.beatthequeue.util

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import sttp.client3._
import sttp.model.StatusCode
import sttp.model.headers.CookieWithMeta

final class HttpTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:
  "getHtml" should "return html as string" in {
    Http()
      .getHtml(uri"https://duckduckgo.com")
      .asserting(_ should include("<!DOCTYPE html>"))
  }

  "getFullResponse" should "include response cookies" in {
    Http()
      .getFullResponse(
        uri"https://whatson.bfi.org.uk/imax/Online/default.asp"
      )
      .map(_.unsafeCookies.map(_.name))
      .asserting(_ should contain("AV-Cookie"))
  }
