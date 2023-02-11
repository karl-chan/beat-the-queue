package com.github.karlchan.beatthequeue.merchants.cinema.bfi

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.util.Properties
import io.circe.generic.auto._
import io.circe.syntax._

final class BFI extends Merchant[BFI, BFICriteria, BFIEvent]:
  override val name = BFI.Name
  override val logoUrl =
    "https://www.bfi.org.uk/dist/server/e51a86bb7ce82a9e9741a54d1f877a9c.svg"
  override val eventFinder = BFICrawler()
  override val defaultCriteria = BFICriteria()
  override val renderer = BFIRenderer()

object BFI:
  val Name = "BFI"
