package com.github.karlchan.beatthequeue.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

extension (date: LocalDate)
  def shortFormat: String = ISO_LOCAL_DATE.format(date)

extension (s: String)
  def containsIgnoreCase(other: String): Boolean =
    s.toLowerCase.contains(other.toLowerCase)

extension [T](option: Option[T])
  def mapOrTrue(f: T => Boolean): Boolean = option.map(f).getOrElse(true)
  def mapOrFalse(f: T => Boolean): Boolean = option.map(f).getOrElse(false)

extension [T](seq: Seq[T])
  def any(f: T => Boolean): Boolean =
    if seq.nonEmpty then seq.exists(f) else true
