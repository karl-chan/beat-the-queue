package com.github.karlchan.beatthequeue.merchants.cinema.cineworld

import com.github.karlchan.beatthequeue.merchants.Codecs
import io.circe.Decoder
import io.circe.Encoder

class CineworldCodecs(using
    encoder: Encoder[CineworldCriteria],
    decoder: Decoder[CineworldCriteria]
) extends Codecs[Cineworld]:
  override val criteriaEncoder =
    encoder.contramap(_.asInstanceOf[CineworldCriteria])
  override val criteriaDecoder = decoder.map(identity)
