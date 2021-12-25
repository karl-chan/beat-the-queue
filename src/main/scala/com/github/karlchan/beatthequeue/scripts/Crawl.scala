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
import com.github.karlchan.beatthequeue.util.given_Db

def crawl(): IO[ExitCode] =
  for {
    allMerchantEvents <- getAllMerchantEvents()
    allUsers <- getAllUsers()
    matches = allUsers
      .map(user => (user, findMatchesForUser(_, allMerchantEvents)))
      .toMap

  } yield ExitCode.Success

private def getAllMerchantEvents(): IO[Map[String, Seq[Event[_]]]] =
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
): Map[Criteria[_], Seq[Event[_]]] =
  user.criteria
    .map(criteria =>
      (criteria, findMatchesForCriteria(criteria, allMerchantEvents))
    )
    .toMap

private def findMatchesForCriteria[M](
    criteria: Criteria[M],
    allMerchantEvents: Map[String, Seq[Event[_]]]
): Seq[Event[_]] =
  val merchantEvents =
    allMerchantEvents(criteria.merchant).asInstanceOf[Seq[Event[M]]]
  merchantEvents.filter(criteria.matches(_))
