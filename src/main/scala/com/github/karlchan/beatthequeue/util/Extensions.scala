package com.github.karlchan.beatthequeue.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

extension (date: LocalDate)
  def shortFormat: String = ISO_LOCAL_DATE.format(date)
