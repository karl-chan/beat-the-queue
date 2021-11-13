package com.github.karlchan.beatthequeue.merchants

import cats.effect.IO

trait Merchant[M]:
  def name: String
  def eventFinder: EventFinder[M]
  def matcher: Matcher[M]

trait Event[M]:
  def name: String

trait EventFinder[M]:
  def run(): IO[Seq[Event[M]]]

trait Criteria[M]

trait Matcher[M]:
  def matches(event: Event[M], criteria: Criteria[M]): Boolean
