package com.github.karlchan.beatthequeue.server.routes

import cats.effect.IO
import com.github.karlchan.beatthequeue.server.routes.pages.homePage
import com.github.karlchan.beatthequeue.server.routes.pages.loginPage
import org.http4s.Response
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.Location
import org.http4s.headers.`Content-Type`
import scalatags.Text.TypedTag
import scalatags.Text.all._

val htmlRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
  case GET -> Root            => Ok(homePage)
  case GET -> Root / "login"  => Ok(loginPage)
  case POST -> Root / "login" => redirectTo("/")
  case GET -> Root / "logout" => redirectTo("/login")
}

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
