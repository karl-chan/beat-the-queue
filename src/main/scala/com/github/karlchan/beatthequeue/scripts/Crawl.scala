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
import fs2.Stream
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.LocalDateTime

def crawl(): IO[ExitCode] =
  for {
    logger <- Slf4jLogger.create[IO]

    allUsers <- getAllUsers()
    _ <- logger.info("Got all users.")

    allMatchResults <- streamAllEvents()
      .fold(initMatchResults(allUsers))(accumlateMatchResults)
      .compile
      .lastOrError
    _ <- logger.info("Got match results.")

    alreadyNotifiedUserEvents = allUsers
      .map(user => (user, user.notifications.map(_.event).toSet))
      .toMap
    newMatchResults = filterNewMatchResults(
      allMatchResults,
      alreadyNotifiedUserEvents
    )
    _ <- logger.info("Filtered out new match results.")

    _ <- allMatchResults.parTraverse(notifyUser(_))
    _ <- logger.info("Notified all users.")
  } yield ExitCode.Success

private def streamAllEvents(): Stream[IO, Event[_]] =
  Stream
    .emits(Merchants.AllList)
    .map(_.eventFinder.run())
    .parJoin(5)

private def getAllUsers()(using db: Db): IO[Seq[Models.User]] =
  for {
    usersCollection <- db.users
    users <- usersCollection.find.all
  } yield users.toSeq

private def initMatchResults(users: Seq[Models.User]): MatchResults =
  users
    .map(user =>
      MatchResult(
        user = user,
        matchingEventsByCriteria = user.criteria.map((_, Seq.empty)).toMap
      )
    )

private def accumlateMatchResults[M](
    matchResults: MatchResults,
    event: Event[M]
): MatchResults =
  matchResults.map(
    _.modify(_.matchingEventsByCriteria).using(
      _.map((criteria, events) =>
        if criteria.merchant == event.merchant
          && criteria.asInstanceOf[Criteria[M]].matches(event)
        then (criteria, event +: events)
        else (criteria, events)
      )
    )
  )

private def filterNewMatchResults(
    matchResults: MatchResults,
    alreadyNofifiedUserEvents: Map[Models.User, Set[Event[_]]]
): MatchResults =
  matchResults.map(matchResult =>
    matchResult
      .modify(_.matchingEventsByCriteria)
      .using(
        _.map((criteria, events) =>
          val newMatchResults =
            (events.toSet -- alreadyNofifiedUserEvents(matchResult.user)).toSeq
          (criteria, newMatchResults)
        )
      )
  )

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

private case class MatchResult(
    user: Models.User,
    matchingEventsByCriteria: Map[Criteria[_], Seq[Event[_]]]
)

private type MatchResults = Seq[MatchResult]
