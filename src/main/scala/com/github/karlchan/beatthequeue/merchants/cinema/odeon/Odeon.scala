package com.github.karlchan.beatthequeue.merchants.cinema.odeon

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.util.Properties
import io.circe.generic.auto._
import io.circe.syntax._

final class Odeon extends Merchant[Odeon, OdeonCriteria, OdeonEvent]:
  override val name = Odeon.Name
  override val logoUrl =
    "https://upload.wikimedia.org/wikipedia/commons/2/26/Odeon_logo.svg"
  override val eventFinder = OdeonCrawler()
  override val defaultCriteria = OdeonCriteria()
  override val renderer = OdeonRenderer()

object Odeon:
  val Name = "odeon"