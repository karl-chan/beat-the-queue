package com.github.karlchan.beatthequeue

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.github.karlchan.beatthequeue.scripts.crawl

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    val command = parseArgs(args)
    command match
      case Command.Help =>
        printHelp()
      case Command.Crawl =>
        crawl()

  private def parseArgs(args: List[String]): Command =
    args match
      case "crawl" :: rest => Command.Crawl
      case _               => Command.Help

  private def printHelp(): IO[ExitCode] =
    IO(println("Usage: crawl")).as(ExitCode.Error)

enum Command:
  case Crawl, Help
