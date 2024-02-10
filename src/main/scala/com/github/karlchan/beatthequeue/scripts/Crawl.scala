package com.github.karlchan.beatthequeue.scripts

import java.time.LocalDateTime

import cats.effect.ExitCode
import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.merchants.given_Ordering_Event
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Models
import com.github.karlchan.beatthequeue.util.Notifications
import com.github.karlchan.beatthequeue.util.given_Db
import com.mongodb.client.result.UpdateResult
import com.softwaremill.quicklens.modify
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

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

    _ <- newMatchResults.parTraverse(notifyUser(_))
    _ <- logger.info("Notified all users.")
  } yield ExitCode.Success

private def streamAllEvents(): Stream[IO, Event[?]] =
  def logError(e: Throwable) =
    Stream.eval(IO(e.printStackTrace())) >> Stream.empty

  Stream
    .emits(Merchants.AllList)
    .filter(_.enabled)
    .map(_.eventFinder.run().handleErrorWith(logError))
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
        matchingEventsByCriteria = user.criteria.map((_, Set.empty)).toMap
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
        then (criteria, events + event)
        else (criteria, events)
      )
    )
  )

private def filterNewMatchResults(
    matchResults: MatchResults,
    alreadyNofifiedUserEvents: Map[Models.User, Set[Event[?]]]
): MatchResults =
  matchResults.map(matchResult =>
    matchResult
      .modify(_.matchingEventsByCriteria)
      .using(
        _.map((criteria, events) =>
          val newMatchResults =
            events -- alreadyNofifiedUserEvents(matchResult.user)
          (criteria, newMatchResults)
        )
      )
  )

private def notifyUser(matchResult: MatchResult)(using db: Db): IO[Unit] =
  val now = LocalDateTime.now
  val settings = matchResult.user.notificationSettings
  val newEvents =
    matchResult.matchingEventsByCriteria.values.flatten.toSeq.sorted.distinct
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
    matchingEventsByCriteria: Map[Criteria[?], Set[Event[?]]]
)

private type MatchResults = Seq[MatchResult]
