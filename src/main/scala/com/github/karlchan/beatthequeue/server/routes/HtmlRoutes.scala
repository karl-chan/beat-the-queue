package com.github.karlchan.beatthequeue.server.routes

import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.server.auth.Auth
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.github.karlchan.beatthequeue.server.routes.pages.auth.loginFailedPage
import com.github.karlchan.beatthequeue.server.routes.pages.auth.loginPage
import com.github.karlchan.beatthequeue.server.routes.pages.homePage
import com.github.karlchan.beatthequeue.util.given_Db
import org.http4s.Response
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.Location
import org.http4s.headers.`Content-Type`
import org.http4s.server.AuthMiddleware
import scalatags.Text.TypedTag
import scalatags.Text.all._
import tsec.authentication.TSecAuthService
import tsec.authentication.asAuthed

private val privateRoutes: HttpRoutes[IO] =
  Auth.withFallThrough(TSecAuthService {
    case GET -> Root asAuthed user            => Ok(homePage)
    case GET -> Root / "logout" asAuthed user => redirectTo("/login")
  })

private val publicRoutes: HttpRoutes[IO] = HttpRoutes.of {
  case GET -> Root           => redirectTo("/login")
  case GET -> Root / "login" => Ok(loginPage)
  case req @ POST -> Root / "login" =>
    req.decode[UrlForm] { m =>
      (m.getFirst("username"), m.getFirst("password")) match {
        case (Some(username), Some(password)) =>
          Auth.login(
            username,
            password,
            onSuccess = Ok(loginPage),
            onFailure = Ok(loginFailedPage)
          )
        case _ => Ok(loginFailedPage)
      }
    }
}

val htmlRoutes: HttpRoutes[IO] = privateRoutes <+> publicRoutes

// TODO: Migrate to http4s-scalatags when Scala 3 pom is available.
private given EntityEncoder[IO, TypedTag[String]] =
  EntityEncoder
    .stringEncoder[IO]
    .contramap[TypedTag[String]](content => content.render)
    .withContentType(`Content-Type`(MediaType.text.html, Charset.`UTF-8`))

private def redirectTo(location: String): IO[Response[IO]] =
  IO.pure(
    Response[IO]()
      .withStatus(Status.Found)
      .withHeaders(Location(Uri.unsafeFromString(location)))
  )
