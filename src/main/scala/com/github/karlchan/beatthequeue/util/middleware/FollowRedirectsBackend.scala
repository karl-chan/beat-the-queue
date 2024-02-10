package com.github.karlchan.beatthequeue.util.middleware

import cats.effect.IO
import com.github.karlchan.beatthequeue.util.Cookies
import sttp.capabilities.Effect
import sttp.client3.DelegateSttpBackend
import sttp.client3.Identity
import sttp.client3.NoBody
import sttp.client3.Request
import sttp.client3.Response
import sttp.client3.SttpBackend
import sttp.client3.UriContext
import sttp.model.Header
import sttp.model.HeaderNames
import sttp.model.Method
import sttp.model.StatusCode
import sttp.model.Uri
import sttp.model.headers.CookieWithMeta

/** @param transformUri
  *   Defines if and how [[Uri]] s from the `Location` header should be
  *   transformed. For example, this enables changing the encoding of host,
  *   path, query and fragment segments to be more strict or relaxed.
  */
class FollowRedirectsBackend[P](
    delegate: SttpBackend[IO, P],
    contentHeaders: Set[String] = HeaderNames.ContentHeaders,
    sensitiveHeaders: Set[String] = HeaderNames.SensitiveHeaders,
    transformUri: Uri => Uri = FollowRedirectsBackend.DefaultUriTransform
) extends DelegateSttpBackend[IO, P](delegate) {

  // this is needed to maintain binary compatibility with 3.3.14 and earlier
  def this(
      delegate: SttpBackend[IO, P],
      contentHeaders: Set[String],
      sensitiveHeaders: Set[String]
  ) =
    this(
      delegate,
      contentHeaders,
      sensitiveHeaders,
      FollowRedirectsBackend.DefaultUriTransform
    )

  type PE = P with Effect[IO]

  override def send[T, R >: PE](request: Request[T, R]): IO[Response[T]] = {
    sendWithCounter(request, 0, Seq.empty)
  }

  private def sendWithCounter[T, R >: PE](
      request: Request[T, R],
      redirects: Int,
      cookies: Seq[CookieWithMeta]
  ): IO[Response[T]] = {
    // if there are nested follow redirect backends, disabling them and handling redirects here
    val resp = delegate.send(request.followRedirects(false))
    if (request.options.followRedirects) {
      responseMonad.flatMap(resp) { (response: Response[T]) =>
        if (response.isRedirect) {
          followRedirect(request, response, redirects, cookies)
        } else {
          val responseWithCookies = response.copy(
            headers = cookies.map(Header.setCookie(_)) ++ response.headers
          )
          responseMonad.unit(responseWithCookies)
        }
      }
    } else {
      resp
    }
  }

  private def followRedirect[T, R >: PE](
      request: Request[T, R],
      response: Response[T],
      redirects: Int,
      cookies: Seq[CookieWithMeta]
  ): IO[Response[T]] = {
    response.header(HeaderNames.Location).fold(responseMonad.unit(response)) {
      loc =>
        if (redirects >= request.options.maxRedirects) {
          responseMonad.error(TooManyRedirectsException(request.uri, redirects))
        } else {
          followRedirect(request, response, redirects, loc, cookies)
        }
    }
  }

  private def followRedirect[T, R >: PE](
      request: Request[T, R],
      response: Response[T],
      redirects: Int,
      loc: String,
      cookies: Seq[CookieWithMeta]
  ): IO[Response[T]] = {
    val uri = if (FollowRedirectsBackend.isRelative(loc)) {
      transformUri(request.uri.resolve(uri"$loc"))
    } else {
      transformUri(uri"$loc")
    }
    val newCookies = Cookies.merge(cookies, response.unsafeCookies)

    val redirectResponse =
      ((stripSensitiveHeaders[T, R](_)) andThen
        (setNewCookies[T, R](_, newCookies)) andThen
        (changePostPutToGet[T, R](_, response.code)) andThen
        (sendWithCounter(_, redirects + 1, newCookies)))
        .apply(request.copy[Identity, T, R](uri = uri))

    responseMonad.map(redirectResponse) { rr =>
      val responseNoBody = response.copy(body = ())
      rr.copy(history = responseNoBody :: rr.history)
    }
  }

  private def setNewCookies[T, R](
      request: Request[T, R],
      cookies: Seq[CookieWithMeta]
  ): Request[T, R] = {
    request
      .copy[Identity, T, R](headers =
        // Overwrite all existing cookies to prevent stale duplicates
        request.headers.filterNot(_.is(HeaderNames.Cookie))
      )
      .cookies(cookies)
  }

  private def stripSensitiveHeaders[T, R](
      request: Request[T, R]
  ): Request[T, R] = {
    request.copy[Identity, T, R](
      headers = request.headers.filterNot(h =>
        sensitiveHeaders.contains(h.name.toLowerCase())
      )
    )
  }

  private def changePostPutToGet[T, R](
      r: Request[T, R],
      statusCode: StatusCode
  ): Request[T, R] = {
    val applicable = r.method == Method.POST || r.method == Method.PUT
    val alwaysChanged = statusCode == StatusCode.SeeOther
    val neverChanged =
      statusCode == StatusCode.TemporaryRedirect || statusCode == StatusCode.PermanentRedirect
    if (
      applicable && (r.options.redirectToGet || alwaysChanged) && !neverChanged
    ) {
      // when transforming POST or PUT into a get, content is dropped, also filter out content-related request headers
      r.method(Method.GET, r.uri)
        .copy(
          body = NoBody,
          headers = r.headers.filterNot(header =>
            contentHeaders.contains(header.name.toLowerCase())
          )
        )
    } else r
  }
}

object FollowRedirectsBackend {
  private val MaxRedirects = 32

  private val protocol = "^[a-z]+://.*".r

  private def isRelative(uri: String): Boolean = {
    val toCheck = uri.toLowerCase().trim
    !protocol.pattern.matcher(toCheck).matches()
  }

  /** By default, the conversion is a no-op */
  val DefaultUriTransform: Uri => Uri = (uri: Uri) => uri
}

case class TooManyRedirectsException(uri: Uri, redirects: Int) extends Exception
