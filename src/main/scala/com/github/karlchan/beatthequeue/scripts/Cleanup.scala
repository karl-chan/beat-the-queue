package com.github.karlchan.beatthequeue.scripts

import java.time.LocalDateTime

import cats.effect.ExitCode
import cats.effect.IO
import cats.syntax.all._
import com.github.karlchan.beatthequeue.util.Db
import com.github.karlchan.beatthequeue.util.Models
import com.github.karlchan.beatthequeue.util.given_Db
import com.softwaremill.quicklens.modify
import org.typelevel.log4cats.slf4j.Slf4jLogger

def cleanup()(using db: Db): IO[ExitCode] =
  for {
    logger <- Slf4jLogger.create[IO]

    allUsers <- getAllUsers()
    _ <- logger.info("Got all users.")

    _ <- cleanup(allUsers)
    _ <- logger.info("Deleted expired notifications for all users.")
  } yield ExitCode.Success

private def getAllUsers()(using db: Db): IO[Seq[Models.User]] =
  for {
    usersCollection <- db.users
    users <- usersCollection.find.all
  } yield users.toSeq

private def cleanup(users: Seq[Models.User])(using
    db: Db
): IO[_] =
  users.parTraverse(user =>
    db.updateUser(user._id.toString(), deleteExpiredNotifications)
  )

def deleteExpiredNotifications(user: Models.User): Models.User =
  user
    .modify(_.notifications)
    .using(_.filter(_.event.time.isAfter(LocalDateTime.now())))
