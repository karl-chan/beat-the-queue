package com.github.karlchan.beatthequeue.server.routes.api

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.given_EntityDecoder_IO_Criteria
import com.github.karlchan.beatthequeue.server.auth.Auth
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Fields
import com.github.karlchan.beatthequeue.util.given_Db
import com.mongodb.client.model.Filters
import com.mongodb.client.result.UpdateResult
import org.http4s._
import org.http4s.dsl.io._
import tsec.authentication.TSecAuthService
import tsec.authentication.asAuthed

import scala.collection.mutable.ArrayBuffer

import collection.JavaConverters._

private val privateRoutes: HttpRoutes[IO] = Auth.service(
  TSecAuthService {
    case req @ POST -> Root / "criteria" asAuthed user =>
      for {
        criteria <- req.request.as[Criteria[_]]
        _ <- upsertCriteria(user, criteria)
        res <- Ok()
      } yield res
    case req @ DELETE -> Root / "criteria" asAuthed user =>
      req.request.params.get("id") match {
        case None => BadRequest("Missing id field!")
        case Some(criteriaId) =>
          for {
            _ <- deleteCriteria(user, criteriaId)
            res <- Ok()
          } yield res
      }
  }
)

val userRoutes: HttpRoutes[IO] = privateRoutes

private def upsertCriteria(authUser: AuthUser, criteria: Criteria[_])(using
    db: Db
): IO[UpdateResult] =
  for {
    res <- db.updateUser(
      authUser,
      user =>
        user.copy(criteria =
          criteria +: user.criteria.filterNot(_.id == criteria.id)
        )
    )
  } yield res

private def deleteCriteria(authUser: AuthUser, criteriaId: String)(using
    db: Db
): IO[UpdateResult] =
  for {
    res <- db.updateUser(
      authUser,
      user => user.copy(criteria = user.criteria.filterNot(_.id == criteriaId))
    )
  } yield res
