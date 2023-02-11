package com.github.karlchan.beatthequeue.merchants.cinema.thecinema

import com.github.karlchan.beatthequeue.merchants.Event

import java.time.LocalDateTime

final case class TheCinemaEvent(
    override val merchant: String = TheCinema.Name,
    override val name: String,
    override val time: LocalDateTime,
    venue: String,
    screenType: String
) extends Event[TheCinema]
