package com.github.karlchan.beatthequeue.util

import cats.effect.testing.scalatest.AsyncIOSpec
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.CineworldEvent
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should

import java.time.LocalDateTime

final class NotificationsTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers:
  "sendEmail" should "deliver list of events succesfully" in {
    // Send email to myself
    val res = Notifications.sendEmail(
      Properties.get("mail.server.user"),
      Seq(
        CineworldEvent(
          name = "Dune",
          time = LocalDateTime.of(2021, 10, 15, 0, 0),
          venue = "Leicester Square",
          screenType = "IMAX 2D"
        ),
        CineworldEvent(
          name = "No Time To Die",
          time = LocalDateTime.of(2021, 10, 15, 0, 0),
          venue = "Leicester Square",
          screenType = "IMAX 2D"
        )
      )
    )
    res.asserting(_ should not be ())
  }
