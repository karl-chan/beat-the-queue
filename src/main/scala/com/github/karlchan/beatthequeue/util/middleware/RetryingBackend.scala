package com.github.karlchan.beatthequeue.util.middleware

import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration

import cats.effect.IO
import org.typelevel.log4cats.slf4j.Slf4jLogger
import sttp.capabilities.Effect
import sttp.client3.DelegateSttpBackend
import sttp.client3.Request
import sttp.client3.Response
import sttp.client3.RetryWhen
import sttp.client3.SttpBackend
import sttp.client3.SttpClientException

final class RetryingBackend[P](
    delegate: SttpBackend[IO, P],
    maxRetries: Int,
    retryDelay: FiniteDuration
) extends DelegateSttpBackend[IO, P](delegate):

  override def send[T, R >: P with Effect[IO]](
      request: Request[T, R]
  ): IO[Response[T]] =
    sendWithRetryCounter(request, 0)

  private def sendWithRetryCounter[T, R >: P with Effect[IO]](
      request: Request[T, R],
      retries: Int
  ): IO[Response[T]] = {

    def logWarningAndRetry(): IO[Response[T]] =
      for {
        logger <- Slf4jLogger.create[IO]
        _ <- logger.warn(
          s"Retrying ${request.method} request to ${request.uri} on failed attempt ${retries + 1} out of ${maxRetries}. Waiting ${retryDelay.toMillis}ms..."
        )
        _ <- IO.sleep(retryDelay)
        res <- sendWithRetryCounter(request, retries + 1)
      } yield res

    val r = responseMonad.handleError(delegate.send(request)) {
      case t if shouldRetry(request, Left(t)) && retries < maxRetries =>
        logWarningAndRetry()
    }

    responseMonad.flatMap(r) { resp =>
      if (shouldRetry(request, Right(resp)) && retries < maxRetries) {
        logWarningAndRetry()
      } else {
        responseMonad.unit(resp)
      }
    }
  }

  private val shouldRetry: RetryWhen = {
    case (_, Left(_: SttpClientException.ReadException)) => true
    case (request, eitherResponse) => RetryWhen.Default(request, eitherResponse)
  }
