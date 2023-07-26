package com.github.karlchan.beatthequeue.merchants.cinema.bfi

import java.time.LocalDateTime

import com.github.karlchan.beatthequeue.merchants.Event

final case class BFIEvent(
    override val merchant: String = BFI.Name,
    override val name: String,
    override val time: LocalDateTime,
    venue: String,
    screenType: String
) extends Event[BFI]
