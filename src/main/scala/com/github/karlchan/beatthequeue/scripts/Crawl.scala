package com.github.karlchan.beatthequeue.scripts

import cats.effect.ExitCode
import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Models
import com.github.karlchan.beatthequeue.util.Notifications
import com.github.karlchan.beatthequeue.util.given_Db
import com.mongodb.client.result.UpdateResult
import com.softwaremill.quicklens.modify
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.LocalDateTime

def crawl(): IO[ExitCode] =
  for {
    logger <- Slf4jLogger.create[IO]

    allMerchantEvents <- findAllEvents()
    _ <- logger.info("Finshed crawling for all events.")

    allUsers <- getAllUsers()
    _ <- logger.info("Got all users.")

    allMatchResults = allUsers.map(findMatchesForUser(_, allMerchantEvents))
    _ <- logger.info("Got match results.")

    _ <- allMatchResults.parTraverse(notifyUser(_))
    _ <- logger.info("Notified all users.")
  } yield ExitCode.Success

private def findAllEvents(): IO[Map[String, Seq[Event[_]]]] =
  Merchants.AllByName.toSeq
    .parTraverse((name, merchant) =>
      for {
        events <- merchant.eventFinder.run()
      } yield (name, events)
    )
    .map(_.toMap)

private def getAllUsers()(using db: Db): IO[Seq[Models.User]] =
  for {
    usersCollection <- db.users
    users <- usersCollection.find.all
  } yield users.toSeq

private def findMatchesForUser(
    user: Models.User,
    allMerchantEvents: Map[String, Seq[Event[_]]]
): MatchResult =
  MatchResult(
    user = user,
    matchingEventsByCriteria = user.criteria
      .map(criteria =>
        (
          criteria,
          removeDuplicates(
            findMatchesForCriteria(criteria, allMerchantEvents),
            user.notifications.map(_.event)
          )
        )
      )
      .toMap
  )

private def findMatchesForCriteria[M](
    criteria: Criteria[M],
    allMerchantEvents: Map[String, Seq[Event[_]]]
): Seq[Event[_]] =
  val merchantEvents =
    allMerchantEvents(criteria.merchant).asInstanceOf[Seq[Event[M]]]
  merchantEvents.filter(criteria.matches(_))

private def removeDuplicates(
    newEvents: Seq[Event[_]],
    oldEvents: Seq[Event[_]]
): Seq[Event[_]] =
  (newEvents.toSet -- oldEvents.toSet).toSeq

private def notifyUser(matchResult: MatchResult)(using db: Db): IO[Unit] =
  val now = LocalDateTime.now
  val settings = matchResult.user.notificationSettings
  val newEvents = matchResult.matchingEventsByCriteria.values.flatten.toSeq
  val newNotifications =
    newEvents.map(event => Models.Notification(event = event, published = now))
  for {
    // Insert new events into existing db user object
    _ <- db.updateUser(
      matchResult.user._id.toString,
      _.modify(_.notifications).using(_ ++ newNotifications)
    )
    // Send out notifications via subscribed means
    _ <- Notifications.sendEmail(settings.emailAddresses, newEvents)
    _ <- settings.pushSubscriptions.parTraverse(
      Notifications.sendPush(_, newEvents)
    )
  } yield ()

case class MatchResult(
    user: Models.User,
    matchingEventsByCriteria: Map[Criteria[_], Seq[Event[_]]]
)
