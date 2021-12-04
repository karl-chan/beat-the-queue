package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.Merchant

class Cineworld extends Merchant[Cineworld]:
  override val name = "cineworld"
  override val eventFinder = CineworldCrawler()
  override val matcher = CineworldMatcher()
