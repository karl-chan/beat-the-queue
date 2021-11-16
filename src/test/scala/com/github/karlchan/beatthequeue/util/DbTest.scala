package com.github.karlchan.beatthequeue.util

import cats.effect.testing.scalatest.AsyncIOSpec
import com.github.karlchan.beatthequeue.util.given_Db
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should

class DbTest extends AsyncFlatSpec with AsyncIOSpec with should.Matchers:
  "name" should "return name of database" in {
    given_Db.name should be("beat-the-queue")
  }
