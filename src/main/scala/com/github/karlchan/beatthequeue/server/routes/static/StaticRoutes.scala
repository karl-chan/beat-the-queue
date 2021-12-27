package com.github.karlchan.beatthequeue.server.routes.static

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.server.staticcontent.FileService
import org.http4s.server.staticcontent.fileService

val staticRoutes: HttpRoutes[IO] =
  fileService(FileService.Config("./static"))
