package com.github.karlchan.beatthequeue.merchants

import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.Cineworld

object Merchants:
  val All: Seq[Merchant[?]] = Vector {
    Cineworld()
  }
