package com.github.karlchan.beatthequeue.util

import cats.effect.IO
import cats.effect.std.Semaphore
import cats.effect.unsafe.implicits.global
import cats.instances.parallel
import cats.syntax.all._
import io.circe.Decoder
import sttp.capabilities.Effect
import sttp.client3.DelegateSttpBackend
import sttp.client3.Request
import sttp.client3.Response
import sttp.client3.RetryWhen
import sttp.client3.SttpBackend
import sttp.client3.basicRequest
import sttp.client3.circe.asJson
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.model.Uri

import scala.concurrent.ExecutionContext

import concurrent.duration.DurationInt

final class Http(
    maxParallelism: Int = Properties.getInt("http.max.parallelism"),
    maxRetries: Int = Properties.getInt("http.max.retries"),
    retryDelay: Int = Properties.getInt("http.retry.delay.ms")
):

  def get[R](s: String)(using d: Decoder[R]): IO[R] =
    get(Uri.unsafeParse(s))

  def get[R](uri: Uri)(using d: Decoder[R]): IO[R] =
    request(basicRequest.get(uri)).map(_.body)

  def post[R](uri: Uri)(using d: Decoder[R]): IO[R] =
    request(basicRequest.post(uri)).map(_.body)

  private def request[R](r: Request[Either[String, String], Any])(using
      d: Decoder[R]
  ): IO[Response[R]] =
    clientResource.use { backend =>
      val backendWithMiddleware =
        RetryingBackend(
          ThrottleBackend(
            Slf4jLoggingBackend(backend),
            semaphore
          ),
          maxRetries,
          retryDelay
        )
      r.response(asJson[R].getRight)
        .send(
          backendWithMiddleware
        )
    }

  private val clientResource =
    HttpClientCatsBackend.resource[IO]()

  private val semaphore = Semaphore[IO](maxParallelism).unsafeRunSync()

private class RetryingBackend[P](
    delegate: SttpBackend[IO, P],
    maxRetries: Int,
    retryDelay: Int,
    shouldRetry: RetryWhen = RetryWhen.Default
) extends DelegateSttpBackend[IO, P](delegate):

  override def send[T, R >: P with Effect[IO]](
      request: Request[T, R]
  ): IO[Response[T]] =
    sendWithRetryCounter(request, 0)

  private def sendWithRetryCounter[T, R >: P with Effect[IO]](
      request: Request[T, R],
      retries: Int
  ): IO[Response[T]] = {

    val r = responseMonad.handleError(delegate.send(request)) {
      case t if shouldRetry(request, Left(t)) && retries < maxRetries =>
        sendWithRetryCounter(request, retries + 1)
    }

    responseMonad.flatMap(r) { resp =>
      if (shouldRetry(request, Right(resp)) && retries < maxRetries) {
        IO.sleep(retryDelay.milliseconds)
        sendWithRetryCounter(request, retries + 1)
      } else {
        responseMonad.unit(resp)
      }
    }
  }

private class ThrottleBackend[P](
    delegate: SttpBackend[IO, P],
    semaphore: Semaphore[IO]
) extends DelegateSttpBackend[IO, P](delegate):

  type PE = P with Effect[IO]

  override def send[T, R >: PE](request: Request[T, R]): IO[Response[T]] =
    for {
      _ <- semaphore.acquire
      response <- delegate.send(request)
      _ <- semaphore.release
    } yield response
