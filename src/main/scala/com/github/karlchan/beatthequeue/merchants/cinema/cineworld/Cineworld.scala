package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Merchant

class Cineworld extends Merchant[Cineworld]:
  private val crawler = CineworldCrawler()
  override val name = "cineworld"
  override val eventFinder = crawler
  override val matcher = CineworldMatcher()
  override def criteriaTemplate() =
    for {
      cinemasRes <- crawler.getCinemas()
      res <- Seq(crawler.getNowPlaying(), crawler.getComingSoon()).parSequence
      Seq(nowPlayingRes, comingSoonRes) = res
      posters = nowPlayingRes.body.posters ::: comingSoonRes.body.posters
      names = posters.map(_.featureTitle)
      venues = cinemasRes.body.cinemas.map(_.displayName)
    } yield CineworldCriteria(
      names = names,
      venues = venues,
      screenTypes = BaseFormats ::: SpecialFormats
    )
