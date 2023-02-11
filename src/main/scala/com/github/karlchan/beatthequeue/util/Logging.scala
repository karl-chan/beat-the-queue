package com.github.karlchan.beatthequeue.util

import ch.qos.logback.classic.Level
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logging:
  private val level =
    LoggerFactory
      .getLogger(Logger.ROOT_LOGGER_NAME)
      .asInstanceOf[ch.qos.logback.classic.Logger]
      .getLevel()
      .toInt()

  def isDebug: Boolean = level <= Level.DEBUG_INT
