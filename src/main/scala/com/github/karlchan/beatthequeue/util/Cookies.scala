package com.github.karlchan.beatthequeue.util

import java.time.Instant

import sttp.model.headers.CookieWithMeta

object Cookies:
  def merge(
      oldCookies: Seq[CookieWithMeta],
      newCookies: Seq[CookieWithMeta]
  ): Seq[CookieWithMeta] = {
    if (newCookies.isEmpty) {
      oldCookies
    } else {
      // The last cookie with the same name in the same domain takes precendence.
      val newCookiesDeduped =
        newCookies
          .groupMapReduce(c => (c.domain, c.name))(identity)((_, last) => last)
          .values
          .toVector
      val newCookieNames = newCookiesDeduped.map(_.name).toSet
      val mergedCookies = newCookiesDeduped
        ++ oldCookies.filterNot(cookie => newCookieNames.contains(cookie.name))
      mergedCookies.filter(_.expires.mapOrTrue(_.isAfter(Instant.now())))
    }
  }
