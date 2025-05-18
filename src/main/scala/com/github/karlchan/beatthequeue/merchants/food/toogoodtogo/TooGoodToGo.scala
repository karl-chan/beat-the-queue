package com.github.karlchan.beatthequeue.merchants.food.toogoodtogo

import cats.effect.IO
import cats.implicits.*
import com.github.karlchan.beatthequeue.merchants.Merchant
import io.circe.generic.auto.*
import io.circe.syntax.*

final class TooGoodToGo
    extends Merchant[TooGoodToGo, TooGoodToGoCriteria, TooGoodToGoEvent]:
  override val name = TooGoodToGo.Name
  override val logoUrl =
    "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9e/TGTG_Logo_green_RGB.svg/1280px-TGTG_Logo_green_RGB.svg.png"
  override val eventFinder = TooGoodToGoCrawler()
  override val criteriaFactory = () => TooGoodToGoCriteria()
  override val renderer = TooGoodToGoRenderer()

object TooGoodToGo:
  val Name = "Too Good To Go"
