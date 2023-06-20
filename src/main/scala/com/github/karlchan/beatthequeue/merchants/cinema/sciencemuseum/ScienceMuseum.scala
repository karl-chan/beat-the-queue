package com.github.karlchan.beatthequeue.merchants.cinema.sciencemuseum

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.util.Properties
import io.circe.generic.auto._
import io.circe.syntax._

final class ScienceMuseum
    extends Merchant[ScienceMuseum, ScienceMuseumCriteria, ScienceMuseumEvent]:
  override val name = ScienceMuseum.Name
  override val logoUrl =
    "https://logos-download.com/wp-content/uploads/2021/01/Science_Museum_Logo.png"
  override val eventFinder = ScienceMuseumCrawler()
  override val criteriaFactory = () => ScienceMuseumCriteria()
  override val renderer = ScienceMuseumRenderer()

object ScienceMuseum:
  val Name = "Science Museum"
