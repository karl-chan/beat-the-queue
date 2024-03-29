package com.github.karlchan.beatthequeue.server.routes.api

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

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
import com.softwaremill.quicklens.modify
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.dsl.io._
import tsec.authentication.TSecAuthService
import tsec.authentication.asAuthed

private val privateRoutes: HttpRoutes[IO] = Auth.service(
  TSecAuthService {
    case req @ POST -> Root / "criteria" asAuthed user =>
      for {
        criteria <- req.request.as[Criteria[?]]
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
    case req @ POST -> Root / "settings" asAuthed user =>
      for {
        settings <- req.request.as[Settings]
        _ <- updateSettings(user, settings)
        res <- Ok()
      } yield res
  }
)

val userRoutes: HttpRoutes[IO] = privateRoutes

private def upsertCriteria(authUser: AuthUser, criteria: Criteria[?])(using
    db: Db
): IO[UpdateResult] =
  for {
    res <- db.updateUser(
      authUser,
      _.modify(_.criteria).using(criteria +: _.filterNot(_.id == criteria.id))
    )
  } yield res

private def deleteCriteria(authUser: AuthUser, criteriaId: String)(using
    db: Db
): IO[UpdateResult] =
  for {
    res <- db.updateUser(
      authUser,
      _.modify(_.criteria).using(_.filterNot(_.id == criteriaId))
    )
  } yield res

private def updateSettings(authUser: AuthUser, settings: Settings)(using
    db: Db
): IO[UpdateResult] =
  for {
    res <- db.updateUser(
      authUser,
      _.modify(_.notificationSettings.emailAddresses)
        .setTo(settings.emailAddresses)
    )
  } yield res

private case class Settings(
    emailAddresses: Seq[String]
)
