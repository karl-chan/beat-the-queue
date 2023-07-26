package com.github.karlchan.beatthequeue.merchants.cinema.vue

import java.time.LocalDateTime

import com.github.karlchan.beatthequeue.merchants.Event

final case class VueEvent(
    override val merchant: String = Vue.Name,
    override val name: String,
    override val time: LocalDateTime,
    venue: String,
    screenTypes: Seq[String]
) extends Event[Vue]
