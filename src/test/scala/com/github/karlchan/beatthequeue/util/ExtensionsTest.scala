package com.github.karlchan.beatthequeue.util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.time.LocalDate

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

  "Seq.mapOrTrue" should "return decide correctly" in {
    Seq().mapOrTrue(_ => false) should be(true)
    Seq(1, 1).mapOrTrue(_ == 1) should be(true)
    Seq(1, 2).mapOrTrue(_ == 1) should be(false)
  }

  "Seq.mapOrFalse" should "return decide correctly" in {
    Seq().mapOrFalse(_ => true) should be(false)
    Seq(1, 1).mapOrFalse(_ == 1) should be(true)
    Seq(1, 2).mapOrFalse(_ == 1) should be(false)
  }
