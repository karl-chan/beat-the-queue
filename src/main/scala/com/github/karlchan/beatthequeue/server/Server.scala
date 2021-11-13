package com.github.karlchan.beatthequeue.server

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.github.karlchan.beatthequeue.util.Properties
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(Properties.getInt("server.port"), "localhost")
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
