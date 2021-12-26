package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.Event

import java.time.LocalDateTime

final case class CineworldEvent(
    override val merchant: String = Cineworld.Name,
    override val name: String,
    override val time: LocalDateTime,
    venue: String,
    screenType: String
) extends Event[Cineworld]
