package com.github.karlchan.beatthequeue.merchants

import cats.effect.IO
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.Cineworld
import com.github.karlchan.beatthequeue.merchants.cinema.cineworld.CineworldCriteria
import com.github.karlchan.beatthequeue.merchants.given_Decoder_Criteria
import com.github.karlchan.beatthequeue.merchants.given_Encoder_Criteria
import org.scalatest.EitherValues._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers.all

import java.time.LocalDate

final class MerchantTest extends AnyFlatSpec with should.Matchers:
  "criteria" should "be encodable to / decodable from json losslessly" in {
    val criteria: Criteria[Cineworld] = CineworldCriteria(
      filmNames = Seq("Dune", "No Time To Die"),
      startDate = Some(LocalDate.of(2021, 10, 15)),
      endDate = None,
      venues = Seq("Leicester Square"),
      screenTypes = Seq.empty
    )
    val encoded = given_Encoder_Criteria.apply(criteria)
    val decoded = given_Decoder_Criteria.decodeJson(encoded)
    decoded.right.value should equal(criteria)
  }
