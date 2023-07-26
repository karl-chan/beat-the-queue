package com.github.karlchan.beatthequeue.util

import java.time.DayOfWeek

import scala.util.Try

import io.circe.Decoder
import io.circe.Encoder

val dayOfWeekEncoder: Encoder[DayOfWeek] =
  Encoder.encodeInt.contramap[DayOfWeek](_.getValue() % 7)

val dayOfWeekDecoder: Decoder[DayOfWeek] = Decoder.decodeInt.emapTry { i =>
  Try(i match
    case 0 => DayOfWeek.SUNDAY
    case _ => DayOfWeek.of(i)
  )
}
