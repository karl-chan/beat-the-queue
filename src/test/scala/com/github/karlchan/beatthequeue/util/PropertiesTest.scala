package com.github.karlchan.beatthequeue.util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

final class PropertiesTest extends AnyFlatSpec with should.Matchers:
  "getInt" should "return property as int" in {
    Properties.getInt("server.port") should be(8080)
  }

  "get" should "return property as string" in {
    Properties.get("PATH") should include("/usr/local/bin")
  }
