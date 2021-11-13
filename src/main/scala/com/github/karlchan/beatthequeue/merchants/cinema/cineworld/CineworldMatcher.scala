package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.Matcher
import com.github.karlchan.beatthequeue.merchants.Event
import com.github.karlchan.beatthequeue.merchants.Criteria

class CineworldMatcher extends Matcher[Cineworld]:
  override def matches(event: Event[Cineworld], criteria: Criteria[Cineworld]) =
    ???
