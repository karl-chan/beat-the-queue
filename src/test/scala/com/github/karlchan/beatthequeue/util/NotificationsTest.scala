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

  val events =
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

  "sendEmail" should "deliver list of events succesfully" in {
    // Send email to myself
    val res = Notifications.sendEmail(
      Seq(Properties.get("mail.server.user")),
      events
    )
    res.asserting(_ should not be ())
  }

  "sendNotification" should "deliver push notification successfully" in {
    // Send notification to myself
    // TODO: Update this variable before running the test.
    val testSubscription =
      Models.PushSubscription(
        endpoint =
          "https://fcm.googleapis.com/fcm/send/f1OQ18qHXvo:APA91bGw0wgEok8LDppZK9OPoA2Rb5K2DYG60A8YatbsXtOSJbLaFQ1SFU4U1p5jd_m2mqHhD7EMQCAtnElxE2sG9Jhfe7VMjS3PW3GAdALmGpo_2_jXhF6pulgljCtepQLxWsEC9g22",
        keys = Models.PushSubscriptionKeys(
          p256dh =
            "BD4CnV_s-iamTizS5nWJqfTtCzpRmIJCgbjoGjhSf2C14y9sT2zryiEBY7VwkUJKDeIB_7yMZIRuxOF8fOe1uOE",
          auth = "_USyP-Tr_w0kMfdj8fpKAg"
        )
      )
    val res = Notifications.sendPush(testSubscription, events)
    res.asserting(_ should not be ())
  }
