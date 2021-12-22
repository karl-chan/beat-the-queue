package com.github.karlchan.beatthequeue.util

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.merchants.Merchants
import io.circe.Decoder
import io.circe.Encoder
import io.circe.HCursor
import io.circe.Json
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
      criteria: Seq[Criteria[_]]
  )

object Fields:
  val Id = "_id"
  val Username = "username"

private given [M]: Encoder[Criteria[M]] = new {
  final def apply(criteria: Criteria[M]): Json =
    val merchant = Merchants
      .AllByName(criteria.merchant)
      .asInstanceOf[Merchant[M, _]]
    merchant.criteriaEncoder.apply(criteria)
}

private given Decoder[Criteria[_]] = new {
  final def apply(c: HCursor): Decoder.Result[Criteria[_]] =
    for {
      name <- c.get[String]("merchant")
      merchant = Merchants.AllByName(name)
      result <- merchant.criteriaDecoder.apply(c)
    } yield result
}
