package com.github.karlchan.beatthequeue.merchants.cinema.odeon

import java.time.LocalDateTime

import com.github.karlchan.beatthequeue.merchants.Event

final case class OdeonEvent(
    override val merchant: String = Odeon.Name,
    override val name: String,
    override val time: LocalDateTime,
    venue: String,
    screenType: String
) extends Event[Odeon]
