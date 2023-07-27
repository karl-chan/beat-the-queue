package com.github.karlchan.beatthequeue.merchants.cinema.picturehouse

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Merchant
import io.circe.generic.auto._
import io.circe.syntax._

final class Picturehouse
    extends Merchant[Picturehouse, PicturehouseCriteria, PicturehouseEvent]:
  override val name = Picturehouse.Name
  override val logoUrl =
    "https://s3picturehouses.s3.eu-central-1.amazonaws.com/settings/ph1563896910.png"
  override val eventFinder = PicturehouseCrawler()
  override val criteriaFactory = () => PicturehouseCriteria()
  override val renderer = PicturehouseRenderer()

object Picturehouse:
  val Name = "Picturehouse"
