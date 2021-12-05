package com.github.karlchan.beatthequeue.scripts

import cats.effect.ExitCode
import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.merchants.Merchants

def crawl(): IO[ExitCode] =
  for {
    allEvents <- Merchants.AllList.parTraverse(_.eventFinder.run())
    _ <- IO(println(allEvents))
  } yield ExitCode.Success
