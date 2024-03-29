package com.github.karlchan.beatthequeue.util

import java.time.LocalDate

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

final class ExtensionsTest extends AnyFlatSpec with should.Matchers:
  "LocalDate.shortFormat" should "return short format" in {
    LocalDate.of(2001, 1, 1).shortFormat should be("2001-01-01")
  }

  "String.containsIgnoreCase" should "return decide correctly" in {
    "The Quick Brown Fox".containsIgnoreCase("qUICK bROWN") should be(true)
    "Quick Brown".containsIgnoreCase("tHE qUICK bROWN fOX") should be(false)
  }

  "Option.mapOrTrue" should "return decide correctly" in {
    None.mapOrTrue(_ => false) should be(true)
    Some(1).mapOrTrue(_ == 1) should be(true)
    Some(1).mapOrTrue(_ == 2) should be(false)
  }

  "Option.mapOrFalse" should "return decide correctly" in {
    None.mapOrFalse(_ => true) should be(false)
    Some(1).mapOrFalse(_ == 1) should be(true)
    Some(1).mapOrFalse(_ == 2) should be(false)
  }

  "Seq.any" should "default to true if empty" in {
    Seq().any(_ => false) should be(true)
    Seq(1, 1).any(_ == 1) should be(true)
    Seq(2, 2).any(_ == 1) should be(false)
  }
