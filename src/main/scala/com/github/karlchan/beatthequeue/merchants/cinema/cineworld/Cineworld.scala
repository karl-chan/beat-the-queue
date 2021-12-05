package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Merchant

final class Cineworld extends Merchant[Cineworld]:
  private val crawler = CineworldCrawler()
  override val name = "cineworld"
  override val eventFinder = crawler
