package com.github.karlchan.beatthequeue.util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

final class PropertiesTest extends AnyFlatSpec with should.Matchers:
  "get" should "return property as string" in {
    Properties.get("PATH") should include("/usr/local/bin")
  }

  "getInt" should "return property as int" in {
    Properties.getInt("server.port") should be(8080)
  }

  "getList" should "return property as list of strings" in {
    Properties.getList("cineworld.cinemaIds") should be(Vector("103", "077"))
  }
