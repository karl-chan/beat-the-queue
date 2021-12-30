package com.github.karlchan.beatthequeue.server.routes.api

import cats.effect.IO
import com.github.karlchan.beatthequeue.util.Properties
import org.http4s.HttpRoutes
import org.http4s._
import org.http4s.dsl.io._

val serviceWorkerRoutes: HttpRoutes[IO] = HttpRoutes.of {
  case GET -> Root / "vapid-public-key" =>
    Ok(Properties.get("vapid.public.key"))

}
