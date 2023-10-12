package com.github.karlchan.beatthequeue.merchants.attraction.horizon22

import java.time.LocalDateTime

import com.github.karlchan.beatthequeue.merchants.Event

final case class Horizon22Event(
    override val merchant: String = Horizon22.Name,
    override val name: String = "Standard Ticket",
    override val time: LocalDateTime
) extends Event[Horizon22]
