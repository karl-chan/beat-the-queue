package com.github.karlchan.beatthequeue.util

import cats.effect.IO
import cats.effect.unsafe.implicits.global
<<<<<<< HEAD
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.CineworldCriteria
=======
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.merchants.given_Decoder_Criteria
import com.github.karlchan.beatthequeue.merchants.given_Decoder_Event
import com.github.karlchan.beatthequeue.merchants.given_Encoder_Event
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.mongodb.client.result.UpdateResult
import io.circe.Decoder
import io.circe.Encoder
import io.circe.HCursor
import io.circe.Json
>>>>>>> master
import io.circe.generic.auto._
import io.circe.syntax._
import mongo4cats.bson.ObjectId
import mongo4cats.circe._
import mongo4cats.client.MongoClient
import mongo4cats.collection.MongoCollection
import mongo4cats.collection.operations.Filter

import java.time.LocalDateTime
import java.util.UUID

given Db = Db()

final class Db:
  private val (client, shutdownHook): (MongoClient[IO], IO[Unit]) =
    MongoClient
      .fromConnectionString[IO](
        Properties.get("mongo.uri")
      )
      .allocated
      .unsafeRunSync()
  private val db =
    client.getDatabase(Properties.get("mongo.dbname")).unsafeRunSync()

  def name: String = db.name

  def users: IO[MongoCollection[IO, Models.User]] =
    db.getCollectionWithCodec[Models.User]("users")

  def findUser(authUser: AuthUser): IO[Models.User] = findUser(authUser.id)
  def findUser(userId: String): IO[Models.User] =
    for {
      usersCollection <- users
      maybeUser <- usersCollection.find
        .filter(Filter.eq(Fields.Id, ObjectId(userId)))
        .first
    } yield maybeUser.get

  def updateUser(
      authUser: AuthUser,
      updateFunction: Models.User => Models.User
  ): IO[UpdateResult] = updateUser(authUser.id, updateFunction)
  def updateUser(
      userId: String,
      updateFunction: Models.User => Models.User
  ): IO[UpdateResult] =
    for {
      usersCollection <- users
      maybeUser <- usersCollection.find
        .filter(Filter.eq(Fields.Id, ObjectId(userId)))
        .first
      user = maybeUser.get
      newUser = updateFunction(user)
      res <- usersCollection.replaceOne(
        Filter.eq(Fields.Id, ObjectId(userId)),
        newUser
      )
    } yield res

  def close(): IO[Unit] =
    shutdownHook

object Models:
  final case class User(
      _id: ObjectId,
      username: String,
      hash: String,
      criteria: Seq[Criteria[_]] = Seq.empty,
      notificationSettings: NotificationSettings = NotificationSettings(),
      notifications: Seq[Notification] = Seq.empty
  )

  final case class NotificationSettings(
      emailAddresses: Seq[String] = Seq.empty,
      pushSubscriptions: Seq[PushSubscription] = Seq.empty
  )

  final case class PushSubscription(
      endpoint: String,
      expirationTime: Option[String] = None,
      keys: PushSubscriptionKeys
  )

  final case class PushSubscriptionKeys(
      p256dh: String,
      auth: String
  )

  final case class Notification(
      id: String = UUID.randomUUID.toString,
      event: Event[_],
      published: LocalDateTime,
      hidden: Boolean = false
  )

object Fields:
  val Id = "_id"
  val Username = "username"
  val Criteria = "criteria"
  val CriteriaId = "id"

given Encoder[Criteria[?]] = new {
  final def apply(criteria: Criteria[?]): Json =
    val merchant = Merchants.findMerchantFor(criteria)
    merchant.criteriaEncoder.apply(criteria)
}
