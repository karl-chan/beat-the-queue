package com.github.karlchan.beatthequeue.server

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.github.karlchan.beatthequeue.server.routes.htmlRoutes
import com.github.karlchan.beatthequeue.server.routes.userRoutes
import com.github.karlchan.beatthequeue.util.Properties
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends IOApp:

  private val Port: Int =
    sys.env
      .get("PORT")
      .map(_.toInt)
      .getOrElse(Properties.getInt("server.port"))

  private val app = Router(
    "/api" -> Router(
      "/user" -> userRoutes
    ),
    "" -> htmlRoutes
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(Port, "0.0.0.0")
      .withHttpApp(app)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
