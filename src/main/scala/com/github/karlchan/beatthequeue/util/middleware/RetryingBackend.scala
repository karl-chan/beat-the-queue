package com.github.karlchan.beatthequeue.util.middleware

import cats.effect.IO
import sttp.capabilities.Effect
import sttp.client3.DelegateSttpBackend
import sttp.client3.Request
import sttp.client3.Response
import sttp.client3.RetryWhen
import sttp.client3.SttpBackend
import sttp.client3.SttpClientException

import concurrent.duration.DurationInt

final class RetryingBackend[P](
    delegate: SttpBackend[IO, P],
    maxRetries: Int,
    retryDelay: Int
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

  private val shouldRetry: RetryWhen = {
    case (_, Left(_: SttpClientException.ReadException)) => true
    case (request, eitherResponse) => RetryWhen.Default(request, eitherResponse)
  }
