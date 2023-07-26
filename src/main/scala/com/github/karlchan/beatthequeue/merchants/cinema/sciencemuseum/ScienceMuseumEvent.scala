package com.github.karlchan.beatthequeue.merchants.cinema.sciencemuseum

import java.time.LocalDateTime

import com.github.karlchan.beatthequeue.merchants.Event

final case class ScienceMuseumEvent(
    override val merchant: String = ScienceMuseum.Name,
    override val name: String,
    override val time: LocalDateTime,
    val productTypeId: String
) extends Event[ScienceMuseum]
