package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.Merchant

class Cineworld extends Merchant[Cineworld]:
  override def name = "cineworld"
  override def eventFinder = CineworldCrawler()
  override def matcher = CineworldMatcher()
