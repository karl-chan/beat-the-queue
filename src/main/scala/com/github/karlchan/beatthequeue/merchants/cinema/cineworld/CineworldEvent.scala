package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import java.time.LocalDateTime

import com.github.karlchan.beatthequeue.merchants.Event

final case class CineworldEvent(
    override val merchant: String = Cineworld.Name,
    override val name: String,
    override val time: LocalDateTime,
    venue: String,
    screenType: String
) extends Event[Cineworld]
