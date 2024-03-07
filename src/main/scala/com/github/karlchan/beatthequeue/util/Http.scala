package com.github.karlchan.beatthequeue.util

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration

import cats.effect.IO
import cats.effect.std.Semaphore
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import com.github.karlchan.beatthequeue.util.middleware.FollowRedirectsBackend
import com.github.karlchan.beatthequeue.util.middleware.RetryingBackend
import com.github.karlchan.beatthequeue.util.middleware.ThrottleBackend
import com.github.karlchan.beatthequeue.util.middleware.UserAgentBackend
import com.linecorp.armeria.client.ClientFactory
import com.linecorp.armeria.client.WebClient
import com.linecorp.armeria.client.encoding.DecodingClient
import io.circe.Decoder
import sttp.client3.Request
import sttp.client3.Response
import sttp.client3.ResponseAs
import sttp.client3.armeria.cats.ArmeriaCatsBackend
import sttp.client3.asStringAlways
import sttp.client3.basicRequest
import sttp.client3.circe.asJson
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.model.HeaderNames
import sttp.model.Uri
import sttp.model.headers.CookieWithMeta

final class Http(
    maxParallelism: Int = Properties.getInt("http.max.parallelism"),
    maxRetries: Int = Properties.getInt("http.max.retries"),
    retryDelay: FiniteDuration =
      Properties.getInt("http.retry.delay.ms").milliseconds,
    persistCookies: Boolean = false
)(using httpConnection: HttpConnection):

  def getHtml(
      uri: Uri,
      headers: Map[String, String] = Map.empty,
      cookies: Seq[CookieWithMeta] = Seq.empty
  ): IO[String] =
    request(
      basicRequest.get(uri).headers(headers).cookies(cookies),
      asStringAlways
    ).map(_.body)

  def get[R](
      uri: Uri,
      headers: Map[String, String] = Map.empty,
      cookies: Seq[CookieWithMeta] = Seq.empty
  )(using
      d: Decoder[R]
  ): IO[R] =
    request(
      basicRequest.get(uri).headers(headers).cookies(cookies),
      asJson[R].getRight
    )
      .map(_.body)

  def getFullResponse(
      uri: Uri,
      headers: Map[String, String] = Map.empty,
      cookies: Seq[CookieWithMeta] = Seq.empty
  ): IO[Response[String]] =
    request(
      basicRequest.get(uri).headers(headers).cookies(cookies),
      asStringAlways
    )

  def postHtml(
      uri: Uri,
      body: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty,
      cookies: Seq[CookieWithMeta] = Seq.empty
  ): IO[String] =
    request(
      basicRequest.post(uri).body(body).headers(headers).cookies(cookies),
      asStringAlways
    )
      .map(_.body)

  def post[R](
      uri: Uri,
      body: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty,
      cookies: Seq[CookieWithMeta] = Seq.empty
  )(using
      d: Decoder[R]
  ): IO[R] =
    request(
      basicRequest.post(uri).body(body).headers(headers).cookies(cookies),
      asJson[R].getRight
    ).map(_.body)

  def inspectCookies: Vector[CookieWithMeta] = cookies.toVector

  private def request[R](
      req: Request[Either[String, String], Any],
      decodeFn: ResponseAs[R, Any]
  )(using d: Decoder[R]): IO[Response[R]] =
    val backendWithMiddleware =
      new FollowRedirectsBackend(
        RetryingBackend(
          ThrottleBackend(
            UserAgentBackend(
              Slf4jLoggingBackend(
                httpConnection.backend,
                logRequestBody = Logging.isDebug,
                logResponseBody = Logging.isDebug,
                sensitiveHeaders =
                  if Logging.isDebug then Set.empty
                  else HeaderNames.SensitiveHeaders
              )
            ),
            semaphore
          ),
          maxRetries,
          retryDelay
        )
      )

    var res = req
      .cookies(cookies)
      .response(decodeFn)
      .send(backendWithMiddleware)

    if (persistCookies) {
      res.map(r => {
        Http.this.cookies = Cookies.merge(Http.this.cookies, r.unsafeCookies)
        r
      })
    } else {
      res
    }

  private val semaphore = Semaphore[IO](maxParallelism).unsafeRunSync()

  private var cookies: Seq[CookieWithMeta] = Seq.empty

given HttpConnection = HttpConnection()
final class HttpConnection:
  val backend = ArmeriaCatsBackend.usingClient[IO](
    WebClient
      .builder()
      .decorator(
        DecodingClient
          .builder()
          .autoFillAcceptEncoding(false)
          .strictContentEncoding(true)
          .newDecorator()
      )
      .factory(ClientFactory.insecure())
      .build()
  )

  def close(): IO[Unit] = backend.close()
