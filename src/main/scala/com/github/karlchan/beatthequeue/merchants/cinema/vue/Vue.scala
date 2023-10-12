package com.github.karlchan.beatthequeue.merchants.cinema.vue

import cats.effect.IO
import cats.implicits._
import com.github.karlchan.beatthequeue.merchants.Merchant
import io.circe.generic.auto._
import io.circe.syntax._

final class Vue extends Merchant[Vue, VueCriteria, VueEvent]:
  override val name = Vue.Name
  override val logoUrl =
    "https://www.myvue.com/-/jssmedia/global/img/logo-header-vue.png"
  override val eventFinder = VueCrawler()
  override val criteriaFactory = () => VueCriteria()
  override val renderer = VueRenderer()

object Vue:
  val Name = "Vue"
