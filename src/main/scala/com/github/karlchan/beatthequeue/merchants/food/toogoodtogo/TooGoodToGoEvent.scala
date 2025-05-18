package com.github.karlchan.beatthequeue.merchants.food.toogoodtogo

import java.time.LocalDateTime

import com.github.karlchan.beatthequeue.merchants.Event

final case class TooGoodToGoEvent(
    override val merchant: String = TooGoodToGo.Name,
    override val name: String,
    override val time: LocalDateTime
) extends Event[TooGoodToGo]
