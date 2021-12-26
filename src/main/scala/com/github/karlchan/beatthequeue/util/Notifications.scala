package com.github.karlchan.beatthequeue.util

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.given_Encoder_Event
import emil._
import emil.builder._
import emil.javamail._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.mutable

object Notifications:
  def sendEmail(emailAddress: String, events: Seq[Event[_]]): IO[_] =
    val body = mutable.StringBuilder()
    body ++= "<p>The following events satisfy your criteria</p>"
    body ++= "<ul>"
    for (event <- events) {
      body ++= s"<li>${event.asJson}</li>"
    }
    body ++= "</ul>"

    val mail: Mail[IO] = MailBuilder.build(
      From(senderAddress),
      To(emailAddress),
      Subject("[Beat the Queue] Upcoming events"),
      HtmlBody(body.toString)
    )

    emailClient(smtpConf).send(mail)

  def sendPush(pushEndpoint: String, events: Seq[Event[_]]): IO[Unit] = ???

  private val emailClient = JavaMailEmil[IO]()
  private val senderAddress = Properties.get("mail.server.user")
  private val smtpConf =
    MailConfig(
      Properties.get("mail.server.smtp.address"),
      senderAddress,
      Properties.get("mail.server.password"),
      SSLType.StartTLS
    )
