package com.github.karlchan.beatthequeue.server.auth

import java.time.Instant
import java.util.UUID

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

import cats.Id
import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.IO
import cats.effect.kernel.Sync
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Fields
import com.github.karlchan.beatthequeue.util.Models
import com.github.karlchan.beatthequeue.util.given_Db
import com.github.karlchan.beatthequeue.util.mapOrFalse
import com.softwaremill.quicklens.modify
import mongo4cats.bson.ObjectId
import mongo4cats.operations.Filter
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.Response
import tsec.authentication.AuthenticatedCookie
import tsec.authentication.BackingStore
import tsec.authentication.SecuredRequest
import tsec.authentication.SecuredRequestHandler
import tsec.authentication.SignedCookieAuthenticator
import tsec.authentication.TSecAuthService
import tsec.authentication.TSecCookieSettings
import tsec.common._
import tsec.hashing.jca._
import tsec.mac.jca.HMACSHA256

final case class AuthUser(
    id: String,
    maybePushSubscription: Option[Models.PushSubscription]
)

type AuthService =
  TSecAuthService[AuthUser, AuthCookie, IO]
private type AuthCookie = AuthenticatedCookie[HMACSHA256, String]

object Auth:
  def service(service: AuthService): HttpRoutes[IO] =
    handler.liftWithFallthrough(service)

  def register(
      username: String,
      password: String,
      maybePushSubscription: Option[Models.PushSubscription] = None,
      onSuccess: IO[Response[IO]],
      onFailure: Kleisli[IO, String, Response[IO]]
  )(using db: Db): IO[Response[IO]] =
    if password.length < 8 then onFailure("Password too short")
    else
      for {
        usersCollection <- db.users
        maybeExistingUser <- usersCollection.find
          .filter(Filter.eq(Fields.Username, username))
          .first
        response <- maybeExistingUser match {
          case Some(_) => onFailure("Username already taken")
          case None =>
            val hash = password.utf8Bytes.hash[SHA512].toB64String
            val userId = ObjectId()
            for {
              _ <- usersCollection.insertOne(
                Models.User(
                  _id = userId,
                  username = username,
                  hash = hash,
                  notificationSettings = Models.NotificationSettings(
                    pushSubscriptions = maybePushSubscription.toSeq
                  )
                )
              )
              cookie <- authenticator.create(userId.toString)
              successResponse <- onSuccess
              _ <- idStore.update(
                AuthUser(
                  id = userId.toString,
                  maybePushSubscription = maybePushSubscription
                )
              )
            } yield authenticator.embed(successResponse, cookie)
        }
      } yield response

  def login(
      username: String,
      password: String,
      maybePushSubscription: Option[Models.PushSubscription] = None,
      onSuccess: IO[Response[IO]],
      onFailure: IO[Response[IO]]
  )(using db: Db): IO[Response[IO]] =
    for {
      usersCollection <- db.users
      maybeDbUser <- usersCollection.find
        .filter(Filter.eq(Fields.Username, username))
        .first
      response <- maybeDbUser match {
        case None => onFailure
        case Some(dbUser) =>
          val success =
            password.utf8Bytes.hash[SHA512].toB64String == dbUser.hash
          for {
            response <-
              if success then
                for {
                  cookie <- authenticator.create(dbUser._id.toString)
                  successResponse <- onSuccess
                  _ <- idStore.update(
                    AuthUser(
                      id = dbUser._id.toString,
                      maybePushSubscription = maybePushSubscription
                    )
                  )
                  // Insert new push notification endpoint
                  _ <-
                    maybePushSubscription match {
                      case Some(pushSubscription)
                          if !dbUser.notificationSettings.pushSubscriptions
                            .contains(pushSubscription) =>
                        usersCollection.replaceOne(
                          Filter.eq(Fields.Username, username),
                          dbUser
                            .modify(_.notificationSettings.pushSubscriptions)
                            .using(pushSubscription +: _)
                        )
                      case _ => IO.unit
                    }

                } yield authenticator.embed(successResponse, cookie)
              else onFailure
          } yield response
      }
    } yield response

  def logout(
      securedRequest: SecuredRequest[IO, AuthUser, AuthCookie],
      onSuccess: IO[Response[IO]]
  )(using db: Db): IO[Response[IO]] =
    val SecuredRequest(_, authUser, cookie) = securedRequest
    for {
      _ <- authenticator.discard(cookie)
      _ <- idStore.delete(authUser.id)

      // Delete obsolete notification endpoint
      _ <- authUser.maybePushSubscription match {
        case Some(pushSubscription) =>
          db.updateUser(
            authUser,
            _.modify(_.notificationSettings.pushSubscriptions)
              .using(_.filterNot(_ == pushSubscription))
          )
        case None => IO.unit
      }

      res <- onSuccess
    } yield res.removeCookie(CookieName)

  private val CookieName = "authCookie"
  private val idStore = InMemoryBackingStore[IO, String, AuthUser](_.id)
  private val authenticator =
    SignedCookieAuthenticator(
      settings = TSecCookieSettings(
        cookieName = CookieName,
        secure = false,
        expiryDuration = 30.days,
        maxIdle = None
      ),
      tokenStore = InMemoryBackingStore[IO, UUID, AuthCookie](
        _.id
      ),
      idStore = idStore,
      key = HMACSHA256.generateKey[Id]
    )
  private val handler = SecuredRequestHandler(authenticator)

private class InMemoryBackingStore[F[_], I, V](getId: V => I)(implicit
    F: Sync[F]
) extends BackingStore[F, I, V]:
  private val storageMap = mutable.HashMap.empty[I, V]

  override def put(elem: V): F[V] = {
    val map = storageMap.put(getId(elem), elem)
    if (map.isEmpty)
      F.pure(elem)
    else
      F.raiseError(new IllegalArgumentException)
  }

  override def get(id: I): OptionT[F, V] =
    OptionT.fromOption[F](storageMap.get(id))

  override def update(v: V): F[V] = {
    storageMap.update(getId(v), v)
    F.pure(v)
  }

  override def delete(id: I): F[Unit] =
    storageMap.remove(id) match {
      case Some(_) => F.unit
      case None    => F.raiseError(new IllegalArgumentException)
    }
