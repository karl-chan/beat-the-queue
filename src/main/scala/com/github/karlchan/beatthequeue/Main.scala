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
        printHelp(args)
      case Command.Crawl =>
        crawl()

  private def parseArgs(args: List[String]): Command =
    args match
      case "crawl" :: rest => Command.Crawl
      case _               => Command.Help

  private def printHelp(args: List[String]): IO[ExitCode] =
    val message = s"""Usage: crawl

    but instead received args: $args"""
    IO(println(message)).as(ExitCode.Error)

enum Command:
  case Crawl, Help
