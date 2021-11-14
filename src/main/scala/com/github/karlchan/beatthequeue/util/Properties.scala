package com.github.karlchan.beatthequeue.util

import pureconfig.ConfigSource

object Properties:
  def get(path: String): String =
    getFromEnv(path).orElse(getFromFile(path)) match
      case Left(err)    => throw err
      case Right(value) => value

  def getInt(path: String): Int = get(path).toInt

  def getDouble(path: String): Double = get(path).toDouble

  private def getFromFile(path: String): Either[Throwable, String] =
    ConfigSource
      .resources("application.secret.conf")
      .withFallback(
        ConfigSource
          .resources("application.conf")
      )
      .config
      .map(_.getString(path))
      .left
      .map(failure => Exception(failure.prettyPrint(2)))

  private def getFromEnv(path: String): Either[Throwable, String] =
    sys.env.get(path) match
      case None        => Left(Exception(s"[$path] not found in env!"))
      case Some(value) => Right(value)
