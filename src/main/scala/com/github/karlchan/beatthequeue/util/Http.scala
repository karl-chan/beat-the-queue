package com.github.karlchan.beatthequeue.util

import cats.effect.Concurrent
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.std.Hotswap
import cats.effect.std.Semaphore
import cats.instances.parallel
import cats.syntax.all._
import org.http4s.EntityDecoder
import org.http4s.Method
import org.http4s.Request
import org.http4s.Response
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.client.middleware.FollowRedirect
import org.http4s.client.middleware.GZip
import org.http4s.client.middleware.RequestLogger
import org.http4s.client.middleware.Retry

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class Http(
    maxParallelism: Int = Properties.getInt("http.max.parallelism"),
    maxRedirects: Int = Properties.getInt("http.max.redirects"),
    maxRetries: Int = Properties.getInt("http.max.retries"),
    retryDelayMs: Int = Properties.getInt("http.retry.delay.ms")
):

  def get[R](s: String)(using d: EntityDecoder[IO, R]): IO[R] =
    get(Uri.unsafeFromString(s))

  def get[R](uri: Uri)(using d: EntityDecoder[IO, R]): IO[R] =
    request(Request(method = Method.GET, uri = uri))

  def post[R](uri: Uri)(using d: EntityDecoder[IO, R]): IO[R] =
    request(Request(method = Method.POST, uri = uri))

  private def request[R](r: Request[IO])(using d: EntityDecoder[IO, R]): IO[R] =
    clientResource.use {
      middleware(_).expect[R](r)
    }

  private val clientResource = BlazeClientBuilder[IO](global).resource

  private val middleware =
    FollowRedirect[IO](maxRedirects) andThen
      Retry[IO]((req, res, numTries) =>
        if res.isLeft && numTries < maxRedirects then
          Some(FiniteDuration(retryDelayMs, TimeUnit.MILLISECONDS))
        else None
      ) andThen
      Throttle(Semaphore[IO](maxParallelism)) andThen
      GZip() andThen
      RequestLogger(logHeaders = true, logBody = false)

private object Throttle:
  def apply(
      semaphore: IO[Semaphore[IO]]
  )(client: Client[IO]): Client[IO] =

    def throttle(
        req: Request[IO],
        hotswap: Hotswap[IO, Response[IO]]
    ): IO[Response[IO]] =
      for {
        // TODO: Fix semaphore dereferencing returning a different instance each time.
        s <- semaphore
        _ <- s.acquire
        res <- hotswap.swap(client.run(req))
        _ <- s.release
      } yield res

    Client { req =>
      Hotswap.create[IO, Response[IO]].flatMap { case hotswap =>
        Resource.eval(throttle(req, hotswap))
      }
    }
