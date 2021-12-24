package com.github.karlchan.beatthequeue.merchants

import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.Cineworld

private type Category = String

object Merchants:
  val All: Map[Category, Seq[Merchant[_, _]]] = Map {
    "Cinema" -> Seq(
      Cineworld()
    )
  }

  val AllList: Seq[Merchant[_, _]] = All.values.flatten.toSeq

  val AllByName: Map[String, Merchant[_, _]] =
    AllList.collect(merchant => (merchant.name, merchant)).toMap

  def findMerchantFor[M](
      criteria: Criteria[M]
  ): Merchant[M, Criteria[M]] =
    AllByName(criteria.merchant).asInstanceOf[Merchant[M, Criteria[M]]]
