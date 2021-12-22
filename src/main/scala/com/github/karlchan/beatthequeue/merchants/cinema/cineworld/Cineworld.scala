package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Codecs
import com.github.karlchan.beatthequeue.merchants.Merchant
import io.circe.generic.auto._
import io.circe.syntax._

final class Cineworld extends Merchant[Cineworld, CineworldCriteria]:
  override val name = Cineworld.Name
  override val eventFinder = CineworldCrawler()
  override val defaultCriteria = CineworldCriteria()

object Cineworld:
  val Name = "cineworld"
