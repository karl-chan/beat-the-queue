package com.github.karlchan.beatthequeue.merchants.attraction.horizon22

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Merchant
import io.circe.generic.auto._
import io.circe.syntax._

final class Horizon22
    extends Merchant[Horizon22, Horizon22Criteria, Horizon22Event]:
  override val name = Horizon22.Name
  override val logoUrl =
    "https://cdn.sanity.io/images/t96hxkdf/production/6501ab92171d8138b81744d111b919ed6722ba0a-138x40.svg"
  override val eventFinder = Horizon22Crawler()
  override val criteriaFactory = () => Horizon22Criteria()
  override val renderer = Horizon22Renderer()

object Horizon22:
  val Name = "Horizon22"
