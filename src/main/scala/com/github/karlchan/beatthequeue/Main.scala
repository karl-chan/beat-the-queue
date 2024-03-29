package com.github.karlchan.beatthequeue

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.github.karlchan.beatthequeue.scripts.cleanup
import com.github.karlchan.beatthequeue.scripts.crawl
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.HttpConnection
import com.github.karlchan.beatthequeue.util.given_Db
import com.github.karlchan.beatthequeue.util.given_HttpConnection
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    val command = parseArgs(args)
    for {
      exitCode <- runCommand(command, args)
      _ <- shutdown()
    } yield exitCode

  private def parseArgs(args: List[String]): Command =
    args match
      case "crawl" :: rest   => Command.Crawl
      case "cleanup" :: rest => Command.Cleanup
      case "loop-crawl" :: rest   => Command.LoopCrawl
      case _                 => Command.Help

  private def printHelp(args: List[String]): IO[ExitCode] =
    val message = s"""Usage: crawl, cleanup, loop-crawl

    but instead received args: $args"""
    IO.println(message).as(ExitCode.Error)

  private def runCommand(command: Command, args: List[String]): IO[ExitCode] =
    command match
      case Command.Help =>
        printHelp(args)
      case Command.Crawl =>
        crawl()
      case Command.Cleanup =>
        cleanup()
      case Command.LoopCrawl =>
        crawl().foreverM

  private def shutdown()(using
      db: Db,
      httpConnection: HttpConnection
  ): IO[Unit] =
    for {
      logger <- Slf4jLogger.create[IO]
      _ <- db.close()
      _ <- logger.info("Shutdown db.")
      _ <- httpConnection.close()
      _ <- logger.info("Shutdown http connection.")
      // Hack to force shutdown because of AsyncHttpClient non-daemon threads
      _ <- logger.info("Terminating JVM...")
      _ <- IO(Runtime.getRuntime.halt(0))
    } yield ()

enum Command:
  case Crawl, Cleanup, LoopCrawl, Help
