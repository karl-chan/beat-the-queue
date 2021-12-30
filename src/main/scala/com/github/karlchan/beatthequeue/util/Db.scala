package com.github.karlchan.beatthequeue.util

import cats.effect.IO
import cats.effect.unsafe.implicits.global
<<<<<<< HEAD
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.CineworldCriteria
=======
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.merchants.given_Decoder_Criteria
import com.github.karlchan.beatthequeue.merchants.given_Encoder_Criteria
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.mongodb.client.result.UpdateResult
import io.circe.Decoder
import io.circe.Encoder
import io.circe.HCursor
import io.circe.Json
>>>>>>> master
import io.circe.generic.auto._
import mongo4cats.bson.ObjectId
import mongo4cats.circe._
import mongo4cats.client.MongoClient
import mongo4cats.collection.MongoCollection
import mongo4cats.collection.operations.Filter

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

  def findUser(authUser: AuthUser): IO[Models.User] =
    for {
      usersCollection <- users
      maybeUser <- usersCollection.find
        .filter(Filter.eq(Fields.Id, ObjectId(authUser.id)))
        .first
    } yield maybeUser.get

  def updateUser(
      authUser: AuthUser,
      updateFunction: Models.User => Models.User
  ): IO[UpdateResult] =
    for {
      usersCollection <- users
      maybeUser <- usersCollection.find
        .filter(Filter.eq(Fields.Id, ObjectId(authUser.id)))
        .first
      user = maybeUser.get
      newUser = updateFunction(user)
      res <- usersCollection.replaceOne(
        Filter.eq(Fields.Id, ObjectId(authUser.id)),
        newUser
      )
    } yield res

object Models:
  final case class User(
      _id: ObjectId,
      username: String,
      hash: String,
      criteria: Seq[Criteria[_]] = Seq.empty,
      notificationSettings: NotificationSettings = NotificationSettings()
  )

  final case class NotificationSettings(
      maybeEmailAddress: Option[String] = None,
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

object Fields:
  val Id = "_id"
  val Username = "username"
  val Criteria = "criteria"
  val CriteriaId = "id"
