package com.github.karlchan.beatthequeue.merchants

import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.Cineworld

private type Category = String

object Merchants:
  val All: Map[Category, Seq[Merchant[_]]] = Map {
    "cinema" -> Seq(
      Cineworld()
    )
  }

  val AllList: Seq[Merchant[_]] = All.values.flatten.toSeq

  val AllByName: Map[String, Merchant[_]] =
    AllList.collect(merchant => (merchant.name, merchant)).toMap
