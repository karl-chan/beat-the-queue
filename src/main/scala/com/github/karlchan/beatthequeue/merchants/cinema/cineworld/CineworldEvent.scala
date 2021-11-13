package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.Event

import java.time.LocalDateTime

case class CineworldEvent(
    name: String,
    time: LocalDateTime,
    venue: String,
    screenType: String
) extends Event[Cineworld]
