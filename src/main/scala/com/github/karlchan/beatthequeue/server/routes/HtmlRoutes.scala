package com.github.karlchan.beatthequeue.server.routes

import cats.data.Kleisli
import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.given_Decoder_Criteria
import com.github.karlchan.beatthequeue.server.auth.Auth
import com.github.karlchan.beatthequeue.server.auth.AuthUser
import com.github.karlchan.beatthequeue.server.routes.pages.CriteriaCatalogPage
import com.github.karlchan.beatthequeue.server.routes.pages.CriteriaEditPage
import com.github.karlchan.beatthequeue.server.routes.pages.HomePage
import com.github.karlchan.beatthequeue.server.routes.pages.auth.LoginPage
import com.github.karlchan.beatthequeue.server.routes.pages.auth.RegistrationPage
import com.github.karlchan.beatthequeue.server.routes.pages.testPage
import com.github.karlchan.beatthequeue.util.Models
import com.github.karlchan.beatthequeue.util.Properties
import com.github.karlchan.beatthequeue.util.given_Db
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
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

import java.io.File
import com.github.karlchan.beatthequeue.server.routes.pages.SettingsPage
import com.github.karlchan.beatthequeue.server.routes.pages.SettingsEditPage

private val privateRoutes: HttpRoutes[IO] =
  Auth.service(TSecAuthService {
    case GET -> Root asAuthed user =>
      HomePage
        .render(user)
        .flatMap(Ok(_))
    case req @ GET -> Root / "logout" asAuthed user =>
      Auth.logout(req, onSuccess = redirectTo("/"))

    // Merchant routes
    case GET -> Root / "criteria" / "catalog" asAuthed user =>
      Ok(CriteriaCatalogPage.render)
    case req @ GET -> Root / "criteria" / "edit" asAuthed user =>
      req.request.params
        .get("criteria")
        .map(criteriaString =>
          decode[Criteria[_]](criteriaString) match {
            case Left(err) =>
              BadRequest(
                s"$criteriaString is not a valid criteria!"
              )
            case Right(criteria) =>
              CriteriaEditPage
                .render(criteria)
                .flatMap(Ok(_))
          }
        )
        .getOrElse(
          redirectTo("/criteria/catalog")
        )

    // Settings route
    case GET -> Root / "settings" asAuthed user =>
      SettingsPage.render(user).flatMap(Ok(_))
    case GET -> Root / "settings" / "edit" asAuthed user =>
      SettingsEditPage.render(user).flatMap(Ok(_))
  })

private val publicRoutes: HttpRoutes[IO] = HttpRoutes.of {
  case GET -> Root           => redirectTo("/login")
  case GET -> Root / "login" => Ok(LoginPage.success)
  case req @ POST -> Root / "login" =>
    req.decode[UrlForm] { m =>
      (
        m.getFirst("username"),
        m.getFirst("password"),
        m.getFirst("pushSubscriptionJson")
      ) match {
        case (Some(username), Some(password), Some(pushSubscriptionJson))
            if username.nonEmpty && password.nonEmpty =>
          Auth.login(
            username,
            password,
            maybePushSubscription =
              decode[Models.PushSubscription](pushSubscriptionJson).toOption,
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
        m.getFirst("confirmPassword"),
        m.getFirst("pushSubscriptionJson")
      ) match {
        case (
              Some(username),
              Some(password),
              Some(confirmPassword),
              Some(pushSubscriptionJson)
            )
            if username.nonEmpty && password.nonEmpty && confirmPassword.nonEmpty =>
          if password == confirmPassword then
            Auth.register(
              username,
              password,
              maybePushSubscription =
                decode[Models.PushSubscription](pushSubscriptionJson).toOption,
              onSuccess = redirectTo("/"),
              onFailure = Kleisli { error =>
                Ok(RegistrationPage.renderFailure(error))
              }
            )
          else Ok(RegistrationPage.renderFailure("Passwords don't match"))
        case _ => Ok(RegistrationPage.renderFailure("Missing fields"))
      }
    }

  case _ => redirectTo("/")
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
