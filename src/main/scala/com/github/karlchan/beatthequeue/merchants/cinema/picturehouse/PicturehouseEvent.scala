package com.github.karlchan.beatthequeue.merchants.cinema.picturehouse

import java.time.LocalDateTime

import com.github.karlchan.beatthequeue.merchants.Event

final case class PicturehouseEvent(
    override val merchant: String = Picturehouse.Name,
    override val name: String,
    override val time: LocalDateTime,
    venue: String,
    screenType: String
) extends Event[Picturehouse]
