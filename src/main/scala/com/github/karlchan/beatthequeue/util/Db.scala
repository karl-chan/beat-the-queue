package com.github.karlchan.beatthequeue.util

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.CineworldCriteria
import io.circe.generic.auto._
import mongo4cats.bson.ObjectId
import mongo4cats.circe._
import mongo4cats.client.MongoClient
import mongo4cats.collection.MongoCollection

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

object Models:
  final case class User(
      _id: ObjectId,
      username: String,
      hash: String,
      alerts: Alerts = Alerts()
  )

  final case class Alerts(
      cineworld: Seq[CineworldCriteria] = Seq.empty
  )
