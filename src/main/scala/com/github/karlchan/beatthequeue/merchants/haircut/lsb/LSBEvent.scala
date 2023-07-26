package com.github.karlchan.beatthequeue.merchants.haircut.lsb

import java.time.LocalDateTime

import com.github.karlchan.beatthequeue.merchants.Event

final case class LSBEvent(
    override val merchant: String = LSB.Name,
    override val name: String,
    override val time: LocalDateTime,
    val category: String
) extends Event[LSB]
