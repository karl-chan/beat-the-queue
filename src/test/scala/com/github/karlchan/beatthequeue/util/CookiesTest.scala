package com.github.karlchan.beatthequeue.util

import java.time.Instant

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import sttp.model.headers.CookieWithMeta

final class CookiesTest extends AnyFlatSpec with should.Matchers:
  "merge" should "deduplicate cookies of the same name" in {
    val oldCookies = Seq(
      CookieWithMeta(name = "A", value = "old_a"),
      CookieWithMeta(name = "C", value = "old_c")
    )
    val newCookies = Seq(
      CookieWithMeta(name = "A", value = "new_a"),
      CookieWithMeta(name = "B", value = "new_b")
    )

    val cookies = Cookies.merge(oldCookies, newCookies)

    cookies should contain theSameElementsAs Vector(
      CookieWithMeta(name = "A", value = "new_a"),
      CookieWithMeta(name = "B", value = "new_b"),
      CookieWithMeta(name = "C", value = "old_c")
    )
  }

  "merge" should "remove expired cookies" in {
    val past = Instant.now().minusSeconds(60)
    val future = Instant.now().plusSeconds(60)
    val oldCookies = Seq(
      CookieWithMeta(
        name = "expired_old",
        value = "",
        expires = Some(past)
      ),
      CookieWithMeta(
        name = "not_expired",
        value = "",
        expires = Some(future)
      )
    )
    val newCookies = Seq(
      CookieWithMeta(
        name = "expired_new",
        value = "",
        expires = Some(past)
      )
    )

    val cookies = Cookies.merge(oldCookies, newCookies)

    cookies should contain theSameElementsAs Vector(
      CookieWithMeta(
        name = "not_expired",
        value = "",
        expires = Some(future)
      )
    )

  }
