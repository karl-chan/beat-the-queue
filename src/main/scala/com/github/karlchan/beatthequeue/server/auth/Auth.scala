package com.github.karlchan.beatthequeue.server.auth

import cats.Id
import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.IO
import cats.effect.kernel.Sync
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Fields
import com.github.karlchan.beatthequeue.util.Models
import com.github.karlchan.beatthequeue.util.given_Db
import mongo4cats.bson.ObjectId
import mongo4cats.collection.operations.Filter
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
import tsec.mac.jca.HMACSHA256
import tsec.mac.jca.MacSigningKey
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.HardenedSCrypt

import java.time.Instant
import java.util.UUID
import scala.collection.mutable

import concurrent.duration.DurationInt

final case class AuthUser(id: String)

type AuthService =
  TSecAuthService[AuthUser, AuthCookie, IO]
private type AuthCookie = AuthenticatedCookie[HMACSHA256, String]

object Auth:
  def service(service: AuthService): HttpRoutes[IO] =
    handler.liftWithFallthrough(service)

  def register(
      username: String,
      password: String,
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
            for {
              hash <- HardenedSCrypt.hashpw[IO](password)
              userId = ObjectId()
              _ <- usersCollection.insertOne(
                Models.User(
                  _id = userId,
                  username = username,
                  hash = hash,
                  criteria = Seq.empty
                )
              )
              cookie <- authenticator.create(userId.toString)
              successResponse <- onSuccess
              _ <- idStore.update(AuthUser(userId.toString))
            } yield authenticator.embed(successResponse, cookie)
        }
      } yield response

  def login(
      username: String,
      password: String,
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
          for {
            success <- HardenedSCrypt
              .checkpwBool[IO](password, PasswordHash(dbUser.hash))
            response <-
              if success then
                for {
                  cookie <- authenticator.create(dbUser._id.toString)
                  successResponse <- onSuccess
                  _ <- idStore.update(AuthUser(dbUser._id.toString))
                } yield authenticator.embed(successResponse, cookie)
              else onFailure
          } yield response
      }
    } yield response

  def logout(
      securedRequest: SecuredRequest[IO, AuthUser, AuthCookie],
      onSuccess: IO[Response[IO]]
  ): IO[Response[IO]] =
    val SecuredRequest(_, user, cookie) = securedRequest
    for {
      _ <- authenticator.discard(cookie)
      _ <- idStore.delete(user.id)
    } yield ()
    onSuccess.map(_.removeCookie(CookieName))

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
