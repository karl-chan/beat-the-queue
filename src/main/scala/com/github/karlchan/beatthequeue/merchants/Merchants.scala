package com.github.karlchan.beatthequeue.merchants

import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.Cineworld

private type Category = String

object Merchants:
  val All: Map[Category, Seq[Merchant[_, _, _]]] = Map {
    "Cinema" -> Seq(
      Cineworld()
    )
  }

  val AllList: Seq[Merchant[_, _, _]] = All.values.flatten.toSeq

  val AllByName: Map[String, Merchant[_, _, _]] =
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
