package com.github.karlchan.beatthequeue.server

import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all._
import com.github.karlchan.beatthequeue.server.routes.api.serviceWorkerRoutes
import com.github.karlchan.beatthequeue.server.routes.api.userRoutes
import com.github.karlchan.beatthequeue.server.routes.htmlRoutes
import com.github.karlchan.beatthequeue.server.routes.static.staticRoutes
import com.github.karlchan.beatthequeue.util.Properties
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.GZip

object Server extends IOApp:

  private val Port: Int =
    sys.env
      .get("PORT")
      .map(_.toInt)
      .getOrElse(Properties.getInt("server.port"))

  private val app = Router(
    "/api" -> Router(
      "/service-worker" -> serviceWorkerRoutes,
      "/user" -> userRoutes
    ),
    "/static" -> staticRoutes,
    "" -> htmlRoutes
  ).orNotFound

  private val middleware = GZip

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(Port, "0.0.0.0")
      .withHttpApp(middleware(app))
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
