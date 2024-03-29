package com.github.karlchan.beatthequeue.merchants

import com.github.karlchan.beatthequeue.merchants.attraction.horizon22.Horizon22
import com.github.karlchan.beatthequeue.merchants.cinema.bfi.BFI
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.Cineworld
import com.github.karlchan.beatthequeue.merchants.cinema.odeon.Odeon
import com.github.karlchan.beatthequeue.merchants.cinema.picturehouse.Picturehouse
import com.github.karlchan.beatthequeue.merchants.cinema.sciencemuseum.ScienceMuseum
import com.github.karlchan.beatthequeue.merchants.cinema.thecinema.TheCinema
import com.github.karlchan.beatthequeue.merchants.cinema.vue.Vue
import com.github.karlchan.beatthequeue.merchants.haircut.lsb.LSB

private type Category = String

object Merchants:
  val All: Map[Category, Seq[Merchant[?, ?, ?]]] = Map(
    "Attraction" -> Seq(
      Horizon22()
    ),
    "Cinema" -> Seq(
      BFI(),
      Cineworld(),
      Odeon(),
      Picturehouse(),
      ScienceMuseum(),
      TheCinema(),
      Vue()
    ),
    "Haircut" -> Seq(
      LSB()
    )
  )

  val AllList: Seq[Merchant[?, ?, ?]] = All.values.flatten.toSeq

  val AllByName: Map[String, Merchant[?, ?, ?]] =
    AllList.collect(merchant => (merchant.name, merchant)).toMap

  def findMerchantFor[M](
      criteria: Criteria[M]
  ): Merchant[M, Criteria[M], Event[M]] =
    AllByName(criteria.merchant)
      .asInstanceOf[Merchant[M, Criteria[M], Event[M]]]

  def findMerchantFor[M](
      event: Event[M]
  ): Merchant[M, Criteria[M], Event[M]] =
    AllByName(event.merchant)
      .asInstanceOf[Merchant[M, Criteria[M], Event[M]]]
