package com.github.karlchan.beatthequeue.util

import cats.effect.IO
import cats.effect.std.Semaphore
import cats.effect.unsafe.implicits.global
import cats.instances.parallel
import cats.syntax.all._
import com.github.karlchan.beatthequeue.util.middleware.RetryingBackend
import com.github.karlchan.beatthequeue.util.middleware.ThrottleBackend
import com.github.karlchan.beatthequeue.util.middleware.UserAgentBackend
import io.circe.Decoder
import sttp.client3.Request
import sttp.client3.Response
import sttp.client3.ResponseAs
import sttp.client3.asStringAlways
import sttp.client3.basicRequest
import sttp.client3.circe.asJson
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.model.Uri
import sttp.model.headers.CookieWithMeta

import scala.concurrent.ExecutionContext

final class Http(
    maxParallelism: Int = Properties.getInt("http.max.parallelism"),
    maxRetries: Int = Properties.getInt("http.max.retries"),
    retryDelay: Int = Properties.getInt("http.retry.delay.ms")
):

  def getHtml(uri: Uri): IO[String] =
    request(basicRequest.get(uri), asStringAlways).map(_.body)

  def get[R](uri: Uri)(using d: Decoder[R]): IO[R] =
    request(basicRequest.get(uri), asJson[R].getRight).map(_.body)

  def getFullResponse(uri: Uri): IO[Response[String]] =
    request(basicRequest.get(uri), asStringAlways)

  def postHtml(uri: Uri): IO[String] =
    request(basicRequest.post(uri), asStringAlways).map(_.body)

  def post[R](uri: Uri)(using d: Decoder[R]): IO[R] =
    request(basicRequest.post(uri), asJson[R].getRight).map(_.body)

  private def request[R](
      req: Request[Either[String, String], Any],
      decodeFn: ResponseAs[R, Any]
  )(using d: Decoder[R]): IO[Response[R]] =
    clientResource.use { backend =>
      val backendWithMiddleware =
        RetryingBackend(
          ThrottleBackend(
            Slf4jLoggingBackend(UserAgentBackend(backend)),
            semaphore
          ),
          maxRetries,
          retryDelay
        )
      req
        .response(decodeFn)
        .send(backendWithMiddleware)
    }

  private val clientResource = HttpClientCatsBackend.resource[IO]()

  private val semaphore = Semaphore[IO](maxParallelism).unsafeRunSync()
