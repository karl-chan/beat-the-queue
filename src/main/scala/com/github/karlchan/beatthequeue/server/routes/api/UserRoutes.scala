package com.github.karlchan.beatthequeue.server.routes.api

import cats.effect.IO
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Fields
import com.github.karlchan.beatthequeue.util.given_Db
import fs2.Stream
import mongo4cats.collection.operations.Filter
import org.http4s._
import org.http4s.dsl.io._
import mongo4cats.bson.ObjectId

val userRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
  case GET -> Root / userId => Ok(findUser(userId))

}

private def findUser(userId: String)(using db: Db): Stream[IO, String] =
  Stream
    .eval(
      db.users
        .map(_.find.filter(Filter.eq(Fields.Id, ObjectId(userId))).stream)
    )
    .flatten
    .map(_._id.toString)
