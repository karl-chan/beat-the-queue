package com.github.karlchan.beatthequeue.server.routes

import cats.effect.IO
import org.http4s.Charset
import org.http4s.EntityEncoder
import org.http4s.HttpRoutes
import org.http4s.MediaType
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import scalatags.Text.TypedTag
import scalatags.Text.all._

val htmlRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root =>
  Ok(homePage)
}

def homePage = html(
  head(),
  body(
    div("Hello world")
  )
)

// TODO: Migrate to http4s-scalatags when Scala 3 pom is available.
private given EntityEncoder[IO, TypedTag[String]] =
  EntityEncoder
    .stringEncoder[IO]
    .contramap[TypedTag[String]](content => content.render)
    .withContentType(`Content-Type`(MediaType.text.html, Charset.`UTF-8`))
