package com.github.karlchan.beatthequeue.merchants.cinema.vue

import com.github.karlchan.beatthequeue.merchants.Event

import java.time.LocalDateTime

final case class VueEvent(
    override val merchant: String = Vue.Name,
    override val name: String,
    override val time: LocalDateTime,
    venue: String,
    screenTypes: Seq[String]
) extends Event[Vue]
