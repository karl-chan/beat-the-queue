package com.github.karlchan.beatthequeue.merchants.cinema.sciencemuseum

import com.github.karlchan.beatthequeue.merchants.Event

import java.time.LocalDateTime

final case class ScienceMuseumEvent(
    override val merchant: String = ScienceMuseum.Name,
    override val name: String,
    override val time: LocalDateTime,
    val productTypeId: String
) extends Event[ScienceMuseum]
