package com.github.karlchan.beatthequeue.util

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import sttp.client3.Response
import sttp.model.Header
import sttp.model.HeaderNames
import sttp.model.headers.Cookie.SameSite
import sttp.model.headers.CookieWithMeta

object Cookies:
  def merge(
      oldCookies: Seq[CookieWithMeta],
      newCookies: Seq[CookieWithMeta]
  ): Seq[CookieWithMeta] =
    if (newCookies.isEmpty) {
      oldCookies
    } else {
      // The last cookie with the same name in the same domain takes precendence.
      val newCookiesDeduped =
        newCookies
          .groupMapReduce(c => (c.domain, c.name))(identity)((_, last) => last)
          .values
          .toVector
      val newCookieNames = newCookiesDeduped.map(_.name).toSet
      val mergedCookies = newCookiesDeduped
        ++ oldCookies.filterNot(cookie => newCookieNames.contains(cookie.name))
      mergedCookies.filter(_.expires.mapOrTrue(_.isAfter(Instant.now())))
    }

  def parse[R](r: Response[R]): Seq[CookieWithMeta] =
    r.headers(HeaderNames.SetCookie)
      .map(SttpPatch.parse)
      .map(
        _.fold(e => throw new RuntimeException(e), identity[CookieWithMeta])
      )

// The below is a fork of the sttp source code with my fixes, highlighted with "FIX".
private object SttpPatch:
  // https://tools.ietf.org/html/rfc6265#section-4.1.1
  /** Parse the cookie, represented as a header value (in the format:
    * `[name]=[value]; [directive]=[value]; ...`).
    */
  def parse(s: String): Either[String, CookieWithMeta] =
    def splitkv(kv: String): (String, Option[String]) =
      (kv.split("=", 2).map(_.trim): @unchecked) match {
        case Array(v1)     => (v1, None)
        case Array(v1, v2) => (v1, Some(v2))
      }

    val components = s.split(";").map(_.trim)
    val (first, other) = (components.head, components.tail)
    val (name, value) = splitkv(first)
    var result: Either[String, CookieWithMeta] = Right(
      CookieWithMeta.apply(name, value.getOrElse(""))
    )
    other.map(splitkv).map(t => (t._1, t._2)).foreach {
      case (ci"expires", Some(v)) =>
        parseHttpDate(v) match {
          case Right(expires) =>
            result = result.right.map(_.expires(Some(expires)))
          case Left(_) =>
            result = Left(
              s"Expires cookie directive is not a valid RFC1123 or RFC850 datetime: $v"
            )
        }
      case (ci"max-age", Some(v)) =>
        Try(v.toLong) match {
          case Success(maxAge) =>
            result = result.right.map(_.maxAge(Some(maxAge)))
          case Failure(_) =>
            result = Left(s"Max-Age cookie directive is not a number: $v")
        }
      case (ci"domain", v) =>
        result = result.right.map(_.domain(Some(v.getOrElse(""))))
      case (ci"path", v) =>
        result = result.right.map(_.path(Some(v.getOrElse(""))))
      case (ci"secure", _)   => result = result.right.map(_.secure(true))
      case (ci"httponly", _) => result = result.right.map(_.httpOnly(true))
      case (ci"samesite", Some(v)) =>
        v.trim match {
          case ci"lax" =>
            result = result.right.map(_.sameSite(Some(SameSite.Lax)))
          case ci"strict" =>
            result = result.right.map(_.sameSite(Some(SameSite.Strict)))
          case ci"none" =>
            result = result.right.map(_.sameSite(Some(SameSite.None)))
          case _ =>
            result =
              Left(s"Same-Site cookie directive is not an allowed value: $v")
        }
      case (k, v) => result = result.right.map(_.otherDirective((k, v)))
    }

    result

  def parseHttpDate(v: String): Either[String, Instant] =
    Try(Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(v))) match {
      case Success(r) => Right(r)
      case Failure(e) =>
        Try(parseRfc850DateTime(v)) match {
          case Success(r) => Right(r)
          case Failure(_) => Left(s"Invalid http date: $v (${e.getMessage})")
        }
    }

  def parseRfc850DateTime(v: String): Instant =
    val expiresParts = v.split(", ")
    if (expiresParts.length != 2)
      throw new Exception("There must be exactly one \", \"")
    if (
      !Rfc850WeekDays.contains(expiresParts(0).trim.toLowerCase(Locale.ENGLISH))
    )
      throw new Exception("String must start with weekday name")
    Instant.from(Rfc850DatetimeFormat.parse(expiresParts(1)))

  // FIX - Accept 2 digit year
  lazy val Rfc850DatetimeFormat =
    DateTimeFormatterBuilder()
      .appendPattern("dd-MMM-")
      .appendValueReduced(ChronoField.YEAR, 2, 4, 1970)
      .appendPattern(" HH:mm:ss zzz")
      .toFormatter(Locale.US);
  val Rfc850WeekDays = Set(
    "mon",
    "tue",
    "wed",
    "thu",
    "fri",
    "sat",
    "sun"
  ) // not private b/c of bin-compat
  implicit class StringInterpolations(sc: StringContext):
    class CaseInsensitiveStringMatcher {
      def unapply(other: String): Boolean =
        sc.parts.mkString.equalsIgnoreCase(other)
    }
    def ci: CaseInsensitiveStringMatcher = new CaseInsensitiveStringMatcher
