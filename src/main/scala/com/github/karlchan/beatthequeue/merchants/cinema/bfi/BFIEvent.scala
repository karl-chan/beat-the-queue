package com.github.karlchan.beatthequeue.merchants.cinema.bfi

import com.github.karlchan.beatthequeue.merchants.Event

import java.time.LocalDateTime

final case class BFIEvent(
    override val merchant: String = BFI.Name,
    override val name: String,
    override val time: LocalDateTime,
    venue: String,
    screenType: String
) extends Event[BFI]
