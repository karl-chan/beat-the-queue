package com.github.karlchan.beatthequeue.util.middleware

import cats.effect.IO
import cats.effect.std.Semaphore
import sttp.capabilities.Effect
import sttp.client3.DelegateSttpBackend
import sttp.client3.Request
import sttp.client3.Response
import sttp.client3.SttpBackend

class UserAgentBackend[P](
    delegate: SttpBackend[IO, P]
) extends DelegateSttpBackend[IO, P](delegate):

  override def send[T, R >: P with Effect[IO]](
      request: Request[T, R]
  ): IO[Response[T]] =
    delegate.send(
      request.header(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
      )
    )
