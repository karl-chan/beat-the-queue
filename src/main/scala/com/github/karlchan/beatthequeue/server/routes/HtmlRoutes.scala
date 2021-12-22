package com.github.karlchan.beatthequeue.server.routes

import cats.data.Kleisli
import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.server.auth.Auth
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.github.karlchan.beatthequeue.server.routes.pages.HomePage
import com.github.karlchan.beatthequeue.server.routes.pages.auth.LoginPage
import com.github.karlchan.beatthequeue.server.routes.pages.auth.RegistrationPage
import com.github.karlchan.beatthequeue.server.routes.pages.testPage
import com.github.karlchan.beatthequeue.util.Properties
import com.github.karlchan.beatthequeue.util.given_Db
import org.http4s.Response
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.Location
import org.http4s.headers.`Content-Type`
import org.http4s.server.AuthMiddleware
import org.http4s.server.Router
import scalatags.Text.TypedTag
import scalatags.Text.all._
import tsec.authentication.TSecAuthService
import tsec.authentication.asAuthed

private val privateRoutes: HttpRoutes[IO] =
  Auth.service(TSecAuthService {
    case GET -> Root asAuthed user =>
      HomePage
        .render(user)
        .flatMap(Ok(_))
    case req @ GET -> Root / "logout" asAuthed user =>
      Auth.logout(req, onSuccess = redirectTo("/")),
  })

private val publicRoutes: HttpRoutes[IO] = HttpRoutes.of {
  case GET -> Root           => redirectTo("/login")
  case GET -> Root / "login" => Ok(LoginPage.success)
  case req @ POST -> Root / "login" =>
    req.decode[UrlForm] { m =>
      (m.getFirst("username"), m.getFirst("password")) match {
        case (Some(username), Some(password))
            if username.nonEmpty && password.nonEmpty =>
          Auth.login(
            username,
            password,
            onSuccess = redirectTo("/"),
            onFailure = Ok(LoginPage.failure)
          )
        case _ => Ok(LoginPage.failure)
      }
    }
  case GET -> Root / "register" => Ok(RegistrationPage.render())
  case req @ POST -> Root / "register" =>
    req.decode[UrlForm] { m =>
      (
        m.getFirst("username"),
        m.getFirst("password"),
        m.getFirst("confirmPassword")
      ) match {
        case (Some(username), Some(password), Some(confirmPassword))
            if username.nonEmpty && password.nonEmpty && confirmPassword.nonEmpty =>
          if password == confirmPassword then
            Auth.register(
              username,
              password,
              onSuccess = redirectTo("/"),
              onFailure = Kleisli { error =>
                Ok(RegistrationPage.renderFailure(error))
              }
            )
          else Ok(RegistrationPage.renderFailure("Passwords don't match"))
        case _ => Ok(RegistrationPage.renderFailure("Missing fields"))
      }
    }
}

private val testRoutes: HttpRoutes[IO] = HttpRoutes.of {
  case GET -> Root / "test" => Ok(testPage)
}

val htmlRoutes: HttpRoutes[IO] =
  if Properties.getBoolean("is.production") then privateRoutes <+> publicRoutes
  else privateRoutes <+> publicRoutes <+> testRoutes

// TODO: Migrate to http4s-scalatags when Scala 3 pom is available.
given EntityEncoder[IO, TypedTag[String]] =
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
