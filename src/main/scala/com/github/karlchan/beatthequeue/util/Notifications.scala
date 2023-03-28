package com.github.karlchan.beatthequeue.util

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.given_Encoder_Event
import emil._
import emil.builder._
import emil.javamail._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import nl.martijndwars.webpush.Notification
import nl.martijndwars.webpush.PushAsyncService
import nl.martijndwars.webpush.Subscription
import nl.martijndwars.webpush.Subscription.Keys
import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.security.Security
import scala.collection.mutable

object Notifications:
  def sendEmail(emailAddresses: Seq[String], events: Seq[Event[?]]): IO[?] =
    if emailAddresses.isEmpty || events.isEmpty then return IO.unit

    def buildMessage(): String =
      val builder = mutable.StringBuilder()
      builder ++= "<p>The following events may be of interest to you:</p>"
      builder ++= "<ul>"
      for (event <- events) {
        builder ++= s"<li>${event.asJson}</li>"
      }
      builder ++= "</ul>"
      builder.toString

    val message = buildMessage()

    def buildEmail(recipientAddress: String): Mail[IO] =
      MailBuilder.build(
        From(senderAddress),
        To(recipientAddress),
        Subject("[Beat the Queue] Upcoming events"),
        HtmlBody(message)
      )

    val emails = emailAddresses.map(buildEmail(_))
    emailClient(smtpConf).send(emails.head, emails.tail*)

  def sendPush(
      pushSubscription: Models.PushSubscription,
      events: Seq[Event[?]]
  ): IO[?] =
    if events.isEmpty then return IO.unit

    val notification =
      Notification(
        Subscription(
          pushSubscription.endpoint,
          Keys(pushSubscription.keys.p256dh, pushSubscription.keys.auth)
        ),
        events.length.toString
      )
    IO.fromCompletableFuture(IO(pushService.send(notification)))

  private val emailClient = JavaMailEmil[IO]()
  private val senderAddress = Properties.get("mail.server.from")
  private val smtpConf =
    MailConfig(
      Properties.get("mail.server.smtp.address"),
      Properties.get("mail.server.user"),
      Properties.get("mail.server.password"),
      SSLType.StartTLS
    )

  Security.addProvider(new BouncyCastleProvider())
  private val pushService = PushAsyncService(
    Properties.get("vapid.public.key"),
    Properties.get("vapid.private.key")
  )
