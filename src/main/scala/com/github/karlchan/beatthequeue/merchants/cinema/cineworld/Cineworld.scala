package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.util.Properties
import io.circe.generic.auto._
import io.circe.syntax._

final class Cineworld
    extends Merchant[Cineworld, CineworldCriteria, CineworldEvent]:
  override val name = Cineworld.Name
  override val logoUrl =
    "https://classic.cineworld.co.uk/xmedia/img/10108/logo.svg"
  override val eventFinder = CineworldCrawler()
  override val defaultCriteria = CineworldCriteria()
  override val renderer = CineworldRenderer()

object Cineworld:
  val Name = "Cineworld"
