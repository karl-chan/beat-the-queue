package com.github.karlchan.beatthequeue.server.auth

import cats.Id
import cats.data.OptionT
import cats.effect.IO
import cats.effect.kernel.Sync
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.given_Db
import mongo4cats.collection.operations.Filter
import org.http4s.HttpRoutes
import org.http4s.Response
import tsec.authentication.AuthenticatedCookie
import tsec.authentication.BackingStore
import tsec.authentication.SecuredRequestHandler
import tsec.authentication.SignedCookieAuthenticator
import tsec.authentication.TSecAuthService
import tsec.authentication.TSecCookieSettings
import tsec.mac.jca.HMACSHA256
import tsec.mac.jca.MacSigningKey
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.HardenedSCrypt

import java.util.UUID
import scala.collection.mutable

import concurrent.duration.DurationInt

case class AuthUser(id: String)

type AuthService =
  TSecAuthService[AuthUser, AuthenticatedCookie[HMACSHA256, String], IO]

object Auth:
  def withFallThrough(service: AuthService): HttpRoutes[IO] =
    handler.liftWithFallthrough(service)

  def login(
      username: String,
      password: String,
      onSuccess: IO[Response[IO]],
      onFailure: IO[Response[IO]]
  )(using db: Db): IO[Response[IO]] =
    for {
      usersCollection <- db.users
      maybeDbUser <- usersCollection.find(Filter.eq("username", username)).first
      response <- maybeDbUser match {
        case None => onFailure
        case Some(dbUser) =>
          for {
            success <- HardenedSCrypt
              .checkpwBool[IO](password, PasswordHash(dbUser.hash))
            response <-
              if success then
                for {
                  cookie <- authenticator.create(username)
                  successResponse <- onSuccess
                } yield authenticator.embed(successResponse, cookie)
              else onFailure
          } yield response
      }
    } yield response

  private val authenticator =
    SignedCookieAuthenticator(
      settings = TSecCookieSettings(
        cookieName = "authCookie",
        secure = false,
        expiryDuration = 30.days,
        maxIdle = None
      ),
      tokenStore =
        InMemoryBackingStore[IO, UUID, AuthenticatedCookie[HMACSHA256, String]](
          _.id
        ),
      idStore = InMemoryBackingStore[IO, String, AuthUser](_.id),
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
