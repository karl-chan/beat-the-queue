package com.github.karlchan.beatthequeue.util

import com.typesafe.config.Config
import io.circe.parser.parse
import pureconfig.ConfigSource

import scala.jdk.CollectionConverters._

object Properties:
  def get[V](
      path: String,
      parseEnv: String => V = identity,
      parseConfig: (Config, String) => V = _.getString(_)
  ): V =
    getFromEnv(path, parseEnv).orElse(getFromFile(path, parseConfig)) match
      case Left(err)    => throw err
      case Right(value) => value

  def getBoolean(path: String): Boolean =
    get(path, _.toBoolean, _.getBoolean(_))

  def getInt(path: String): Int = get(path, _.toInt, _.getInt(_))

  def getDouble(path: String): Double = get(path, _.toDouble, _.getDouble(_))

  def getList(path: String): Vector[String] =
    get(
      path,
      parse(_).flatMap(_.as[Vector[String]]) match {
        case Right(list) => list
        case Left(err)   => throw err
      },
      _.getStringList(_).asScala.toVector
    )

  private def getFromFile[V](
      path: String,
      parseConfig: (Config, String) => V
  ): Either[Throwable, V] =
    ConfigSource
      .resources("application.secret.conf")
      .withFallback(
        ConfigSource
          .resources("application.conf")
      )
      .config
      .map(parseConfig(_, path))
      .left
      .map(failure => Exception(failure.prettyPrint(2)))

  private def getFromEnv[V](
      path: String,
      parseEnv: String => V
  ): Either[Throwable, V] =
    sys.env.get(path) match
      case None        => Left(Exception(s"[$path] not found in env!"))
      case Some(value) => Right(parseEnv(value))
