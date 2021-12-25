package com.github.karlchan.beatthequeue.server.routes.pages

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.Criteria
import com.github.karlchan.beatthequeue.merchants.Merchants
import com.github.karlchan.beatthequeue.merchants.given_Encoder_Criteria
import com.github.karlchan.beatthequeue.server.routes.pages.templates.widgets._

object CriteriaEditPage:
  def render[M](criteria: Criteria[M]): IO[Html] =
    for {
      editor <- Merchants
        .findMerchantFor(criteria)
        .renderer
        .renderEditor(criteria)
    } yield Template.styledPage(
      navigationBar,
      editor
    )
