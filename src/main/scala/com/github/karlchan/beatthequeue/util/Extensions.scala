package com.github.karlchan.beatthequeue.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

extension (date: LocalDate)
  def shortFormat: String = ISO_LOCAL_DATE.format(date)

extension [T](option: Option[T])
  def mapTruthy(f: T => Boolean): Boolean = option.map(f).getOrElse(true)
  def mapFalsy(f: T => Boolean): Boolean = option.map(f).getOrElse(false)

extension [T](seq: Seq[T])
  def mapTruthy(f: T => Boolean): Boolean =
    if seq.nonEmpty then seq.forall(f) else true
  def mapFalsy(f: T => Boolean): Boolean =
    if seq.nonEmpty then seq.forall(f) else false
