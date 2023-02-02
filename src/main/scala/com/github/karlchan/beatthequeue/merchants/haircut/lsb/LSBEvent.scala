package com.github.karlchan.beatthequeue.merchants.haircut.lsb

import com.github.karlchan.beatthequeue.merchants.Event

import java.time.LocalDateTime

final case class LSBEvent(
    override val merchant: String = LSB.Name,
    override val name: String,
    override val time: LocalDateTime,
    val category: String
) extends Event[LSB]
