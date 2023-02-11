package com.github.karlchan.beatthequeue.util.middleware

import cats.effect.IO
import cats.effect.std.Semaphore
import sttp.capabilities.Effect
import sttp.client3.DelegateSttpBackend
import sttp.client3.Request
import sttp.client3.Response
import sttp.client3.SttpBackend

final class ThrottleBackend[P](
    delegate: SttpBackend[IO, P],
    semaphore: Semaphore[IO]
) extends DelegateSttpBackend[IO, P](delegate):

  override def send[T, R >: P with Effect[IO]](
      request: Request[T, R]
  ): IO[Response[T]] =
    for {
      _ <- semaphore.acquire
      response <- delegate.send(request)
      _ <- semaphore.release
    } yield response
