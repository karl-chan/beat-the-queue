package com.github.karlchan.beatthequeue.merchants.cinema.thecinema

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Merchant
import com.github.karlchan.beatthequeue.util.Properties
import io.circe.generic.auto._
import io.circe.syntax._

final class TheCinema
    extends Merchant[TheCinema, TheCinemaCriteria, TheCinemaEvent]:
  override val name = TheCinema.Name
  override val logoUrl =
    "https://www.thecinemainthepowerstation.com/assets/images/cinema_2/cinema-logo.png"
  override val eventFinder = TheCinemaCrawler()
  override val criteriaFactory = () => TheCinemaCriteria()
  override val renderer = TheCinemaRenderer()

object TheCinema:
  val Name = "The Cinema"
